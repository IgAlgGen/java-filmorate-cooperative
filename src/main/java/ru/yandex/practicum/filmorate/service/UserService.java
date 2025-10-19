package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.utils.FilmRecommendationData;
import ru.yandex.practicum.filmorate.storage.filmLike.FilmLikeStorage;
import ru.yandex.practicum.filmorate.storage.friendship.FriendshipStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    @Qualifier("userDbStorage")
    private final UserStorage userStorage;

    @Qualifier("friendshipDbStorage")
    private final FriendshipStorage friendshipStorage;

    @Qualifier("filmLikeDbStorage")
    private final FilmLikeStorage filmLikeStorage;

    private final FilmService filmService;


    public User create(User u) {
        log.debug("Создание пользователя: email='{}', login='{}', birthday={}", u.getEmail(), u.getLogin(), u.getBirthday());
        User createdUser = userStorage.create(u);
        log.debug("Пользователь создан: id={}, login='{}'", createdUser.getId(), createdUser.getLogin());
        return createdUser;
    }

    public User update(User u) {
        log.debug("Обновление пользователя: id={}, email='{}', login='{}', birthday={}",
                u.getId(),
                u.getEmail(),
                u.getLogin(),
                u.getBirthday());
        User existingUser = userStorage.update(u);
        log.debug("Пользователь обновлен: id={}, login='{}'", existingUser.getId(), existingUser.getLogin());
        return existingUser;
    }

    public User getById(int id) {
        log.debug("Поиск пользователя по ID {}", id);
        User user = userStorage.getById(id).orElseThrow();
        log.debug("Найден пользователь: id={}, login='{}'", user.getId(), user.getLogin());
        return user;
    }

    public List<User> getAll() {
        log.debug("Поиск всех пользователей");
        List<User> list = userStorage.getAll();
        log.debug("Найдено {} пользователей", list.size());
        return list;
    }

    public void delete(int id) {
        log.debug("Удаление пользователя с ID {}", id);
        userStorage.deleteById(id);
        log.debug("Пользователь с ID {} удален", id);
    }

    public void addFriend(int userId, int friendId) {
        log.debug("Пользователь с ID {} добавляет в друзья пользователя с ID {}", userId, friendId);
        friendshipStorage.addFriend(userId, friendId);
        log.debug("Пользователь с ID {} добавил в друзья пользователя с ID {}", userId, friendId);
    }

    public void removeFriend(int userId, int friendId) {
        log.debug("Пользователь с ID {} удаляет из друзей пользователя с ID {}", userId, friendId);
        // (опционально) checkUserExistence(userId); checkUserExistence(friendId)
        friendshipStorage.removeFriend(userId, friendId);
        log.debug("Пользователь с ID {} удалил из друзей пользователя с ID {}", userId, friendId);
    }

    public List<User> getFriends(int userId) {
        log.debug("Получение списка друзей пользователя с ID {}", userId);
        List<User> friends = friendshipStorage.findFriendsOf(userId);
        log.debug("Найдено {} друзей пользователя с ID {}", friends.size(), userId);
        return friends;
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        log.debug("Получение списка общих друзей пользователей с ID {} и ID {}", userId, otherId);
        List<User> commonFriends = friendshipStorage.findCommonFriends(userId, otherId);
        log.debug("Найдено {} общих друзей пользователей с ID {} и ID {}", commonFriends.size(), userId, otherId);
        return commonFriends;
    }

    public List<Film> getRecommendations(int targetUserId) {
        log.debug("Получение рекомендаций фильмов для пользователя с ID {}", targetUserId);
        // получаем Map всех пользователей с id фильмов, которые они лайкали
        Map<Integer, Set<Integer>> usersLikesData = filmLikeStorage.getUsersLikesData();

        // если пользователя нет в данных, значит он ничего не лайкал, и рекомендаций ему дать нельзя
        if (!usersLikesData.containsKey(targetUserId)) {
            log.debug("Пользователь с ID={} не имеет ни одного лайка", targetUserId);
            return new ArrayList<>();
        }

        // преобразуем множества (Set) id фильмов в объекты FilmRecommendationData
        Map<Integer, FilmRecommendationData> filmRecommendationDataMap = usersLikesData.entrySet().stream()
                .map(userIdMovieIdSetEntry -> {
                    int userId = userIdMovieIdSetEntry.getKey();
                    Set<Integer> movieIdSet = userIdMovieIdSetEntry.getValue();
                    Map.Entry<Integer, FilmRecommendationData> newEntry = Map.entry(userId, new FilmRecommendationData(userId));
                    newEntry.getValue().setLikedFilmIds(movieIdSet);
                    return newEntry;
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        // сохраняем информацию о пользователе, которому нужно дать рекомендации
        // и удаляем его из filmRecommendationDataMap
        Set<Integer> targetUserLikedFilms = filmRecommendationDataMap.get(targetUserId).getLikedFilmIds();
        filmRecommendationDataMap.remove(targetUserId);

        // если пользователей в filmRecommendationDataMap не осталось, значит рекомендаций сделать нельзя
        if (filmRecommendationDataMap.isEmpty()) {
            log.debug("В БД недостаточно пользователей с лайками, чтобы сделать рекомендации");
            return new ArrayList<>();
        }

        // для всех пользователей в filmRecommendationDataMap рассчитываем схожесть по лайкам
        // сортируем их по этой схожести
        // берем первого
        Set<Integer> mostSimilarUserLikedFilms = filmRecommendationDataMap.values().stream()
                .peek(recData -> recData.computeSimilarityScore(targetUserLikedFilms))
                .max(Comparator.comparing(FilmRecommendationData::getSimilarityScore))
                .map(FilmRecommendationData::getLikedFilmIds)
                .orElse(Collections.emptySet());

        // находим ID фильмов, которые пользователь не лайкал
        // возвращаем список объектов этих фильмов
        Set<Integer> filmsUserDoesNotHave = new HashSet<>(mostSimilarUserLikedFilms);
        filmsUserDoesNotHave.removeAll(targetUserLikedFilms);
        log.debug("ID фильмов, которые рекомендуются пользователю: {}", filmsUserDoesNotHave);
        return filmsUserDoesNotHave.stream()
                .map(filmService::getById)
                .toList();
    }
}
