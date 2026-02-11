package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private long currentId = 1;

    @Override
    public User addUser(User user) {
        long id = getNextId();
        user.setId(id);
        users.put(id, user);
        log.info("Добавлен пользователь с id={}: {}", id, user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        if (users.containsKey(user.getId())) {
            users.put(user.getId(), user);
            log.info("Обновлён пользователь с id={}: {}", user.getId(), user);
            return user;
        } else {
            log.warn("Пользователь с id={} не найден для обновления", user.getId());
            throw new NotFoundException(String.format("Пользователь с id=%d не найден", user.getId()));
        }
    }

    @Override
    public void deleteUser(long id) {
        if (users.containsKey(id)) {
            users.remove(id);
            log.info("Удален пользователь с id={}", id);
        } else {
            throw new NotFoundException(String.format("Пользователь с id=%d не найден", id));
        }
    }

    @Override
    public User getUserById(long id) {
        User user = users.get(id);
        if (user == null) {
            throw new NotFoundException(String.format("Пользователь с id=%d не найден", id));
        }
        return user;
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    private long getNextId() {
        return currentId++;
    }

}
