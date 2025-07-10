package com.zorzi.backend.repository;

import com.zorzi.backend.model.Choice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChoiceRepository extends JpaRepository<Choice, Long> {
}
