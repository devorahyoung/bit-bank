-- Create processed_events table for tracking processed transactions
CREATE TABLE IF NOT EXISTS processed_events (
                                  transaction_id VARCHAR(255) PRIMARY KEY,
                                  processed_at TIMESTAMP NOT NULL
);

-- Create audit_log table for tracking transactions
CREATE TABLE IF NOT EXISTS  audit_log (
                           id BIGSERIAL PRIMARY KEY,
                           transaction_id VARCHAR(255) NOT NULL,
                           source_account_id VARCHAR(255) NULL,
                           target_account_id VARCHAR(255) NOT NULL,
                           amount NUMERIC(38,2) NOT NULL,
                           timestamp TIMESTAMP NOT NULL,
                           event_type VARCHAR(50) NOT NULL DEFAULT 'TRANSFER'
);
ALTER TABLE audit_log ALTER COLUMN source_account_id DROP NOT NULL;

-- Create ledger_entries table for tracking debits and credits
CREATE TABLE IF NOT EXISTS  ledger_entries (
                                id BIGSERIAL PRIMARY KEY,
                                transaction_id VARCHAR(255) NOT NULL,
                                account_id VARCHAR(255) NOT NULL,
                                amount NUMERIC(38,2) NOT NULL,
                                timestamp TIMESTAMP NOT NULL,
                                entry_type VARCHAR(10) NOT NULL,
                                CONSTRAINT chk_entry_type CHECK (entry_type IN ('DEBIT', 'CREDIT'))
);

-- Create daily_traffic_stats table for tracking daily statistics
CREATE TABLE  daily_traffic_stats (
                                     date DATE PRIMARY KEY,
                                     total_count BIGINT NOT NULL DEFAULT 0,
                                     total_volume NUMERIC(38,2) NOT NULL DEFAULT 0,
                                     version BIGINT NOT NULL DEFAULT 0
);

-- Create indexes for better query performance
CREATE INDEX idx_audit_log_transaction_id ON audit_log(transaction_id);
CREATE INDEX idx_audit_log_timestamp ON audit_log(timestamp);

CREATE INDEX idx_ledger_entries_transaction_id ON ledger_entries(transaction_id);
CREATE INDEX idx_ledger_entries_account_id ON ledger_entries(account_id);
CREATE INDEX idx_ledger_entries_timestamp ON ledger_entries(timestamp);

CREATE INDEX idx_processed_events_processed_at ON processed_events(processed_at);