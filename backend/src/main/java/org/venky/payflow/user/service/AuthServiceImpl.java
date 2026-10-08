package org.venky.payflow.user.service;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.venky.payflow.common.exception.DuplicateResourceException;
import org.venky.payflow.common.exception.ResourceNotFoundException;
import org.venky.payflow.user.dto.*;
import org.venky.payflow.user.entity.User;
import org.venky.payflow.user.enums.Role;
import org.venky.payflow.user.mapper.UserMapper;
import org.venky.payflow.user.repository.UserRepository;

import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final JwtService jwtService;

    public AuthServiceImpl(UserRepository userRepository,  PasswordEncoder passwordEncoder,  UserMapper userMapper,  JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.jwtService = jwtService;
    }

    @Override
    public UserResponse signup(SignupRequest signupRequest) {
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }

        User user = userMapper.toEntity(signupRequest);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setRole(Role.CUSTOMER);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User savedUser = userRepository.save(user);

        return userMapper.toResponse(savedUser);
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {

        User savedUser = userRepository.findByEmail(loginRequest.getEmail());
        System.out.println(savedUser);
        if(savedUser == null){
            throw new BadCredentialsException("Invalid email");
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(),savedUser.getPassword())) {
            throw new BadCredentialsException("Invalid Email or Password");
        }

        String token = jwtService.generateToken(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getRole().name()
        );

        return  new LoginResponse(token);
    }
}
