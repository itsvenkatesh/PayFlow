package org.venky.payflow.user.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.venky.payflow.user.dto.CreateUserRequest;
import org.venky.payflow.user.dto.UserResponse;
import org.venky.payflow.user.service.UserService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public UserResponse createUser(@RequestBody @Valid CreateUserRequest createUserRequest){
        return userService.createUser(createUserRequest);
    }

    @GetMapping
    public List<UserResponse> findAllUsers(){
        return userService.findAllUsers();
    }

    @GetMapping("/{userId}")
    public UserResponse findUserById(@PathVariable UUID userId){
        return userService.findUserById(userId);
    }

}
