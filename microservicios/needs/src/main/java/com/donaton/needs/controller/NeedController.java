package com.donaton.needs.controller;

import com.donaton.needs.model.NeedEntity;
import com.donaton.needs.service.NeedService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/needs")
public class NeedController {

    private final NeedService service;

    public NeedController(NeedService service) {
        this.service = service;
    }

    @GetMapping
    public List<NeedEntity> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status
    ) {
        return service.list(category, status);
    }

    @GetMapping("/{id}")
    public NeedEntity get(@PathVariable String id) {
        return service.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NeedEntity create(
            @RequestBody NeedEntity need,
            @RequestHeader("X-User-Email") String email
    ) {
        return service.create(need, email);
    }

    @PutMapping("/{id}")
    public NeedEntity update(@PathVariable String id, @RequestBody NeedEntity update) {
        return service.update(id, update);
    }

    @PutMapping("/{id}/receive")
    public NeedEntity receive(@PathVariable String id, @RequestParam Double amount) {
        return service.receive(id, amount);
    }

    @PutMapping("/{id}/rollback")
    public NeedEntity rollback(@PathVariable String id, @RequestParam Double amount) {
        return service.rollbackReceive(id, amount);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        service.delete(id);
    }
}
