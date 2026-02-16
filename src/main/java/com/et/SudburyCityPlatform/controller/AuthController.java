package com.et.SudburyCityPlatform.controller;

import com.et.SudburyCityPlatform.models.jobs.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @GetMapping("/me")
    public Map<String, Object> me(Authentication auth) {
        Map<String, Object> out = new HashMap<>();
        out.put("authenticated", auth != null && auth.isAuthenticated());
        out.put("name", auth != null ? auth.getName() : null);
        out.put("authorities", auth != null
                ? auth.getAuthorities().stream().map(a -> a.getAuthority()).collect(Collectors.toList())
                : null);

        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails principal) {
            out.put("principalRole", principal.getRole());
            out.put("principalUsername", principal.getUsername());
            out.put("principalUserId", principal.getUserId());
            out.put("principalEmployerId", principal.getEmployerId());
        } else {
            out.put("principalType", auth != null && auth.getPrincipal() != null ? auth.getPrincipal().getClass().getName() : null);
        }

        return out;
    }
}

