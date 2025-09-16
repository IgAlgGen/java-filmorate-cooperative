package ru.yandex.practicum.filmorate.storage.sql;

public final class SqlKeys {
    private SqlKeys() {
    }

    public static final class User {
        public static final String SQL_SELECT_ALL = """
                SELECT user_id, email, login, name, birthday
                FROM users
                ORDER BY user_id
                """;

        public static final String SQL_SELECT_BY_ID = """
                SELECT user_id, email, login, name, birthday
                FROM users
                WHERE user_id = ?
                """;

        public static final String SQL_UPDATE = """
                UPDATE users
                SET email = ?, login = ?, name = ?, birthday = ?
                WHERE user_id = ?
                """;

        public static final String SQL_DELETE = """
                DELETE FROM users WHERE user_id = ?
                """;

        public static final String SQL_USER_EXISTS = """
                SELECT EXISTS(SELECT 1 FROM users WHERE user_id = ?)
                """;
    }

    public static final class Film {
        public static final String SQL_SELECT_ALL = """
                SELECT film_id, name, description, release_date, duration, mpa
                FROM films
                ORDER BY film_id
                """;

        public static final String SQL_SELECT_BY_ID = """
                SELECT film_id, name, description, release_date, duration, mpa
                FROM films
                WHERE film_id = ?
                """;

        public static final String SQL_UPDATE = """
                UPDATE films
                SET name = ?, description = ?, release_date = ?, duration = ?, mpa = ?
                WHERE film_id = ?
                """;

        public static final String SQL_DELETE = """
                DELETE FROM films WHERE film_id = ?
                """;

        public static final String SQL_FILM_EXISTS = """
                SELECT EXISTS(SELECT 1 FROM films WHERE film_id = ?)
                """;
    }

    public static final class Friendship {
        public static final String SQL_RECIPROCAL = """
                SELECT status FROM friendships
                WHERE requester_id = ? AND addressee_id = ?
                """;
        public static final String SQL_UPSERT = """
                MERGE INTO friendships (requester_id, addressee_id, status)
                KEY (requester_id, addressee_id) VALUES (?, ?, ?)
                """;
        public static final String SQL_UPDATE_BOTH_CONFIRMED = """
                UPDATE friendships SET status = ?
                WHERE (requester_id = ? AND addressee_id = ?) OR (requester_id = ? AND addressee_id = ?)
                """;
        public static final String SQL_DELETE = """
                DELETE FROM friendships
                WHERE requester_id = ? AND addressee_id = ?
                """;
        public static final String SQL_UPDATE_DEMOTE_TO_UNCONFIRMED = """
                UPDATE friendships SET status = ?
                WHERE requester_id = ? AND addressee_id = ?
                """;

        public static final String SQL_LIST_FRIENDS = """
                SELECT u.user_id, u.email, u.login, u.name, u.birthday
                FROM friendships f
                JOIN users u ON u.user_id = f.addressee_id
                WHERE f.requester_id = ? AND f.status = 'CONFIRMED'
                ORDER BY u.user_id
                """;

        public static final String SQL_COMMON_FRIENDS = """
                SELECT u.user_id, u.email, u.login, u.name, u.birthday
                FROM friendships f1
                JOIN friendships f2 ON f1.addressee_id = f2.addressee_id
                    AND f1.status = 'CONFIRMED' AND f2.status = 'CONFIRMED'
                JOIN users u ON u.user_id = f1.addressee_id
                WHERE f1.requester_id = ? AND f2.requester_id = ?
                ORDER BY u.user_id
                """;
    }

    public static final class FilmLike {
        public static final String SQL_UPSERT_LIKE =
                "MERGE INTO film_likes (film_id, user_id) KEY (film_id, user_id) VALUES (?, ?)";

        public static final String SQL_DELETE_LIKE =
                "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";

        // «Популярные»: оставляем фильмы с 0 лайков (LEFT JOIN), сортируем по COUNT desc
        public static final String SQL_POPULAR = """
                SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa
                FROM films f
                LEFT JOIN film_likes fl ON fl.film_id = f.film_id
                GROUP BY f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa
                ORDER BY COUNT(fl.user_id) DESC, f.film_id ASC
                LIMIT ?
                """;
    }
}
