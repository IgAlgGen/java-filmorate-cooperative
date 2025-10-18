package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/directors")
@RequiredArgsConstructor
public class DirectorController {

    private final DirectorService directorService;

    @PostMapping
    public ResponseEntity<Director> create (@Valid @RequestBody Director director){
        return null;
    }

    @PutMapping
    public ResponseEntity<Director> update (@Valid @RequestBody Director director){
        return null;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Director> getById (int id){
        return null;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete (int id){
        return null;
    }

    @GetMapping
    public ResponseEntity<List<Director>> getAll (){
        return null;
    }

}

