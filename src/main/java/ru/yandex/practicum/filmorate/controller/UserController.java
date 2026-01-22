package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Integer, User> users = new HashMap<>();
    private int currentId = 1;

    @PostMapping
    public User create(@RequestBody @Valid User user) {
        int id = getNextId();
        user.setId(id);
        users.put(id,user);
        log.info("Создан пользователь с id={}: {}", user.getId(), user);
        return user;
    }

    @GetMapping
    public ArrayList<User> getAll() {
        log.info("Запрос всех пользователей");
        return new ArrayList<>(users.values());
    }

    @PutMapping
    public User update(@RequestBody @Valid User newUser) {
        if (users.containsKey(newUser.getId())) {
            users.put(newUser.getId(),newUser);
            log.info("Обновлён пользователь с id={}: {}", newUser.getId(), newUser);
            return newUser;
        } else {
            log.warn("Пользователь с id={} не найден для обновления", newUser.getId());
            throw new ValidationException("Пользователь с id=" + newUser.getId() + " не найден");
        }
    }

    private int getNextId() {
        return currentId++;
    }
}
