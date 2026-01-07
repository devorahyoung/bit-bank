-- This ensures the database exists and has the correct owner
-- CREATE DATABASE my_bank WITH OWNER agritask;

-- Connect to the new database
\c my_bank

-- Create schema and set permissions
CREATE SCHEMA IF NOT EXISTS public;
GRANT ALL ON SCHEMA public TO agritask;
GRANT ALL ON ALL TABLES IN SCHEMA public TO agritask;
GRANT ALL ON ALL SEQUENCES IN SCHEMA public TO agritask;
