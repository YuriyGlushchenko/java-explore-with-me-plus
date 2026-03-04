package ru.practicum.ewm.user.dto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.user.model.User;

@Component
public class UserMapper {
    public User toUser(NewUserRequest request) {
        return User.builder()
                .email(request.getEmail())
                .name(request.getName())
                .build();
    }

    public UserDto toUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }
}
