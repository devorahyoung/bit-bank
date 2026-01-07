-- V2__Create_transfer_record_table.sql

CREATE TABLE transfer_record (
    id BIGSERIAL PRIMARY KEY,
    idempotency_key VARCHAR(255) UNIQUE NOT NULL,
    from_account_id BIGINT NOT NULL,
    to_account_id BIGINT NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    
    CONSTRAINT fk_transfer_from_account 
        FOREIGN KEY (from_account_id) 
        REFERENCES account(id),
    
    CONSTRAINT fk_transfer_to_account 
        FOREIGN KEY (to_account_id) 
        REFERENCES account(id),
        
    CONSTRAINT chk_transfer_amount_positive 
        CHECK (amount > 0),
        
    CONSTRAINT chk_transfer_different_accounts 
        CHECK (from_account_id != to_account_id)
);

-- Create indexes for better query performance
CREATE INDEX idx_transfer_record_idempotency_key ON transfer_record(idempotency_key);
CREATE INDEX idx_transfer_record_from_account ON transfer_record(from_account_id);
CREATE INDEX idx_transfer_record_to_account ON transfer_record(to_account_id);
CREATE INDEX idx_transfer_record_created_at ON transfer_record(created_at);
CREATE INDEX idx_transfer_record_status ON transfer_record(status);
