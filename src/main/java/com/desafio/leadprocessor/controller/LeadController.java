package com.desafio.leadprocessor.controller;

import com.desafio.leadprocessor.domain.Lead;
import com.desafio.leadprocessor.repository.LeadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leads")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LeadController {

    private final LeadRepository leadRepository;

    // Exemplo de requisição: GET /api/leads?nome=João&page=0&size=10
    @GetMapping
    public ResponseEntity<Page<Lead>> listarLeads(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String origem,
            Pageable pageable) {

        Page<Lead> leads = leadRepository.findComFiltros(nome, email, origem, pageable);
        return ResponseEntity.ok(leads);
    }
}