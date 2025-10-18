package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.net.URI;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/directors")
@RequiredArgsConstructor
public class DirectorController {

    private final DirectorService directorService;

    @PostMapping
    public ResponseEntity<Director> create (@Valid @RequestBody Director director){
        log.info("Добавление режиссера: {}", director.toString());
        Director created = directorService.create(director);
        URI location = URI.create("/directors/" + created.getId());
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping
    public ResponseEntity<Director> update (@Valid @RequestBody Director director){
        log.info("Обновление режиссера с ID {}: {}", director.getId(), director.toString());
        Director updated = directorService.update(director);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Director> getById (int id){
        log.info("Получение режиссера с ID {}", id);
        return ResponseEntity.ok(directorService.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete (int id){
        log.info("Удаление режиссера с ID {}", id);
        directorService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<Director>> getAll (){
        log.info("Получение списка всех режиссеров");
        List<Director> directors = directorService.getAll();
        return ResponseEntity.ok(directors);
    }

}

