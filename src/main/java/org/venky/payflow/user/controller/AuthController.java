package org.venky.payflow.user.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.venky.payflow.user.dto.LoginRequest;
import org.venky.payflow.user.dto.LoginResponse;
import org.venky.payflow.user.dto.SignupRequest;
import org.venky.payflow.user.dto.UserResponse;
import org.venky.payflow.user.service.AuthService;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public UserResponse signup(@Valid @RequestBody SignupRequest signupRequest) {
        return  authService.signup(signupRequest);
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody @Valid LoginRequest loginRequest){
        return authService.login(loginRequest);
    }
}
