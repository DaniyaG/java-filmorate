package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class MpaController {
    private final FilmDbStorage filmDbStorage;

    @GetMapping
    public List<MpaRating> getAllMpa() {
        log.info("Получен запрос на получение всех MPA рейтингов");
        return filmDbStorage.getAllMpaRatings();
    }

    @GetMapping("/{id}")
    public MpaRating getMpaById(@PathVariable Integer id) {
        log.info("Получен запрос на получение MPA рейтинга с id: {}", id);
        return filmDbStorage.getMpaRatingById(id);
    }
}
