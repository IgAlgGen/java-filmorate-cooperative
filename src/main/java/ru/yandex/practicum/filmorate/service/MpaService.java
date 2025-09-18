package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MpaService {
    private final MpaStorage mpaStorage;

    public List<MpaRating> findAll() {
        log.debug("Поиск всех рейтингов MPA");
        List<MpaRating> list = mpaStorage.findAll();
        log.debug("Найдено {} рейтингов MPA", list.size());
        return list;
    }

    public MpaRating findById(int id) {
        log.debug("Поиск рейтинга MPA по ID {}", id);
        MpaRating mpa = mpaStorage.findById(id).orElse(null);
        if (mpa != null) {
            log.debug("Найден рейтинг MPA: id={}, name='{}'", mpa.getId(), mpa.getTitle());
        } else {
            log.debug("Рейтинг MPA с ID {} не найден", id);
        }
        return mpa;
    }
}
