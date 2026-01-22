package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Integer,Film> films = new HashMap<>();
    private int currentId = 1;
    private static final LocalDate EARLIEST_RELEASE_DATE = LocalDate.of(1895, 12, 28);
    private static final String ERROR_INVALID_RELEASE_DATE = "Дата релиза не может быть раньше 28.12.1895";

    @PostMapping
    public Film create(@RequestBody @Valid Film film) {
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(EARLIEST_RELEASE_DATE)) {
            log.warn("Некорректная дата релиза: {}", film.getReleaseDate());
            throw new ValidationException(ERROR_INVALID_RELEASE_DATE);
        }
        int id = getNextId();
        film.setId(id);
        films.put(id,film);
        log.info("Создан фильм с id = {}: {}", id, film);
        return film;
     }

     @GetMapping
     public List<Film> getAll() {
        log.info("Запрос всех фильмов");
        return new ArrayList<>(films.values());
     }

     @PutMapping
     public Film update(@RequestBody @Valid Film newFilm) {
         if (newFilm.getReleaseDate() != null && newFilm.getReleaseDate().isBefore(EARLIEST_RELEASE_DATE)) {
             log.warn("Некорректная дата релиза при обновлении: {}", newFilm.getReleaseDate());
             throw new ValidationException(ERROR_INVALID_RELEASE_DATE);
         }
         if (films.containsKey(newFilm.getId())) {
             films.put(newFilm.getId(),newFilm);
             log.info("Обновлён фильм с id={}: {}", newFilm.getId(), newFilm);
             return newFilm;
         } else {
             log.warn("Фильм с id={} не найден для обновления", newFilm.getId());
             throw new ValidationException("Фильм с id=" + newFilm.getId() + " не найден");
         }
     }

     private int getNextId() {
        return currentId++;
     }

}
