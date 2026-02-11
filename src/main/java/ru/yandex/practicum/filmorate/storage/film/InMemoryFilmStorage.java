package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.*;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();
    private long currentId = 1;

    private static final LocalDate EARLIEST_RELEASE_DATE = LocalDate.of(1895, 12, 28);
    private static final String ERROR_INVALID_RELEASE_DATE = "Дата релиза не может быть раньше 28.12.1895";

    @Override
    public Film addFilm(Film film) {
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(EARLIEST_RELEASE_DATE)) {
            log.warn("Некорректная дата релиза: {}", film.getReleaseDate());
            throw new ValidationException(ERROR_INVALID_RELEASE_DATE);
        }
        long id = getNextId();
        film.setId(id);
        films.put(id, film);
        log.info("Добавлен фильм с id = {}: {}", id, film);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(EARLIEST_RELEASE_DATE)) {
            log.warn("Некорректная дата релиза при обновлении: {}", film.getReleaseDate());
            throw new ValidationException(ERROR_INVALID_RELEASE_DATE);
        }
        if (films.containsKey(film.getId())) {
            films.put(film.getId(), film);
            log.info("Обновлён фильм с id={}: {}", film.getId(), film);
            return film;
        } else {
            log.warn("Фильм с id={} не найден для обновления", film.getId());
            throw new NotFoundException(String.format("Фильм с id=%d не найден", film.getId()));
        }
    }

    @Override
    public void deleteFilm(long id) {
        if (films.containsKey(id)) {
            films.remove(id);
            log.info("Удален фильм с id={}", id);
        } else {
            log.warn("Фильм с id={} не найден для удаления", id);
            throw new NotFoundException(String.format("Фильм с id=%d не найден", id));
        }
    }

    @Override
    public Film getFilmById(long id) {
        Film film = films.get(id);
        if (film == null) {
            throw new NotFoundException(String.format("Фильм с id=%d не найден", id));
        }
        return film;
    }

    @Override
    public List<Film> getAllFilms() {
        return new ArrayList<>(films.values());
    }

    private long getNextId() {
        return currentId++;
    }

}