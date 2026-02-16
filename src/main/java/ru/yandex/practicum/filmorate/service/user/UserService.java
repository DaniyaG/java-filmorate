package ru.yandex.practicum.filmorate.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;

    public User createUser(User user) {
        log.info("Создание пользователя: {}", user);
        return userStorage.addUser(user);
    }

    public User updateUser(User user) {
        log.info("Обновление пользователя: {}", user);
        getUserById(user.getId());
        return userStorage.updateUser(user);
    }

    public Collection<User> getAllUsers() {
        log.info("Получение всех пользователей");
        return userStorage.getAllUsers();
    }

    public User getUserById(long id) {
        log.info("Получение пользователя по id: {}", id);
        return userStorage.getUserById(id);
    }

    public void addFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new ValidationException("Пользователь не может добавить самого себя в друзья");
        }

        User user = getUserById(userId);
        User friend = getUserById(friendId);

        if (friend.getFriends().containsKey(userId)) {
            userStorage.addFriend(userId, friendId, FriendshipStatus.CONFIRMED);
            userStorage.addFriend(friendId, userId, FriendshipStatus.CONFIRMED);
                log.info("Дружба между {} и {} подтверждена", userId, friendId);
            } else {
                userStorage.addFriend(userId, friendId, FriendshipStatus.PENDING);
                log.info("Пользователь {} отправил запрос на добавление в друзья  {}", userId, friendId);
            }

        log.info("Пользователь {} добавил в друзья {}", userId, friendId);
    }

    public void deleteFriend(Long userId, Long friendId) {
        getUserById(userId);
        getUserById(friendId);

        userStorage.deleteFriend(userId, friendId);

        log.info("Пользователь {} удалил друга {}", userId, friendId);
    }

    public List<User> getFriends(Long userId) {
        return userStorage.getFriends(userId);
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        return userStorage.getCommonFriends(userId, otherId);
    }
}
