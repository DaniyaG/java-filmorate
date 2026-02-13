package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
public class User {
    Long id;

    @NotBlank(message = "Логин не может быть пустым")
    @Pattern(regexp = "\\S+", message = "Логин не должен содержать пробелов")
    String login;

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Некорректный формат email")
    String email;

    String name;

    @NotNull
    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    LocalDate birthday;

    private Map<Long, FriendshipStatus> friends = new HashMap<>();

    public String getName() {
        if (name == null || name.isBlank()) {
            return login;
        }
        return name;
    }

    public FriendshipStatus getFriendshipStatus(Long id) {
        return friends.get(id);
    }


}
