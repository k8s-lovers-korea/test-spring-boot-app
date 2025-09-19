package com.k8sloverskorea.testspringbootapp.repository;

import com.k8sloverskorea.testspringbootapp.model.TestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestEntityRepository extends JpaRepository<TestEntity, Long> {
    
    List<TestEntity> findByNameContainingIgnoreCase(String name);
    
    @Query("SELECT t FROM TestEntity t WHERE t.description IS NOT NULL")
    List<TestEntity> findAllWithDescription();
}