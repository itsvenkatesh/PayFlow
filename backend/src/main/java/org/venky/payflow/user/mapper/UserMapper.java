package org.venky.payflow.user.mapper;

import org.mapstruct.Mapper;
import org.venky.payflow.user.dto.SignupRequest;
import org.venky.payflow.user.dto.UserResponse;
import org.venky.payflow.user.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toEntity(SignupRequest signupRequest);

    UserResponse toResponse(User user);
}
