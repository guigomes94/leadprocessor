package com.desafio.leadprocessor.consumer;

import com.desafio.leadprocessor.domain.Lote;
import com.desafio.leadprocessor.domain.LoteProcessamento;
import com.desafio.leadprocessor.domain.StatusLote;
import com.desafio.leadprocessor.dto.LoteChunkConcluidoEvent;
import com.desafio.leadprocessor.dto.LoteFinalizadoEvent;
import com.desafio.leadprocessor.dto.LoteIniciadoEvent;
import com.desafio.leadprocessor.repository.LoteProcessamentoRepository;
import com.desafio.leadprocessor.repository.LoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoteEventConsumer {

    private final LoteRepository loteRepository;
    private final LoteProcessamentoRepository processamentoRepository;

    @KafkaListener(topics = "lote.iniciado", groupId = "lead-processor-group")
    public void handleLoteIniciado(LoteIniciadoEvent event) {
        log.info("Kafka consumiu evento: Lote {} iniciado no tópico lote.iniciado", event.loteId());
    }

    @Transactional
    @KafkaListener(topics = "lote.chunk.concluido", groupId = "lead-processor-group")
    public void handleChunkConcluido(LoteChunkConcluidoEvent event) {
        log.info("Kafka consumiu evento: Chunk do lote {} concluído. Sucesso: {}, Erro: {}",
                event.loteId(), event.linhasSucesso(), event.linhasErro());

        LoteProcessamento progresso = processamentoRepository.findByLoteId(event.loteId())
                .orElseThrow(() -> new IllegalStateException("Processamento não encontrado para o lote"));

        progresso.setLinhasSucesso(progresso.getLinhasSucesso() + event.linhasSucesso());
        progresso.setLinhasErro(progresso.getLinhasErro() + event.linhasErro());
        progresso.setTotalLinhas(progresso.getTotalLinhas() + event.linhasSucesso() + event.linhasErro());
        progresso.setTempoProcessamentoMs(progresso.getTempoProcessamentoMs() + event.tempoProcessamentoMs());

        processamentoRepository.save(progresso);
    }

    @Transactional
    @KafkaListener(topics = "lote.finalizado", groupId = "lead-processor-group")
    public void handleLoteFinalizado(LoteFinalizadoEvent event) {
        log.info("Kafka consumiu evento: Lote {} totalmente finalizado no tópico lote.finalizado", event.loteId());

        Lote lote = loteRepository.findById(event.loteId())
                .orElseThrow(() -> new IllegalStateException("Lote não encontrado"));

        lote.setStatus(StatusLote.CONCLUIDO);
        loteRepository.save(lote);
    }
}