package com.k8sloverskorea.testspringbootapp.service;

import com.k8sloverskorea.testspringbootapp.model.TestEntity;
import com.k8sloverskorea.testspringbootapp.repository.TestEntityRepository;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TestEntityService {
    
    private static final Logger logger = LoggerFactory.getLogger(TestEntityService.class);
    
    @Autowired
    private TestEntityRepository repository;
    
    @Autowired
    private Tracer tracer;
    
    public List<TestEntity> getAllEntities() {
        Span span = tracer.spanBuilder("get-all-entities").startSpan();
        try {
            logger.debug("Fetching all entities");
            List<TestEntity> entities = repository.findAll();
            logger.info("Retrieved {} entities", entities.size());
            return entities;
        } finally {
            span.end();
        }
    }
    
    public Optional<TestEntity> getEntityById(Long id) {
        Span span = tracer.spanBuilder("get-entity-by-id").startSpan();
        try {
            logger.debug("Fetching entity with id: {}", id);
            Optional<TestEntity> entity = repository.findById(id);
            if (entity.isPresent()) {
                logger.info("Found entity: {}", entity.get());
            } else {
                logger.warn("Entity with id {} not found", id);
            }
            return entity;
        } finally {
            span.end();
        }
    }
    
    public TestEntity createEntity(TestEntity entity) {
        Span span = tracer.spanBuilder("create-entity").startSpan();
        try {
            logger.debug("Creating new entity: {}", entity);
            TestEntity savedEntity = repository.save(entity);
            logger.info("Created entity with id: {}", savedEntity.getId());
            return savedEntity;
        } finally {
            span.end();
        }
    }
    
    public TestEntity updateEntity(Long id, TestEntity updatedEntity) {
        Span span = tracer.spanBuilder("update-entity").startSpan();
        try {
            logger.debug("Updating entity with id: {}", id);
            return repository.findById(id)
                    .map(entity -> {
                        entity.setName(updatedEntity.getName());
                        entity.setDescription(updatedEntity.getDescription());
                        TestEntity saved = repository.save(entity);
                        logger.info("Updated entity: {}", saved);
                        return saved;
                    })
                    .orElseThrow(() -> {
                        logger.error("Entity with id {} not found for update", id);
                        return new RuntimeException("Entity not found with id: " + id);
                    });
        } finally {
            span.end();
        }
    }
    
    public void deleteEntity(Long id) {
        Span span = tracer.spanBuilder("delete-entity").startSpan();
        try {
            logger.debug("Deleting entity with id: {}", id);
            if (repository.existsById(id)) {
                repository.deleteById(id);
                logger.info("Deleted entity with id: {}", id);
            } else {
                logger.warn("Entity with id {} not found for deletion", id);
                throw new RuntimeException("Entity not found with id: " + id);
            }
        } finally {
            span.end();
        }
    }
    
    public List<TestEntity> searchEntitiesByName(String name) {
        Span span = tracer.spanBuilder("search-entities-by-name").startSpan();
        try {
            logger.debug("Searching entities by name: {}", name);
            List<TestEntity> entities = repository.findByNameContainingIgnoreCase(name);
            logger.info("Found {} entities matching name '{}'", entities.size(), name);
            return entities;
        } finally {
            span.end();
        }
    }
}