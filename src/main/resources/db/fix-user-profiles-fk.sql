-- Run this once so all child tables reference user_profiles (not job_seeker_profiles).
-- Use when JobSeekerProfile entity uses @Table(name = "user_profiles").
--
-- If DROP fails (wrong constraint name), find names with:
--   SELECT conrelid::regclass, conname FROM pg_constraint
--   WHERE contype = 'f' AND pg_get_constraintdef(oid) LIKE '%profile_id%';

-- 0. education: point FK to user_profiles
ALTER TABLE education
  DROP CONSTRAINT IF EXISTS fkp4fo0m6mp47dg55l1a34qqyor;

ALTER TABLE education
  ADD CONSTRAINT education_profile_fk
  FOREIGN KEY (profile_id) REFERENCES user_profiles(id);

-- 1. job_seeker_skills: point FK to user_profiles
ALTER TABLE job_seeker_skills
  DROP CONSTRAINT IF EXISTS fkgkvklxijrs2g2qy6hxwq4d7u6;

ALTER TABLE job_seeker_skills
  ADD CONSTRAINT job_seeker_skills_profile_fk
  FOREIGN KEY (profile_id) REFERENCES user_profiles(id);

-- 2. job_seeker_primary_skills
DO $$
DECLARE
  cname text;
BEGIN
  SELECT conname INTO cname FROM pg_constraint
  WHERE conrelid = 'job_seeker_primary_skills'::regclass AND contype = 'f' LIMIT 1;
  IF cname IS NOT NULL THEN
    EXECUTE format('ALTER TABLE job_seeker_primary_skills DROP CONSTRAINT %I', cname);
  END IF;
END $$;
ALTER TABLE job_seeker_primary_skills
  ADD CONSTRAINT job_seeker_primary_skills_profile_fk
  FOREIGN KEY (profile_id) REFERENCES user_profiles(id);

-- 3. job_seeker_basic_skills
DO $$
DECLARE
  cname text;
BEGIN
  SELECT conname INTO cname FROM pg_constraint
  WHERE conrelid = 'job_seeker_basic_skills'::regclass AND contype = 'f' LIMIT 1;
  IF cname IS NOT NULL THEN
    EXECUTE format('ALTER TABLE job_seeker_basic_skills DROP CONSTRAINT %I', cname);
  END IF;
END $$;
ALTER TABLE job_seeker_basic_skills
  ADD CONSTRAINT job_seeker_basic_skills_profile_fk
  FOREIGN KEY (profile_id) REFERENCES user_profiles(id);

-- 4. All other profile child tables (work_experience, projects, achievements, etc.): point FK to user_profiles
DO $$
DECLARE
  r RECORD;
  tbl_name text;
  new_fk_name text;
BEGIN
  FOR r IN
    SELECT c.conrelid AS relid, c.conname AS fk_name
    FROM pg_constraint c
    WHERE c.contype = 'f'
      AND c.confrelid = 'job_seeker_profiles'::regclass
  LOOP
    tbl_name := r.relid::regclass::text;
    new_fk_name := regexp_replace(tbl_name, '^.*\.', '') || '_profile_fk';
    EXECUTE format('ALTER TABLE %s DROP CONSTRAINT IF EXISTS %I', tbl_name, r.fk_name);
    EXECUTE format('ALTER TABLE %s DROP CONSTRAINT IF EXISTS %I', tbl_name, new_fk_name);
    EXECUTE format('ALTER TABLE %s ADD CONSTRAINT %I FOREIGN KEY (profile_id) REFERENCES user_profiles(id)',
      tbl_name,
      new_fk_name
    );
  END LOOP;
END $$;
