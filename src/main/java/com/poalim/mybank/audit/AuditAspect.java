package com.poalim.mybank.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
public class AuditAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(AuditAspect.class);
    
    @Autowired
    private AuditRepository auditRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Around("@annotation(auditable)")
    public Object auditMethod(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        long startTime = System.currentTimeMillis();
        LocalDateTime timestamp = LocalDateTime.now();
        
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String methodName = method.getDeclaringClass().getSimpleName() + "." + method.getName();
        
        Object[] args = joinPoint.getArgs();
        String operationType = auditable.operationType().isEmpty() ? 
            method.getName().toUpperCase() : auditable.operationType();
        
        Audit audit = new Audit();
        audit.setOperationType(operationType);
        audit.setMethodName(methodName);
        audit.setTimestamp(timestamp);
        
        // Extract account ID from parameters
        Long accountId = extractAccountId(args, signature.getParameterNames());
        audit.setAccountId(accountId);
        
        // Capture parameters if enabled
        if (auditable.captureParameters()) {
            audit.setParameters(serializeParameters(args, signature));
        }
        
        // Set session and user info (you can enhance this based on your security context)
        audit.setUserId("system"); // Replace with actual user from security context
        audit.setSessionId("session-" + System.currentTimeMillis()); // Replace with actual session ID
        
        Object result = null;
        try {
            result = joinPoint.proceed();
            
            // Capture result if enabled and successful
            if (auditable.captureResult() && result != null) {
                audit.setResult(objectMapper.writeValueAsString(result));
            }
            
            audit.setSuccess(true);
            
        } catch (Exception e) {
            audit.setSuccess(false);
            audit.setErrorMessage(e.getMessage());
            throw e;
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;
            audit.setExecutionTime(executionTime);
            
            try {
                auditRepository.save(audit);
            } catch (Exception e) {
                logger.error("Failed to save audit record", e);
            }
        }
        
        return result;
    }
    
    private Long extractAccountId(Object[] args, String[] paramNames) {
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof Long && 
                (paramNames[i].contains("id") || paramNames[i].contains("accountId"))) {
                return (Long) args[i];
            }
            
            // Handle request objects that might contain account ID
            if (args[i] != null) {
                try {
                    // Try to extract from request objects using reflection
                    Object accountId = extractFromObject(args[i], "accountId", "fromAccountId", "toAccountId", "id");
                    if (accountId instanceof Long) {
                        return (Long) accountId;
                    }
                } catch (Exception e) {
                    // Ignore reflection errors
                }
            }
        }
        return null;
    }
    
    private Object extractFromObject(Object obj, String... fieldNames) {
        Class<?> clazz = obj.getClass();
        for (String fieldName : fieldNames) {
            try {
                java.lang.reflect.Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                Object value = field.get(obj);
                if (value != null) {
                    return value;
                }
            } catch (Exception e) {
                // Try getter method
                try {
                    String getterName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                    java.lang.reflect.Method getter = clazz.getMethod(getterName);
                    Object value = getter.invoke(obj);
                    if (value != null) {
                        return value;
                    }
                } catch (Exception ex) {
                    // Continue to next field
                }
            }
        }
        return null;
    }
    
    private String serializeParameters(Object[] args, MethodSignature signature) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            String[] paramNames = signature.getParameterNames();
            
            for (int i = 0; i < args.length; i++) {
                String paramName = (paramNames != null && i < paramNames.length) ? 
                    paramNames[i] : "param" + i;
                paramMap.put(paramName, args[i]);
            }
            
            return objectMapper.writeValueAsString(paramMap);
        } catch (Exception e) {
            return "Error serializing parameters: " + e.getMessage();
        }
    }
}
