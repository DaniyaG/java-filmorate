package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.film.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.film.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.storage.film.mapper.MpaRowMapper;

import java.sql.PreparedStatement;

import java.sql.Statement;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@Slf4j
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper filmMapper;
    private final MpaRowMapper mpaRatingMapper;
    private final GenreRowMapper genreMapper;

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.filmMapper = new FilmRowMapper();
        this.mpaRatingMapper = new MpaRowMapper();
        this.genreMapper = new GenreRowMapper();
    }

    private static final LocalDate EARLIEST_RELEASE_DATE = LocalDate.of(1895, 12, 28);
    private static final String ERROR_INVALID_RELEASE_DATE = "Дата релиза не может быть раньше 28.12.1895";

    @Override
    public Film addFilm(Film film) {

        validateFilm(film);

        if (film.getMpa() != null && film.getMpa().getId() != null) {
            String sql = "SELECT COUNT(*) FROM mpa_ratings WHERE id = ?";
            Integer mpaCount = jdbcTemplate.queryForObject(sql, Integer.class, film.getMpa().getId());

            if (mpaCount == 0) {
                throw new NotFoundException("Рейтинг MPA с id " + film.getMpa().getId() + " не найден");
            }
        } else {
            throw new ValidationException("Требуется рейтинг MPA");
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Integer> genreIds = film.getGenres().stream()
                    .map(Genre::getId)
                    .collect(Collectors.toSet());

            String sql = "SELECT COUNT(*) FROM genres WHERE id IN (" +
                    genreIds.stream().map(String::valueOf).collect(Collectors.joining(",")) + ")";
            Integer genresCount = jdbcTemplate.queryForObject(sql, Integer.class);

            if (genresCount == null || genresCount != genreIds.size()) {
                throw new NotFoundException("Жанры не найдены");
            }
        }

        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_rating_id) "
                + "VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setInt(5, film.getMpa() != null ? film.getMpa().getId() : null);
            return ps;
        }, keyHolder);

        Long newId = keyHolder.getKey().longValue();
        film.setId(newId);

        saveFilmGenres(film);

        log.info("Добавлен фильм с id: {}", film.getId());
        return getFilmById(film.getId());
    }

   @Override
    public Film updateFilm(Film film) {

        if (film.getId() == null || !deleteFilm(film.getId())) {
           throw new NotFoundException("Фильм не найден");
        }

        validateFilm(film);

        String sql = """
            UPDATE films
            SET name = ?, description = ?, release_date = ?,
                duration = ?, mpa_rating_id = ?
            WHERE id = ?
            """;

       jdbcTemplate.update(sql,
               film.getName(),
               film.getDescription(),
               film.getReleaseDate(),
               film.getDuration(),
               film.getMpa() != null ? film.getMpa().getId() : null,
               film.getId()
       );

       jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
       saveFilmGenres(film);

       log.info("Фильм с id {} обновлен", film.getId());
       return getFilmById(film.getId());
    }

    @Override
    public boolean deleteFilm(Long id) {
        String sql = "SELECT COUNT(*) FROM films WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public Film getFilmById(Long id) {
        String sql = """
            SELECT f.*, m.name as mpa_name
            FROM films f
            LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id
            WHERE f.id = ?
            """;

        try {
            Film film = jdbcTemplate.queryForObject(sql, filmMapper, id);
            loadFilmData(film);
            return film;
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Фильм с id " + id + " не найден");
        }
    }

    @Override
    public Collection<Film> getAllFilms() {
        String sql = """
            SELECT f.*, m.name as mpa_name
            FROM films f
            LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id
            ORDER BY f.id
            """;

        List<Film> films = jdbcTemplate.query(sql, filmMapper);
        films.forEach(this::loadFilmData);
        return films;
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        String sql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
        try {
            jdbcTemplate.update(sql, filmId, userId);
        } catch (Exception e) {
            log.warn("Пользователь {} уже поставил лайк фильму {}", userId, filmId);
        }
    }

    @Override
    public void deleteLike(Long filmId, Long userId) {
        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        int deleted = jdbcTemplate.update(sql, filmId, userId);
        if (deleted == 0) {
            throw new NotFoundException("Лайк не найден у пользователя " + userId + " и фильма " + filmId);
        }
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        String sql = """
            SELECT f.*, m.name as mpa_name,
                   COUNT(l.user_id) as likes_count
            FROM films f
            LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id
            LEFT JOIN likes l ON f.id = l.film_id
            GROUP BY f.id, m.id
            ORDER BY likes_count DESC
            LIMIT ?
            """;
        List<Film> films = jdbcTemplate.query(sql, filmMapper, count);
        films.forEach(this::loadFilmData);
        return films;
    }

    public List<MpaRating> getAllMpaRatings() {
        String sql = "SELECT * FROM mpa_ratings ORDER BY id";
        return jdbcTemplate.query(sql, mpaRatingMapper);
    }

    public MpaRating getMpaRatingById(Integer id) {
        String sql = "SELECT * FROM mpa_ratings WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, mpaRatingMapper, id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Рейтинг MPA с id " + id + " не найден");
        }
    }

    public List<Genre> getAllGenres() {
        String sql = "SELECT * FROM genres ORDER BY id";
        return jdbcTemplate.query(sql, genreMapper);
    }

    public Genre getGenreById(Integer id) {
        String sql = "SELECT * FROM genres WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, genreMapper, id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Жанр с id " + id + " не найден");
        }
    }

    private void validateFilm(Film film) {
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(EARLIEST_RELEASE_DATE)) {
            log.warn("Некорректная дата релиза: {}", film.getReleaseDate());
            throw new ValidationException(ERROR_INVALID_RELEASE_DATE);
        }
    }

    private void saveFilmGenres(Film film) {

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            List<Object[]> batchArgs = film.getGenres().stream()
                    .map(genre -> new Object[]{film.getId(), genre.getId()})
                    .collect(Collectors.toList());
            jdbcTemplate.batchUpdate(
                    "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)",
                    batchArgs
            );
        }
    }

    private void loadFilmData(Film film) {
        if (film == null) return;

        String genresSql = """
        SELECT g.*
        FROM genres g
        JOIN film_genres fg ON g.id = fg.genre_id
        WHERE fg.film_id = ?
        ORDER BY g.id
        """;

        List<Genre> genres = jdbcTemplate.query(genresSql, genreMapper, film.getId());
        film.setGenres(new LinkedHashSet<>(genres));

        String sql = "SELECT user_id FROM likes WHERE film_id = ?";
        List<Long> likes = jdbcTemplate.queryForList(sql, Long.class, film.getId());
        film.setLikes(new HashSet<>(likes));
    }
}