-- Create account table
CREATE TABLE IF NOT EXISTS account (
    id BIGSERIAL PRIMARY KEY,
    owner_name VARCHAR(255) NOT NULL,
    balance NUMERIC(38,2) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT check_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'CLOSED', 'SUSPENDED'))
);

-- Create index on owner_name for faster lookups
CREATE INDEX IF NOT EXISTS idx_account_owner_name ON account(owner_name);

-- Create index on status for filtering
CREATE INDEX IF NOT EXISTS idx_account_status ON account(status);
