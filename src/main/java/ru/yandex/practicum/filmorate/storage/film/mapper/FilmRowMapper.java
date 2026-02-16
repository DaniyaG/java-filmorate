package ru.yandex.practicum.filmorate.storage.film.mapper;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;


public class FilmRowMapper implements RowMapper<Film> {
    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getLong("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));

        Date birthday = rs.getDate("release_date");
        film.setReleaseDate(birthday.toLocalDate());
        film.setDuration(rs.getInt("duration"));

        if (rs.getInt("mpa_rating_id") > 0) {
            MpaRating mpaRating = new MpaRating();
            mpaRating.setId(rs.getInt("mpa_rating_id"));
            mpaRating.setName(rs.getString("mpa_name"));
            film.setMpa(mpaRating);
        }
        return film;
    }
}