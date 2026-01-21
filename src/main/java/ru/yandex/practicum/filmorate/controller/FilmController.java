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

    @PostMapping
    public Film create(@RequestBody @Valid Film film) {
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.warn("Некорректная дата релиза: {}", film.getReleaseDate());
            throw new ValidationException("Дата релиза не может быть раньше 28.12.1895");
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
         if (newFilm.getReleaseDate() != null && newFilm.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
             log.warn("Некорректная дата релиза при обновлении: {}", newFilm.getReleaseDate());
             throw new ValidationException("Дата релиза не может быть раньше 28.12.1895");
         }
         if (films.containsKey(newFilm.getId())) {
             films.put(newFilm.getId(),newFilm);
             log.info("Обновлён фильм с id={}: {}", newFilm.getId(), newFilm);
             return newFilm;
         } else {
             log.warn("Фильм с id={} не найден для обновления", newFilm.getId());
             throw new ValidationException("Фильм не найден");
         }
     }

     private int getNextId() {
        return currentId++;
     }

}
