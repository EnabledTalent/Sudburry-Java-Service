-- One-time cleanup: remove jobs id 3 and 5 (FK-safe order).
-- Child tables referencing jobs.id (see models): saved_jobs, job_invites,
-- JobApplicationRequest -> physical table is usually applicant_details (Spring camelCase -> snake_case).
-- Run manually against PostgreSQL when needed (e.g. psql, DBeaver); not executed at app startup.

DO $$
DECLARE
  applicant_table text;
BEGIN
  IF EXISTS (
    SELECT 1 FROM pg_tables WHERE schemaname = 'public' AND tablename = 'saved_jobs'
  ) THEN
    DELETE FROM saved_jobs WHERE job_id IN (3, 5);
  END IF;

  IF EXISTS (
    SELECT 1 FROM pg_tables WHERE schemaname = 'public' AND tablename = 'job_invites'
  ) THEN
    DELETE FROM job_invites WHERE job_id IN (3, 5);
  END IF;

  SELECT c.relname INTO applicant_table
  FROM pg_class c
  JOIN pg_namespace n ON n.oid = c.relnamespace
  WHERE n.nspname = 'public'
    AND c.relkind = 'r'
    AND c.relname IN ('applicant_details', 'applicantdetails', 'applicantDetails')
  ORDER BY CASE c.relname
    WHEN 'applicant_details' THEN 1
    WHEN 'applicantdetails' THEN 2
    WHEN 'applicantDetails' THEN 3
  END
  LIMIT 1;

  IF applicant_table IS NOT NULL THEN
    EXECUTE format('DELETE FROM %I WHERE job_id IN (3, 5)', applicant_table);
  END IF;

  IF EXISTS (
    SELECT 1 FROM pg_tables WHERE schemaname = 'public' AND tablename = 'jobs'
  ) THEN
    DELETE FROM jobs WHERE id IN (3, 5);
  END IF;
END $$;
