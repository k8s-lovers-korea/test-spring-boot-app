package com.k8sloverskorea.testspringbootapp.controller;

import com.k8sloverskorea.testspringbootapp.model.TestEntity;
import com.k8sloverskorea.testspringbootapp.service.TestEntityService;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
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
@Tag(name = "엔티티 API", description = "TestEntity CRUD 및 검색 API")
public class TestEntityController {
    
    private static final Logger logger = LoggerFactory.getLogger(TestEntityController.class);
    
    @Autowired
    private TestEntityService entityService;
    
    @Autowired
    private Tracer tracer;
    
    @GetMapping
    @Operation(summary = "엔티티 전체 조회", description = "모든 TestEntity 목록을 반환합니다.")
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
    @Operation(summary = "엔티티 단건 조회", description = "ID로 TestEntity를 조회합니다.")
    public ResponseEntity<TestEntity> getEntityById(@Parameter(description = "조회할 엔티티의 ID") @PathVariable Long id) {
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
    @Operation(summary = "엔티티 생성", description = "요청 본문의 TestEntity로 새 엔티티를 생성합니다.")
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
    @Operation(summary = "엔티티 수정", description = "ID에 해당하는 엔티티를 요청 본문 값으로 업데이트합니다.")
    public ResponseEntity<TestEntity> updateEntity(@Parameter(description = "수정할 엔티티의 ID") @PathVariable Long id, @RequestBody TestEntity entity) {
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
    @Operation(summary = "엔티티 삭제", description = "ID에 해당하는 엔티티를 삭제합니다.")
    public ResponseEntity<Void> deleteEntity(@Parameter(description = "삭제할 엔티티의 ID") @PathVariable Long id) {
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
    @Operation(summary = "엔티티 검색", description = "이름에 지정한 키워드가 포함된 엔티티를 검색합니다.")
    public ResponseEntity<List<TestEntity>> searchEntities(@Parameter(description = "이름 검색 키워드") @RequestParam String name) {
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