package org.venky.payflow.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.venky.payflow.common.exception.ResourceNotFoundException;
import org.venky.payflow.user.dto.UserResponse;
import org.venky.payflow.user.entity.User;
import org.venky.payflow.user.mapper.UserMapper;
import org.venky.payflow.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository,  UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    public List<UserResponse> findAllUsers() {
        List<User> users = userRepository.findAll();

        return users.stream()
                .map(userMapper::toResponse)
                .toList();
    }

    public UserResponse findUserById(UUID id) {
        Optional<User> user = userRepository.findById(id);

        if (user.isEmpty()){
            throw new ResourceNotFoundException("User not found with id " + id);
        }
        return userMapper.toResponse(user.get());
    }

}
