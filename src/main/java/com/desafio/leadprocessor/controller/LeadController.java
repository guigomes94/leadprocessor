package com.desafio.leadprocessor.controller;

import com.desafio.leadprocessor.domain.Lead;
import com.desafio.leadprocessor.repository.LeadRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leads")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Leads", description = "Endpoints para gerenciamento dos leads")
public class LeadController {

    private final LeadRepository leadRepository;

    // Exemplo de requisição: GET /api/leads?nome=João&page=0&size=10
    @GetMapping
    @Operation(summary = "Lista os leads por filtros e paginação")
    public ResponseEntity<Page<Lead>> listarLeads(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String origem,
            Pageable pageable) {

        Page<Lead> leads = leadRepository.findComFiltros(nome, email, origem, pageable);
        return ResponseEntity.ok(leads);
    }
}