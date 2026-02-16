package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.storage.user.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.PreparedStatement;
import java.sql.Date;
import java.sql.Statement;
import java.util.*;

@Repository
@Slf4j
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper userMapper;

    @Autowired
    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.userMapper = new UserRowMapper();
    }

    @Override
    public User addUser(User user) {
        String sql = "INSERT INTO users (login, email, name, birthday) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, user.getLogin());
                ps.setString(2, user.getEmail());
                ps.setString(3, user.getName());
                ps.setDate(4, Date.valueOf(user.getBirthday()));
                return ps;
            }, keyHolder);

            Number generatedId = keyHolder.getKey();
            user.setId(generatedId.longValue());
            log.info("Добавлен пользователь с id: {}", user.getId());
            return getUserById(user.getId());
        } catch (Exception e) {
            log.error("Ошибка добавления пользователя: {}", e.getMessage(), e);
            throw e;
        }

    }

    @Override
    public boolean deleteUser(Long id) {
        String sql = "SELECT COUNT(*) FROM users WHERE id = ?";
        try {
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
            return count != null && count > 0;
        } catch (Exception e) {
            log.error("Ошибка при проверке существования пользователя с id  {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public User getUserById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";

        try {
            User user = jdbcTemplate.queryForObject(sql, userMapper, id);
            loadUserFriends(user);
            return user;
        } catch (EmptyResultDataAccessException e) {
            log.warn("Пользователь с id {} не найден ", id);
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        } catch (Exception e) {
            log.error("Ошибка получения пользователя по id {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Collection<User> getAllUsers() {
        String sql = "SELECT * FROM users ORDER BY id";
        List<User> users = jdbcTemplate.query(sql, userMapper);
        users.forEach(this::loadUserFriends);
        return users;
    }

    @Override
    public User updateUser(User user) {

        if (user.getId() == null || !deleteUser(user.getId())) {
            log.warn("Пользователь с id={} не найден для обновления", user.getId());
            throw new NotFoundException(String.format("Пользователь с id=%d не найден", user.getId()));
        }

        String sql = """
                UPDATE users
                SET login = ?, email = ?, name = ?, birthday = ?
                WHERE id = ?
                """;

        try {
            jdbcTemplate.update(sql,
                    user.getLogin(),
                    user.getEmail(),
                    user.getName(),
                    user.getBirthday(),
                    user.getId()
            );

            log.info("Обновлён пользователь с id={}:", user.getId());
            return getUserById(user.getId());
        } catch (Exception e) {
            log.error("Ошибка обновления пользователя с id {}: {}", user.getId(), e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void deleteFriend(Long userId, Long friendId) {
        String sql = "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?";
        try {
            jdbcTemplate.update(sql, userId, friendId);
            log.info("Удален друг {} у пользователя {}", friendId, userId);
        } catch (Exception e) {
            log.error("Error removing friend {} from user {}: {}", friendId, userId, e.getMessage(), e);
            throw e;
        }
    }

    private void loadUserFriends(User user) {
        if (user == null) {
            log.warn("Пользователь не найден");
            return;
        }

        String sql = """
                SELECT friend_id
                FROM friendships
                WHERE user_id = ?
                """;

        try {
            jdbcTemplate.query(sql, rs -> {
                Long friendId = rs.getLong("friend_id");
                String status = rs.getString("status");
                user.getFriends().put(friendId, FriendshipStatus.valueOf(status));
            }, user.getId());
            log.debug("Загружены {} друзья для пользователя с id: {}", user.getFriends().size(), user.getId());
        } catch (Exception e) {
            log.error("Ошибка добавления друзей для пользователя с id {}: {}", user.getId(), e.getMessage(), e);
        }
    }

    @Override
    public void addFriend(Long userId, Long friendId, FriendshipStatus status) {
        String sql = "INSERT INTO friendships (user_id, friend_id, status) VALUES (?, ?, ?)";
        try {
            jdbcTemplate.update(sql, userId, friendId, status.toString());

            if (status == FriendshipStatus.CONFIRMED) {
                jdbcTemplate.update(sql, friendId, userId, status.toString());
            }
            log.info("Друг добавлен {} пользователю {} со статусом {}", friendId, userId, status);
        } catch (Exception e) {
            sql = "UPDATE friendships SET status = ? WHERE user_id = ? AND friend_id = ?";
            jdbcTemplate.update(sql, status.toString(), userId, friendId);
        }
    }


    @Override
    public List<User> getFriends(Long userId) {
        String sql = """
                SELECT u.*
                FROM users u
                JOIN friendships f ON u.id = f.friend_id
                WHERE f.user_id = ?
                ORDER BY u.id
                """;

        try {
            List<User> friends = jdbcTemplate.query(sql, userMapper, userId);
            return friends;
        } catch (Exception e) {
            log.error("Ошибка получения друзей для пользователя id {}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public List<User> getCommonFriends(Long userId, Long otherId) {
        String sql = """
            SELECT u.*
            FROM users u
            JOIN friendships f1 ON u.id = f1.friend_id
            JOIN friendships f2 ON u.id = f2.friend_id
            WHERE f1.user_id = ? AND f2.user_id = ?
            ORDER BY u.id
            """;

        try {
            List<User> commonFriends = jdbcTemplate.query(sql, userMapper, userId, otherId);
            return commonFriends;
        } catch (Exception e) {
            log.error("Ошибка получения общих друзей для пользователей {} и {}: {}", userId, otherId, e.getMessage(), e);
            throw e;
        }
    }
}

