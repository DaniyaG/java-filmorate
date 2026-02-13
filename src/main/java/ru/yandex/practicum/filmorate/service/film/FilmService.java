package ru.yandex.practicum.filmorate.service.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public void addLike(Long filmId, Long userId) {
        filmStorage.getFilmById(filmId);
        userStorage.getUserById(userId);

        filmStorage.addLike(filmId, userId);

        log.info("Пользователь с id {} поставил лайк фильму с id {}", userId, filmId);
    }

    public void deleteLike(Long filmId, Long userId) {
        filmStorage.getFilmById(filmId);
        userStorage.getUserById(userId);

        filmStorage.deleteLike(filmId, userId);

        log.info("Пользователь с id {} удалил лайк у фильма с id {}", userId, filmId);

    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.getPopularFilms(count);
    }

    public Film createFilm(Film film) {
        log.info("Создание фильма: {}", film);
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        log.info("Обновление фильма: {}", film);
        return filmStorage.updateFilm(film);
    }

    public Collection<Film> getAllFilms() {
        log.info("Получение всех фильмов");
        return filmStorage.getAllFilms();
    }

    public Film getFilmById(Long id) {
        log.info("Получение фильма по id: {}", id);
        return filmStorage.getFilmById(id);
    }
}