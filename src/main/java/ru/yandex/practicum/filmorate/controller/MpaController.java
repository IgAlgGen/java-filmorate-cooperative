package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.List;

@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class MpaController {

    private final MpaService mpaService;

    @GetMapping
    public ResponseEntity<List<MpaRating>> getAll() {
        return ResponseEntity.ok(mpaService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MpaRating> getById(@PathVariable int id) {
        MpaRating mpa = mpaService.findById(id);
        return mpa != null ? ResponseEntity.ok(mpa) : ResponseEntity.notFound().build();
    }
}

