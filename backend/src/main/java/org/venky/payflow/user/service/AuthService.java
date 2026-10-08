package org.venky.payflow.user.service;


import org.venky.payflow.user.dto.LoginRequest;
import org.venky.payflow.user.dto.LoginResponse;
import org.venky.payflow.user.dto.SignupRequest;
import org.venky.payflow.user.dto.UserResponse;

public interface AuthService {

    public UserResponse signup(SignupRequest signupRequest);

    public LoginResponse login(LoginRequest loginRequest);
}
