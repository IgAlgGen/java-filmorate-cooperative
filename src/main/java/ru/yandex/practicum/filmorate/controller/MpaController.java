package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class MpaController {

    private final MpaService mpaService;

    @GetMapping
    public ResponseEntity<List<MpaRating>> getAll() {
        log.info("Получение списка всех рейтингов MPA");
        return ResponseEntity.ok(mpaService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MpaRating> getById(@PathVariable int id) {
        log.info("Получение рейтинга MPA с ID {}", id);
        MpaRating mpa = mpaService.findById(id);
        return mpa != null ? ResponseEntity.ok(mpa) : ResponseEntity.notFound().build();
    }
}

