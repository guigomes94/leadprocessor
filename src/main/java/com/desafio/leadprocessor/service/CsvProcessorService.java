package com.desafio.leadprocessor.service;

import com.desafio.leadprocessor.domain.Lead;
import com.desafio.leadprocessor.domain.Lote;
import com.desafio.leadprocessor.dto.LoteChunkConcluidoEvent;
import com.desafio.leadprocessor.dto.LoteFinalizadoEvent;
import com.desafio.leadprocessor.repository.LeadRepository;
import com.desafio.leadprocessor.repository.LoteRepository;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CsvProcessorService {

    @Value("${app.lote.chunk-size}")
    private int chunkSize;

    private final LeadRepository leadRepository;
    private final LoteRepository loteRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Async
    public void processarLoteAssincrono(UUID loteId, File tempCsvFile) {
        log.info("Iniciando processamento do lote {} usando Virtual Threads", loteId);
        Lote loteRef = loteRepository.getReferenceById(loteId); // Proxy do Hibernate para o FK

        // Usamos o Executor de Virtual Threads do Java 21
        try (var executor = Executors.newVirtualThreadPerTaskExecutor();
             var leitor = new FileReader(tempCsvFile, StandardCharsets.UTF_8);
             CSVReader csvReader = new CSVReaderBuilder(leitor).withSkipLines(1).build()) {

            List<Future<?>> tarefasAtivas = new ArrayList<>();
            List<String[]> chunkAtual = new ArrayList<>(chunkSize);
            String[] linha;

            // Lê o CSV particionando em chunks
            while ((linha = csvReader.readNext()) != null) {
                chunkAtual.add(linha);

                if (chunkAtual.size() == chunkSize) {
                    List<String[]> copiaChunk = new ArrayList<>(chunkAtual);
                    tarefasAtivas.add(executor.submit(() -> processarChunk(loteRef, copiaChunk)));
                    chunkAtual.clear();
                }
            }

            // Processa o resto que não encheu um chunk completo
            if (!chunkAtual.isEmpty()) {
                tarefasAtivas.add(executor.submit(() -> processarChunk(loteRef, chunkAtual)));
            }

            // Aguarda todas as Virtual Threads terminarem de salvar no banco e publicar no Kafka
            for (Future<?> tarefa : tarefasAtivas) {
                tarefa.get();
            }

            // Tudo finalizado! Notifica o Kafka.
            kafkaTemplate.send("lote-eventos", new LoteFinalizadoEvent(loteId));
            log.info("Lote {} concluído com sucesso.", loteId);

        } catch (Exception e) {
            log.error("Erro fatal ao processar o lote {}", loteId, e);
            // Aqui caberia atualizar o status do lote para ERRO no banco
        } finally {
            // Limpeza: apaga o arquivo temporário do SO
            if (tempCsvFile.exists()) {
                tempCsvFile.delete();
            }
        }
    }

    private void processarChunk(Lote loteRef, List<String[]> linhasCsv) {
        long inicioMs = System.currentTimeMillis();
        int sucesso = 0;
        int erro = 0;

        for (String[] colunas : linhasCsv) {
            try {
                // Mapeamento: nome, email, telefone, origem, data_cadastro
                Lead lead = Lead.builder()
                        .id(UUID.randomUUID())
                        .lote(loteRef)
                        .nome(colunas[0])
                        .email(colunas[1])
                        .telefone(colunas[2])
                        .origem(colunas[3])
                        .dataCadastro(Instant.now()) // Poderia parsear colunas[4] se fosse uma data real
                        .build();

                // Tenta salvar 1 a 1 para isolar a constraint de unicidade de e-mail.
                // Em um cenário real super otimizado, faríamos um saveAll, mas lidar com constraint
                // violation no saveAll do Hibernate é custoso pois ele faz rollback da transação inteira.
                leadRepository.saveAndFlush(lead);
                sucesso++;

            } catch (DataIntegrityViolationException ex) {
                // Caiu aqui? O e-mail já existe no banco. Incrementamos o erro.
                erro++;
            } catch (Exception ex) {
                erro++;
            }
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long tempoGasto = System.currentTimeMillis() - inicioMs;

        // Publica estatísticas parciais deste chunk
        kafkaTemplate.send("lote-eventos",
                new LoteChunkConcluidoEvent(loteRef.getId(), sucesso, erro, tempoGasto));
    }
}