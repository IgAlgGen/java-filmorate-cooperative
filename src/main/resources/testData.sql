MERGE INTO mpa_ratings (id, name) KEY(id) VALUES
 (1,'G'),(2,'PG'),(3,'PG_13'),(4,'R'),(5,'NC_17');

MERGE INTO genres (id, name) VALUES
  (1,'Comedy'),(2,'Drama'),(3,'Action'),(4,'Thriller'),(5,'Horror'),
  (6,'Documentary'),(7,'Animation');

INSERT INTO users (id, email, login, name, birthday) VALUES
    (1, 'test1@example.com', 'test1', 'Test1', '1990-01-01'),
    (2, 'test2@example.com', 'test2', 'Test2', '1990-01-02'),
    (3, 'test3@example.com', 'test3', 'Test3', '1990-01-03');

INSERT INTO films (id, name, description, release_date, duration, mpa) VALUES
    (1, 'Film1', 'Description1', '2000-01-01', 120, 1),
    (2, 'Film2', 'Description2', '2001-01-01', 90, 2),
    (3, 'Film3', 'Description3', '2002-01-01', 110, 3);