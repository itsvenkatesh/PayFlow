package org.venky.payflow.common.security;

import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.venky.payflow.user.security.AuthenticatedUser;

import java.util.UUID;

@Service
public class CurrentUserService {
    public UUID extractUserIdFromAuthentication() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !(authentication.getPrincipal() instanceof AuthenticatedUser authenticatedUser)) {
            throw new InsufficientAuthenticationException("User is not authenticated");
        }

        return authenticatedUser.userId();
    }
}
