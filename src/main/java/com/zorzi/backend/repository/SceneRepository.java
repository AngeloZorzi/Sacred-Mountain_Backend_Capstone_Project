package com.zorzi.backend.repository;

import com.zorzi.backend.model.Scene;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SceneRepository extends JpaRepository<Scene, Long> {
    @Query("SELECT s FROM Scene s LEFT JOIN FETCH s.choices WHERE s.id = :id")
    Optional<Scene> findByIdWithChoices(@Param("id") Long id);
}
