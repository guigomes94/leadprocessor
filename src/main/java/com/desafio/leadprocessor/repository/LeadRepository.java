package com.desafio.leadprocessor.repository;

import com.desafio.leadprocessor.domain.Lead;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LeadRepository extends JpaRepository<Lead, UUID> {

    @Query("SELECT l FROM Lead l WHERE " +
            "(:nome IS NULL OR LOWER(l.nome) LIKE LOWER(CONCAT('%', CAST(:nome AS String), '%'))) AND " +
            "(:email IS NULL OR LOWER(l.email) LIKE LOWER(CONCAT('%', CAST(:email AS String), '%'))) AND " +
            "(:origem IS NULL OR LOWER(l.origem) LIKE LOWER(CONCAT('%', CAST(:origem AS String), '%'))) " +
            "ORDER BY l.nome ASC")
    Page<Lead> findComFiltros(
            @Param("nome") String nome,
            @Param("email") String email,
            @Param("origem") String origem,
            Pageable pageable
    );
}