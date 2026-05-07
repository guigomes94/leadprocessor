package com.desafio.leadprocessor.service;

import com.desafio.leadprocessor.domain.Lote;
import com.desafio.leadprocessor.domain.LoteProcessamento;
import com.desafio.leadprocessor.domain.StatusLote;
import com.desafio.leadprocessor.dto.LoteIniciadoEvent;
import com.desafio.leadprocessor.repository.LoteProcessamentoRepository;
import com.desafio.leadprocessor.repository.LoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoteService {

    private final LoteRepository loteRepository;
    private final LoteProcessamentoRepository processamentoRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final CsvProcessorService csvProcessor;
    
    public UUID receberUploadCsv(MultipartFile file) throws IOException {
        // Validação básica exigida no desafio
        if (file.isEmpty() || !file.getOriginalFilename().endsWith(".csv")) {
            throw new IllegalArgumentException("Arquivo inválido. Envie um .csv válido.");
        }

        // 1. Cria o Lote
        Lote lote = Lote.builder()
                .nomeArquivo(file.getOriginalFilename())
                .status(StatusLote.PROCESSANDO) // Já vai direto para processando
                .build();
        loteRepository.save(lote);

        // 2. Cria as estatísticas zeradas
        LoteProcessamento proc = LoteProcessamento.builder().lote(lote).build();
        processamentoRepository.save(proc);

        // 3. Salva em um arquivo temporário no SO para a thread assíncrona poder ler
        File tempFile = Files.createTempFile("upload_lote_", ".csv").toFile();
        file.transferTo(tempFile);

        // 4. Publica o evento de início
        kafkaTemplate.send("lote.iniciado", new LoteIniciadoEvent(lote.getId()));

        // 5. Dispara o processamento assíncrono (Fire and Forget)
        csvProcessor.processarLoteAssincrono(lote.getId(), tempFile);

        return lote.getId();
    }
}