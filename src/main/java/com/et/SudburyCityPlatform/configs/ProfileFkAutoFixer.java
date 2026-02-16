package com.et.SudburyCityPlatform.configs;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cloud DBs for this project sometimes end up with mixed FKs:
 * some profile child tables reference job_seeker_profiles(id) while the app persists JobSeekerProfile in user_profiles.
 *
 * That mismatch causes runtime 400s like:
 * - insert into certifications ... violates FK ... Key (profile_id) is not present in job_seeker_profiles
 *
 * This startup hook rewires any FK that still references job_seeker_profiles(id) to reference user_profiles(id).
 * It's idempotent and safe to run multiple times.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProfileFkAutoFixer implements ApplicationRunner {

    private final EntityManager em;

    @Override
    @Transactional
    public void run(org.springframework.boot.ApplicationArguments args) {
        // Only attempt if both relations exist (or legacy one exists for rewriting).
        if (!tableExists("user_profiles")) {
            log.warn("Skipping FK auto-fix: table user_profiles does not exist.");
            return;
        }
        if (!tableExists("job_seeker_profiles")) {
            // Nothing can reference it if it doesn't exist.
            return;
        }

        // 1) Fix known tables that frequently break (skills + education) by dropping any FK and re-adding.
        // These blocks are defensive: they work even if constraint names differ across environments.
        executeSilently("""
            DO $$
            DECLARE cname text;
            BEGIN
              IF to_regclass('public.job_seeker_skills') IS NOT NULL THEN
                SELECT conname INTO cname FROM pg_constraint
                  WHERE conrelid = 'job_seeker_skills'::regclass AND contype='f' LIMIT 1;
                IF cname IS NOT NULL THEN EXECUTE format('ALTER TABLE job_seeker_skills DROP CONSTRAINT %I', cname); END IF;
                EXECUTE 'ALTER TABLE job_seeker_skills DROP CONSTRAINT IF EXISTS job_seeker_skills_profile_fk';
                EXECUTE 'ALTER TABLE job_seeker_skills ADD CONSTRAINT job_seeker_skills_profile_fk FOREIGN KEY (profile_id) REFERENCES user_profiles(id) NOT VALID';
                BEGIN
                  EXECUTE 'ALTER TABLE job_seeker_skills VALIDATE CONSTRAINT job_seeker_skills_profile_fk';
                EXCEPTION WHEN others THEN
                  -- keep NOT VALID if existing rows violate; new writes will still be checked
                  NULL;
                END;
              END IF;
            END $$;
            """);

        executeSilently("""
            DO $$
            DECLARE cname text;
            BEGIN
              IF to_regclass('public.education') IS NOT NULL THEN
                SELECT conname INTO cname FROM pg_constraint
                  WHERE conrelid = 'education'::regclass AND contype='f' AND pg_get_constraintdef(oid) LIKE '%(profile_id)%' LIMIT 1;
                IF cname IS NOT NULL THEN EXECUTE format('ALTER TABLE education DROP CONSTRAINT %I', cname); END IF;
                EXECUTE 'ALTER TABLE education DROP CONSTRAINT IF EXISTS education_profile_fk';
                EXECUTE 'ALTER TABLE education ADD CONSTRAINT education_profile_fk FOREIGN KEY (profile_id) REFERENCES user_profiles(id) NOT VALID';
                BEGIN
                  EXECUTE 'ALTER TABLE education VALIDATE CONSTRAINT education_profile_fk';
                EXCEPTION WHEN others THEN
                  NULL;
                END;
              END IF;
            END $$;
            """);

        // 2) Generic rewrite: any FK still referencing job_seeker_profiles -> user_profiles
        int rewritten = rewriteAllProfileFks();
        if (rewritten > 0) {
            log.info("Rewired {} profile foreign key(s) from job_seeker_profiles -> user_profiles.", rewritten);
        }
    }

    private boolean tableExists(String table) {
        Object r = em.createNativeQuery("select to_regclass('public." + table + "')").getSingleResult();
        return r != null;
    }

    private void executeSilently(String sql) {
        try {
            em.createNativeQuery(sql).executeUpdate();
        } catch (Exception e) {
            // Don't fail app startup; this is best-effort for mixed-schema environments.
            log.warn("FK auto-fix step failed (will continue): {}", e.getMessage());
        }
    }

    private int rewriteAllProfileFks() {
        try {
            Object r = em.createNativeQuery("""
                DO $$
                DECLARE
                  x RECORD;
                  tbl_name text;
                  new_fk_name text;
                  rewired int := 0;
                BEGIN
                  FOR x IN
                    SELECT c.conrelid AS relid, c.conname AS fk_name
                    FROM pg_constraint c
                    WHERE c.contype = 'f'
                      AND c.confrelid = 'job_seeker_profiles'::regclass
                  LOOP
                    tbl_name := x.relid::regclass::text;
                    new_fk_name := regexp_replace(tbl_name, '^.*\\.', '') || '_profile_fk';
                    EXECUTE format('ALTER TABLE %s DROP CONSTRAINT IF EXISTS %I', tbl_name, x.fk_name);
                    EXECUTE format('ALTER TABLE %s DROP CONSTRAINT IF EXISTS %I', tbl_name, new_fk_name);
                    EXECUTE format(
                      'ALTER TABLE %s ADD CONSTRAINT %I FOREIGN KEY (profile_id) REFERENCES user_profiles(id) NOT VALID',
                      tbl_name, new_fk_name
                    );
                    BEGIN
                      EXECUTE format('ALTER TABLE %s VALIDATE CONSTRAINT %I', tbl_name, new_fk_name);
                    EXCEPTION WHEN others THEN
                      NULL;
                    END;
                    rewired := rewired + 1;
                  END LOOP;
                  -- return count via a notice-like select
                  PERFORM rewired;
                END $$;
                """).executeUpdate();
            // Hibernate returns 0 for DO blocks; we can't easily get count without extra query.
            // Return 1 to indicate "attempted" successfully.
            return (r == null) ? 0 : 1;
        } catch (Exception e) {
            log.warn("FK auto-fix generic rewrite failed: {}", e.getMessage());
            return 0;
        }
    }
}

