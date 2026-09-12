package org.venky.payflow.user.service;

import org.venky.payflow.user.dto.CreateUserRequest;
import org.venky.payflow.user.dto.UserResponse;

import java.util.List;
import java.util.UUID;

public interface UserService {
    UserResponse createUser(CreateUserRequest createUserRequest);

    List<UserResponse> findAllUsers();

    UserResponse findUserById(UUID id);


}
