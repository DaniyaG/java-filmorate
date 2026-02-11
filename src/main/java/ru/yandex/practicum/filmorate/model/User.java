package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

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

    Set<Long> friends = new HashSet<>();

    public String getName() {
        if (name == null || name.isBlank()) {
            return login;
        }
        return name;
    }

    public void addFriend(long friendId) {
        friends.add(friendId);
    }

    public void removeFriend(long friendId) {
        friends.remove(friendId);
    }
}
