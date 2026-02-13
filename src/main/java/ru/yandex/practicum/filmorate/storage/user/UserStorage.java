package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;

public interface UserStorage {
    User addUser(User user);

    User updateUser(User user);

    boolean deleteUser(Long id);

    User getUserById(Long id);

    Collection<User> getAllUsers();

    List<User> getFriends(Long userId);

    List<User> getCommonFriends(Long userId1, Long otherId);

    void addFriend(Long userId, Long friendId, FriendshipStatus status);

    void deleteFriend(Long userId, Long friendId);
}