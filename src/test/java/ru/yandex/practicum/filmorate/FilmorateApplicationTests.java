package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;


@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, UserDbStorage.class, })
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmorateApplicationTests {

    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;

    @Test
    void testCreateAndGetFilm() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2023, 1, 1));
        film.setDuration(120);

        MpaRating mpa = new MpaRating();
        mpa.setId(1);
        film.setMpa(mpa);

        Film savedFilm = filmStorage.addFilm(film);
        Film foundFilm = filmStorage.getFilmById(savedFilm.getId());

        Assertions.assertThat(foundFilm).isNotNull();
        Assertions.assertThat(foundFilm.getName()).isEqualTo("Test Film");
    }

    @Test
    void testGetAllFilms() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2023, 1, 1));
        film.setDuration(120);

        MpaRating mpa = new MpaRating();
        mpa.setId(1);
        film.setMpa(mpa);
        filmStorage.addFilm(film);

        Collection<Film> films = filmStorage.getAllFilms();
        Assertions.assertThat(films).isNotEmpty();
    }

    @Test
    void testCreateAndUpdateFilm() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2024, 1, 1));
        film.setDuration(120);

        MpaRating mpa = new MpaRating();
        mpa.setId(1);
        film.setMpa(mpa);

        Film savedFilm = filmStorage.addFilm(film);
        savedFilm.setName("Updated Film");

        Film updatedFilm = filmStorage.updateFilm(savedFilm);
        Assertions.assertThat(updatedFilm.getName()).isEqualTo("Updated Film");
    }

    @Test
    void testGetUserById() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1999, 11, 1));

        User savedUser = userStorage.addUser(user);
        User foundUser = userStorage.getUserById(savedUser.getId());

        Assertions.assertThat(foundUser).isNotNull();
        Assertions.assertThat(foundUser.getId()).isEqualTo(savedUser.getId());
        Assertions.assertThat(foundUser.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void testGetAllUsers() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        userStorage.addUser(user);

        Collection<User> users = userStorage.getAllUsers();
        Assertions.assertThat(users).isNotEmpty();
    }

    @Test
    void testCreateAndUpdateUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1999, 11, 1));

        User savedUser = userStorage.addUser(user);
        savedUser.setName("Updated Name");

        User updatedUser = userStorage.updateUser(savedUser);
        Assertions.assertThat(updatedUser.getName()).isEqualTo("Updated Name");
    }

    @Test
    void testAddAndGetFriends() {
        User user1 = new User();
        user1.setEmail("user1@test.com");
        user1.setLogin("user1");
        user1.setName("User One");
        user1.setBirthday(LocalDate.of(1990, 1, 1));
        User savedUser1 = userStorage.addUser(user1);

        User user2 = new User();
        user2.setEmail("user2@test.com");
        user2.setLogin("user2");
        user2.setName("User Two");
        user2.setBirthday(LocalDate.of(1992, 2, 2));
        User savedUser2 = userStorage.addUser(user2);

        userStorage.addFriend(savedUser1.getId(), savedUser2.getId(), FriendshipStatus.CONFIRMED);

        List<User> friends = userStorage.getFriends(savedUser1.getId());
        Assertions.assertThat(friends).hasSize(1);
        Assertions.assertThat(friends.get(0).getId()).isEqualTo(savedUser2.getId());
    }

    @Test
    void testDeleteFriend() {
        User user1 = new User();
        user1.setEmail("user1@test.com");
        user1.setLogin("user1");
        user1.setName("User One");
        user1.setBirthday(LocalDate.of(1990, 1, 1));
        User savedUser1 = userStorage.addUser(user1);

        User user2 = new User();
        user2.setEmail("user2@test.com");
        user2.setLogin("user2");
        user2.setName("User Two");
        user2.setBirthday(LocalDate.of(1992, 2, 2));
        User savedUser2 = userStorage.addUser(user2);

        userStorage.addFriend(savedUser1.getId(), savedUser2.getId(), FriendshipStatus.PENDING);
        userStorage.deleteFriend(savedUser1.getId(), savedUser2.getId());

        List<User> friends = userStorage.getFriends(savedUser1.getId());
        Assertions.assertThat(friends).isEmpty();
    }

    @Test
    void testGetCommonFriends() {
        User user1 = new User();
        user1.setEmail("user1@test.com");
        user1.setLogin("user1");
        user1.setName("User One");
        user1.setBirthday(LocalDate.of(1990, 1, 1));
        User savedUser1 = userStorage.addUser(user1);

        User user2 = new User();
        user2.setEmail("user2@test.com");
        user2.setLogin("user2");
        user2.setName("User Two");
        user2.setBirthday(LocalDate.of(1992, 2, 2));
        User savedUser2 = userStorage.addUser(user2);

        User user3 = new User();
        user3.setEmail("user3@test.com");
        user3.setLogin("user3");
        user3.setName("User Three");
        user3.setBirthday(LocalDate.of(1993, 3, 3));
        User savedUser3 = userStorage.addUser(user3);

        userStorage.addFriend(savedUser1.getId(), savedUser3.getId(), FriendshipStatus.CONFIRMED);
        userStorage.addFriend(savedUser2.getId(), savedUser3.getId(), FriendshipStatus.CONFIRMED);

        List<User> commonFriends = userStorage.getCommonFriends(savedUser1.getId(), savedUser2.getId());
        Assertions.assertThat(commonFriends).hasSize(1);
        Assertions.assertThat(commonFriends.get(0).getId()).isEqualTo(savedUser3.getId());
    }

    @Test
    void testAddAndDeleteLike() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User savedUser = userStorage.addUser(user);

        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2023, 1, 1));
        film.setDuration(120);

        MpaRating mpa = new MpaRating();
        mpa.setId(1);
        film.setMpa(mpa);
        Film savedFilm = filmStorage.addFilm(film);

        filmStorage.addLike(savedFilm.getId(), savedUser.getId());
        filmStorage.deleteLike(savedFilm.getId(), savedUser.getId());

        Film foundFilm = filmStorage.getFilmById(savedFilm.getId());
        Assertions.assertThat(foundFilm.getLikes()).isEmpty();
    }

    @Test
    void testGetPopularFilms() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User savedUser = userStorage.addUser(user);

        Film film1 = new Film();
        film1.setName("Film 1");
        film1.setDescription("Description 1");
        film1.setReleaseDate(LocalDate.of(2023, 1, 1));
        film1.setDuration(120);
        MpaRating mpa1 = new MpaRating();
        mpa1.setId(1);
        film1.setMpa(mpa1);
        Film savedFilm1 = filmStorage.addFilm(film1);

        Film film2 = new Film();
        film2.setName("Film 2");
        film2.setDescription("Description 2");
        film2.setReleaseDate(LocalDate.of(2023, 2, 2));
        film2.setDuration(90);
        MpaRating mpa2 = new MpaRating();
        mpa2.setId(2);
        film2.setMpa(mpa2);
        Film savedFilm2 = filmStorage.addFilm(film2);

        filmStorage.addLike(savedFilm1.getId(), savedUser.getId());

        List<Film> popularFilms = filmStorage.getPopularFilms(10);
        Assertions.assertThat(popularFilms).isNotEmpty();
        Assertions.assertThat(popularFilms.get(0).getId()).isEqualTo(savedFilm1.getId());
    }

}
