package ru.yandex.practicum.filmorate.storage.sql;

public final class SqlKeys {
    private SqlKeys() {
    }

    public static final class User {
        public static final String SQL_USER_SELECT_ALL = """
                SELECT u.id, u.email, u.login, u.name, u.birthday
                FROM users u
                ORDER BY u.id
                """;

        public static final String SQL_USER_SELECT_BY_ID = """
                SELECT u.id, u.email, u.login, u.name, u.birthday
                FROM users u
                WHERE u.id = ?
                """;

        public static final String SQL_USER_UPDATE = """
                UPDATE users
                SET email = ?, login = ?, name = ?, birthday = ?
                WHERE id = ?
                """;

        public static final String SQL_USER_DELETE = """
                DELETE FROM users WHERE id = ?
                """;

        public static final String SQL_USER_EXISTS = """
                SELECT EXISTS(SELECT 1 FROM users WHERE id = ?)
                """;
    }

    public static final class Film {
        public static final String SQL_FILM_SELECT_ALL = """
                SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa
                FROM films f
                ORDER BY f.id
                """;

        public static final String SQL_FILM_SELECT_BY_ID = """
                SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa
                FROM films f
                WHERE f.id = ?
                """;

        public static final String SQL_FILM_UPDATE = """
                UPDATE films
                SET name = ?, description = ?, release_date = ?, duration = ?, mpa = ?
                WHERE id = ?
                """;

        public static final String SQL_FILM_DELETE = """
                DELETE FROM films WHERE id = ?
                """;

        public static final String SQL_FILM_EXISTS = """
                SELECT EXISTS(SELECT 1 FROM films WHERE id = ?)
                """;
    }

    public static final class Friendship {
        public static final String SQL_FRIENDSHIP_RECIPROCAL = """
                SELECT status FROM friendships
                WHERE requester_id = ? AND addressee_id = ?
                """;

        public static final String SQL_FRIENDSHIP_UPSERT = """
                MERGE INTO friendships (requester_id, addressee_id, status)
                KEY (requester_id, addressee_id) VALUES (?, ?, ?)
                """;

        public static final String SQL_FRIENDSHIP_UPDATE_BOTH_CONFIRMED = """
                UPDATE friendships SET status = ?
                WHERE (requester_id = ? AND addressee_id = ?) OR (requester_id = ? AND addressee_id = ?)
                """;

        public static final String SQL_FRIENDSHIP_DELETE = """
                DELETE FROM friendships
                WHERE requester_id = ? AND addressee_id = ?
                """;

        public static final String SQL_FRIENDSHIP_UPDATE_DEMOTE_TO_UNCONFIRMED = """
                UPDATE friendships SET status = ?
                WHERE requester_id = ? AND addressee_id = ?
                """;

        public static final String SQL_FRIENDSHIP_GET_ALL = """
                SELECT u.id, u.email, u.login, u.name, u.birthday
                FROM friendships f
                JOIN users u ON u.id = f.addressee_id
                WHERE f.requester_id = ? AND f.status = 'CONFIRMED'
                ORDER BY u.id
                """;

        public static final String SQL_FRIENDSHIP_COMMON = """
                SELECT u.id, u.email, u.login, u.name, u.birthday
                FROM friendships f1
                JOIN friendships f2 ON f1.addressee_id = f2.addressee_id
                    AND f1.status = 'CONFIRMED' AND f2.status = 'CONFIRMED'
                JOIN users u ON u.id = f1.addressee_id
                WHERE f1.requester_id = ? AND f2.requester_id = ?
                ORDER BY u.id
                """;
    }

    public static final class FilmLike {
        public static final String SQL_FILMLIKE_UPSERT =
                "MERGE INTO film_likes (film_id, user_id) KEY (film_id, user_id) VALUES (?, ?)";

        public static final String SQL_FILMLIKE_DELETE =
                "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";

        // «Популярные»: оставляем фильмы с 0 лайков (LEFT JOIN), сортируем по COUNT desc
        public static final String SQL_FILMLIKE_POPULAR = """
                SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa
                FROM films f
                LEFT JOIN film_likes fl ON fl.film_id = f.id
                GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa
                ORDER BY COUNT(fl.user_id) DESC, f.id ASC
                LIMIT ?
                """;
    }

    public static final class Genre {
        public static final String SQL_GENRE_SELECT_ALL = """
                SELECT g.id, g.name
                FROM genres g
                ORDER BY g.id
                """;

        public static final String SQL_GENRE_SELECT_BY_ID = """
                SELECT g.id, g.name
                FROM genres g
                WHERE g.id = ?
                """;

        public static final String SQL_GENRE_INSERT = """
                INSERT INTO genres (id, name) VALUES (?, ?)
                """;

        public static final String SQL_GENRE_UPDATE = """
                UPDATE genres SET name = ? WHERE id = ?
                """;

        public static final String SQL_GENRE_DELETE = """
                DELETE FROM genres WHERE id = ?
                """;

        public static final String SQL_GENRE_DELETE_BY_FILM_ID = """
                DELETE FROM film_genres WHERE film_id = ?
                """;

        public static final String SQL_GENRE_INSERT_BY_FILM_ID = """
                INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)
                """;

        public static final String SQL_GENRE_SELECT_BY_FILM_ID = """
                SELECT g.id, g.name
                FROM genres g
                JOIN film_genres fg ON fg.genre_id = g.id
                WHERE fg.film_id = ?
                ORDER BY g.id
                """;
    }

    public static final class Mpa {
        public static final String SQL_MPA_SELECT_ALL = """
                SELECT m.id, m.name
                FROM mpa_ratings m
                ORDER BY m.id
                """;

        public static final String SQL_MPA_SELECT_BY_ID = """
                SELECT m.id, m.name
                FROM mpa_ratings m
                WHERE m.id = ?
                """;
    }
}
