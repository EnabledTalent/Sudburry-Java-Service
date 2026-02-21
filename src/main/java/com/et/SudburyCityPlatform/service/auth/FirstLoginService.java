package com.et.SudburyCityPlatform.service.auth;

import com.et.SudburyCityPlatform.models.auth.UserLoginState;
import com.et.SudburyCityPlatform.repository.auth.UserLoginStateRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FirstLoginService {

    private final UserLoginStateRepository repo;

    public FirstLoginService(UserLoginStateRepository repo) {
        this.repo = repo;
    }

    /**
     * Returns true exactly once per email (best-effort under concurrency).
     * Implementation flips hasLoggedInBefore=false -> true.
     */
    @Transactional
    public boolean consumeFirstTimeLogin(String email) {
        if (email == null || email.isBlank()) return false;
        String key = email.trim().toLowerCase();

        var existing = repo.findByEmail(key);
        if (existing.isPresent()) {
            UserLoginState s = existing.get();
            if (!s.isHasLoggedInBefore()) {
                s.setHasLoggedInBefore(true);
                repo.save(s);
                return true;
            }
            return false;
        }

        // First time we've ever seen this user -> create record and return true.
        try {
            repo.save(new UserLoginState(null, key, true, null, null));
            return true;
        } catch (DataIntegrityViolationException e) {
            // Race: someone else inserted concurrently.
            var nowExisting = repo.findByEmail(key);
            if (nowExisting.isPresent() && !nowExisting.get().isHasLoggedInBefore()) {
                UserLoginState s = nowExisting.get();
                s.setHasLoggedInBefore(true);
                repo.save(s);
                return true;
            }
            return false;
        }
    }
}

