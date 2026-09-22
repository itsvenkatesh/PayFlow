package org.venky.payflow.user.security;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.UUID;

public record AuthenticatedUser(UUID userId, String email, Collection<? extends GrantedAuthority> authorities) {

}