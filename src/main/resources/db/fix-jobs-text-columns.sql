-- Fix "value too long for type character varying(255)" when inserting into jobs.
-- Your entity marks some fields as TEXT, but existing DB columns may still be varchar(255).
-- This migration widens all string-like columns on `jobs` to TEXT (safe in Postgres).

ALTER TABLE jobs ALTER COLUMN role TYPE TEXT;
ALTER TABLE jobs ALTER COLUMN location TYPE TEXT;
ALTER TABLE jobs ALTER COLUMN employment_type TYPE TEXT;
ALTER TABLE jobs ALTER COLUMN requirements TYPE TEXT;
ALTER TABLE jobs ALTER COLUMN description TYPE TEXT;
ALTER TABLE jobs ALTER COLUMN type_of_work TYPE TEXT;
ALTER TABLE jobs ALTER COLUMN address TYPE TEXT;
ALTER TABLE jobs ALTER COLUMN experience_range TYPE TEXT;
ALTER TABLE jobs ALTER COLUMN preferred_language TYPE TEXT;
ALTER TABLE jobs ALTER COLUMN company_name TYPE TEXT;

