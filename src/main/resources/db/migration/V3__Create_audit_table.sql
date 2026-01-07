-- V3__Create_audit_table
CREATE TABLE audit (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT,
    operation_type VARCHAR(50) NOT NULL,
    method_name VARCHAR(100) NOT NULL,
    parameters TEXT,
    result TEXT,
    execution_time BIGINT,
    success BOOLEAN NOT NULL,
    error_message VARCHAR(500),
    timestamp TIMESTAMP NOT NULL,
    user_id VARCHAR(50),
    session_id VARCHAR(100)
);

-- Create indexes for better query performance
CREATE INDEX idx_audit_account_id ON audit(account_id);
CREATE INDEX idx_audit_timestamp ON audit(timestamp);
CREATE INDEX idx_audit_operation_type ON audit(operation_type);
CREATE INDEX idx_audit_account_operation ON audit(account_id, operation_type);
