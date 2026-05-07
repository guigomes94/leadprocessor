package com.desafio.leadprocessor.controller;

import com.desafio.leadprocessor.domain.LoteProcessamento;
import com.desafio.leadprocessor.repository.LoteProcessamentoRepository;
import com.desafio.leadprocessor.repository.LoteRepository;
import com.desafio.leadprocessor.service.LoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/lotes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Libera o CORS para o nosso frontend React no futuro
public class LoteController {

    private final LoteService loteService;
    private final LoteRepository loteRepository;
    private final LoteProcessamentoRepository processamentoRepository;

    @PostMapping
    public ResponseEntity<Map<String, UUID>> uploadCsv(@RequestParam("file") MultipartFile file) {
        try {
            UUID loteId = loteService.receberUploadCsv(file);

            // Retornamos 202 (Accepted) em vez de 200 (OK) ou 201 (Created).
            // Essa é a melhor prática REST para processamentos assíncronos,
            // indicando que a requisição foi aceita, mas ainda não terminou.
            return ResponseEntity.accepted().body(Map.of("loteId", loteId));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", null)); // ou tratar a mensagem de erro adequadamente
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> obterStatusLote(@PathVariable UUID id) {
        Optional<LoteProcessamento> procOpt = processamentoRepository.findByLoteId(id);
        Optional<com.desafio.leadprocessor.domain.Lote> loteOpt = loteRepository.findById(id);

        if (procOpt.isPresent() && loteOpt.isPresent()) {
            LoteProcessamento proc = procOpt.get();

            // Monta um objeto limpo para o React
            Map<String, Object> response = Map.of(
                    "totalLinhas", proc.getTotalLinhas(),
                    "linhasSucesso", proc.getLinhasSucesso(),
                    "linhasErro", proc.getLinhasErro(),
                    "tempoProcessamentoMs", proc.getTempoProcessamentoMs(),
                    "status", loteOpt.get().getStatus().name() // Aqui mandamos o status!
            );
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.notFound().build();
    }
}