-- Allow multiple service provider profiles per email (remove unique constraint on email)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'service_provider_profiles') THEN
        ALTER TABLE service_provider_profiles DROP CONSTRAINT IF EXISTS service_provider_profiles_email_key;
    END IF;
END $$;
