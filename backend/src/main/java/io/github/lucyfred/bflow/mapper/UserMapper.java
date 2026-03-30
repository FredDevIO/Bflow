package io.github.lucyfred.bflow.mapper;

import io.github.lucyfred.bflow.dto.AdminUserResponseDto;
import io.github.lucyfred.bflow.dto.UserRequestDto;
import io.github.lucyfred.bflow.dto.UserResponseDto;
import io.github.lucyfred.bflow.dto.UserUpdateResponse;
import io.github.lucyfred.bflow.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponseDto toUserResponseDto(User user);
    AdminUserResponseDto toAdminUserResponseDto(User user);
    UserUpdateResponse toUserUpdateResponse(User user, String token);
    User toUserFromResponse(UserResponseDto userResponseDto);
    UserResponseDto toUserDtoFromRequest(UserRequestDto userRequestDto);
    User toUserFromRequest(UserRequestDto userRequestDto);

}
