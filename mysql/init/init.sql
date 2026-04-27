-- Initialize Hobbie Database
CREATE DATABASE IF NOT EXISTS hobbie_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE hobbie_db;

-- Create tables (if not already handled by Hibernate/JPA)
-- This script can be used for initial database setup
-- JPA/Hibernate will create/update tables based on entities

-- Grant permissions
GRANT ALL PRIVILEGES ON hobbie_db.* TO 'hobbie_user'@'%';
FLUSH PRIVILEGES;
