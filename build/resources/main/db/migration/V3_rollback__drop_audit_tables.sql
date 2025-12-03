-- =============================================================
-- Rollback Migration: V3 - Drop Audit Tables
-- Description: Rollback for V3__create_audit_tables.sql
-- Created: 2024
-- =============================================================

-- Drop audit logs table
DROP TABLE IF EXISTS audit_logs;

