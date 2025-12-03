-- =============================================================
-- Migration: V4 - Add deleted_at for Soft Delete
-- Description: Add deleted_at column to users and roles tables for soft delete functionality
-- Created: 2024
-- =============================================================

-- Add deleted_at column to users table (H2 compatible - no IF NOT EXISTS in ALTER TABLE)
-- Check if column exists before adding (H2 doesn't support IF NOT EXISTS in ALTER TABLE)
ALTER TABLE users ADD COLUMN deleted_at TIMESTAMP NULL DEFAULT NULL;

-- Add deleted_at column to roles table
ALTER TABLE roles ADD COLUMN deleted_at TIMESTAMP NULL DEFAULT NULL;

