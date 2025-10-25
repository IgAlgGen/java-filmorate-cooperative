package ru.yandex.practicum.filmorate.storage.review;

public enum ReviewQuery {
    CREATE_REVIEW("sql/review/create_review.sql"),
    UPDATE_REVIEW("sql/review/update_review.sql"),
    DELETE_REVIEW("sql/review/delete_review.sql"),
    GET_REVIEW_BY_ID("sql/review/get_review_by_id.sql"),
    GET_ALL_REVIEWS("sql/review/get_all_reviews.sql"),
    GET_REVIEWS_BY_FILM_ID("sql/review/get_reviews_by_film_id.sql"),
    ADD_REVIEW_LIKE("sql/review/add_review_like.sql"),
    REMOVE_REVIEW_LIKE("sql/review/remove_review_like.sql");

    private final String sql;

    ReviewQuery(String queryPath) {
        this.sql = loadSql(queryPath);
    }

    public String getSql() {
        return sql;
    }

    private String loadSql(String path){
        try{
            return java.nio.file.Files.readString(java.nio.file.Path.of(path));
        } catch (java.io.IOException | NullPointerException e ) {
            throw new IllegalStateException("Не удалось загрузить SQL-файл: " + path, e);
        }
    }
}
