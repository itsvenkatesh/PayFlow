package org.venky.payflow.user.service;

import org.venky.payflow.user.dto.UserResponse;

import java.util.List;
import java.util.UUID;

public interface UserService {

    List<UserResponse> findAllUsers();

    UserResponse findUserById(UUID id);

}
