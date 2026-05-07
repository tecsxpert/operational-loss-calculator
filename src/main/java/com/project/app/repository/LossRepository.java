package com.project.app.repository;

import com.project.app.entity.Loss;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LossRepository extends JpaRepository<Loss, Long> {
    Page<Loss> findByDeletedFalse(Pageable pageable);
    Optional<Loss> findByIdAndDeletedFalse(Long id);
}
