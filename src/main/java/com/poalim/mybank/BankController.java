package com.poalim.mybank;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Bank", description = "Banking system APIs")
public class BankController {

    @GetMapping("/api/health")
    @Operation(summary = "Health check", description = "Check if the banking system is up and running")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "System is operational")
    })
    public String health() {
        return "Banking System is Up";
    }
}

