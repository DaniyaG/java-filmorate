package ru.yandex.practicum.filmorate.service.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public void addLike(long filmId, long userId) {
        Film film = filmStorage.getFilmById(filmId);
        userStorage.getUserById(userId);
        film.addLike(userId);
        filmStorage.updateFilm(film);
        log.info("Пользователь с id {} поставил лайк фильму с id {}", userId, filmId);
    }

    public void deleteLike(long filmId, long userId) {
        Film film = filmStorage.getFilmById(filmId);
        userStorage.getUserById(userId);
        if (film.getLikes().contains(userId)) {
            film.removeLike(userId);
            filmStorage.updateFilm(film);
            log.info("Пользователь с id {} удалил лайк у фильма с id {}", userId, filmId);
        } else {
            log.warn("У фильма с id {} нет лайка от пользователя с id {}", filmId, userId);
            throw new NotFoundException(String.format("У фильма с id=%d нет лайка от пользователя с id=%d", filmId, userId));
        }

    }

    public List<Film> getPopularFilms(int count) {
        log.info("Запрос на получение {} популярных фильмов", count);
        return filmStorage.getAllFilms().stream()
                .sorted(Comparator.comparingInt(Film::getLikesCount).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }

    public Film createFilm(Film film) {
        log.info("Создание фильма: {}", film);
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        log.info("Обновление фильма: {}", film);
        return filmStorage.updateFilm(film);
    }

    public List<Film> getAllFilms() {
        log.info("Получение всех фильмов");
        return filmStorage.getAllFilms();
    }

    public Film getFilmById(int id) {
        log.info("Получение фильма по id: {}", id);
        return filmStorage.getFilmById(id);
    }
}