package com.desafio.leadprocessor.controller;

import com.desafio.leadprocessor.domain.LoteProcessamento;
import com.desafio.leadprocessor.repository.LoteProcessamentoRepository;
import com.desafio.leadprocessor.repository.LoteRepository;
import com.desafio.leadprocessor.service.LoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/lotes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LoteController {

    private final LoteService loteService;
    private final LoteRepository loteRepository;
    private final LoteProcessamentoRepository processamentoRepository;

    @PostMapping
    public ResponseEntity<?> uploadCsv(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("erro", "O arquivo não pode estar vazio."));
        }

        // === VALIDAÇÃO RESTRITA DE UTF-8 ===
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                file.getInputStream(),
                StandardCharsets.UTF_8.newDecoder()
                        .onMalformedInput(CodingErrorAction.REPORT)
                        .onUnmappableCharacter(CodingErrorAction.REPORT)))) {

            // Varre o arquivo rapidamente na memória para forçar o erro se não for UTF-8
            char[] buffer = new char[8192];
            while (br.read(buffer) != -1) {
                // Apenas lendo para validar os bytes
            }

        } catch (MalformedInputException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "erro", "Codificação inválida. O arquivo CSV deve estar obrigatoriamente no formato UTF-8."
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("erro", "Erro ao ler o arquivo."));
        }

        try {
            // Substitua esta linha pela chamada real do seu serviço
            UUID loteId = loteService.receberUploadCsv(file);

            return ResponseEntity.accepted().body(Map.of(
                    "mensagem", "Upload recebido com sucesso",
                    "loteId", loteId
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("erro", e.getMessage()));
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