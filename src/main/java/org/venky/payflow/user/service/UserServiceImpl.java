package org.venky.payflow.user.service;

import org.springframework.stereotype.Service;
import org.venky.payflow.common.exception.DuplicateResourceException;
import org.venky.payflow.common.exception.ResourceNotFoundException;
import org.venky.payflow.user.dto.CreateUserRequest;
import org.venky.payflow.user.dto.UserResponse;
import org.venky.payflow.user.entity.User;
import org.venky.payflow.user.mapper.UserMapper;
import org.venky.payflow.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository,  UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    public UserResponse createUser(CreateUserRequest createUserRequest) {
        if (userRepository.existsByEmail(createUserRequest.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }

        User user = userMapper.toEntity(createUserRequest);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        return userMapper.toResponse(savedUser);
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
