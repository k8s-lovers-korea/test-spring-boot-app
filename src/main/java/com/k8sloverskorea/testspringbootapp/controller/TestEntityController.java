package com.k8sloverskorea.testspringbootapp.controller;

import com.k8sloverskorea.testspringbootapp.model.TestEntity;
import com.k8sloverskorea.testspringbootapp.service.TestEntityService;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/entities")
public class TestEntityController {
    
    private static final Logger logger = LoggerFactory.getLogger(TestEntityController.class);
    
    @Autowired
    private TestEntityService entityService;
    
    @Autowired
    private Tracer tracer;
    
    @GetMapping
    public ResponseEntity<List<TestEntity>> getAllEntities() {
        Span span = tracer.spanBuilder("get-all-entities-endpoint").startSpan();
        try {
            logger.info("GET /api/entities - Retrieving all entities");
            List<TestEntity> entities = entityService.getAllEntities();
            return ResponseEntity.ok(entities);
        } finally {
            span.end();
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TestEntity> getEntityById(@PathVariable Long id) {
        Span span = tracer.spanBuilder("get-entity-by-id-endpoint").startSpan();
        try {
            logger.info("GET /api/entities/{} - Retrieving entity by id", id);
            Optional<TestEntity> entity = entityService.getEntityById(id);
            return entity.map(ResponseEntity::ok)
                         .orElse(ResponseEntity.notFound().build());
        } finally {
            span.end();
        }
    }
    
    @PostMapping
    public ResponseEntity<TestEntity> createEntity(@RequestBody TestEntity entity) {
        Span span = tracer.spanBuilder("create-entity-endpoint").startSpan();
        try {
            logger.info("POST /api/entities - Creating new entity: {}", entity.getName());
            TestEntity createdEntity = entityService.createEntity(entity);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdEntity);
        } finally {
            span.end();
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<TestEntity> updateEntity(@PathVariable Long id, @RequestBody TestEntity entity) {
        Span span = tracer.spanBuilder("update-entity-endpoint").startSpan();
        try {
            logger.info("PUT /api/entities/{} - Updating entity", id);
            TestEntity updatedEntity = entityService.updateEntity(id, entity);
            return ResponseEntity.ok(updatedEntity);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } finally {
            span.end();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEntity(@PathVariable Long id) {
        Span span = tracer.spanBuilder("delete-entity-endpoint").startSpan();
        try {
            logger.info("DELETE /api/entities/{} - Deleting entity", id);
            entityService.deleteEntity(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } finally {
            span.end();
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<TestEntity>> searchEntities(@RequestParam String name) {
        Span span = tracer.spanBuilder("search-entities-endpoint").startSpan();
        try {
            logger.info("GET /api/entities/search?name={} - Searching entities", name);
            List<TestEntity> entities = entityService.searchEntitiesByName(name);
            return ResponseEntity.ok(entities);
        } finally {
            span.end();
        }
    }
}