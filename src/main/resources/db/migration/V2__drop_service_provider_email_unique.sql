-- Drop unique constraint on email (allows multiple profiles per email)
-- Handles both Hibernate-generated names (uk...) and PostgreSQL default (table_column_key)
DO $$
DECLARE
    r RECORD;
BEGIN
    FOR r IN (
        SELECT c.conname
        FROM pg_constraint c
        JOIN pg_class t ON c.conrelid = t.oid
        WHERE t.relname = 'service_provider_profiles'
          AND c.contype = 'u'
          AND EXISTS (
              SELECT 1 FROM pg_attribute a
              WHERE a.attrelid = c.conrelid
                AND a.attnum = ANY(c.conkey)
                AND a.attname = 'email'
                AND NOT a.attisdropped
          )
    ) LOOP
        EXECUTE format('ALTER TABLE service_provider_profiles DROP CONSTRAINT IF EXISTS %I', r.conname);
    END LOOP;
END $$;
