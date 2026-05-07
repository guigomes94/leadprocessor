package com.desafio.leadprocessor.repository;

import com.desafio.leadprocessor.domain.LoteProcessamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LoteProcessamentoRepository extends JpaRepository<LoteProcessamento, UUID> {

    Optional<LoteProcessamento> findByLoteId(UUID loteId);
}
