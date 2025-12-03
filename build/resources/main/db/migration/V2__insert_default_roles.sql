-- =============================================================
-- Migration: V2 - Insert Default Roles
-- Description: Insert default system roles
-- Created: 2024
-- =============================================================

-- Insert default roles (using MERGE/ON CONFLICT to prevent duplicates - H2 compatible)
MERGE INTO roles (id, name, description) KEY (name) VALUES
    (1, 'ROLE_USER', 'Standard user role'),
    (2, 'ROLE_ADMIN', 'Administrator role'),
    (3, 'ROLE_MODERATOR', 'Moderator role');

