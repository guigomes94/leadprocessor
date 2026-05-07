package com.desafio.leadprocessor.repository;

import com.desafio.leadprocessor.domain.Lote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LoteRepository extends JpaRepository<Lote, UUID> {
}