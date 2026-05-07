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
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
@KafkaListener(topics = "lote-eventos", groupId = "lead-processor-group")
public class LoteEventConsumer {

    private final LoteRepository loteRepository;
    private final LoteProcessamentoRepository processamentoRepository;

    @KafkaHandler
    public void handleLoteIniciado(LoteIniciadoEvent event) {
        log.info("Kafka consumiu evento: Lote {} iniciado", event.loteId());
        // O status "PROCESSANDO" já foi definido no LoteService no momento do upload.
        // Este handler serve mais para registro ou futuras expansões.
    }

    @KafkaHandler
    @Transactional
    public void handleChunkConcluido(LoteChunkConcluidoEvent event) {
        log.info("Kafka consumiu evento: Chunk do lote {} concluído. Sucesso: {}, Erro: {}",
                event.loteId(), event.linhasSucesso(), event.linhasErro());

        // Busca as estatísticas atuais do banco
        LoteProcessamento progresso = processamentoRepository.findByLoteId(event.loteId())
                .orElseThrow(() -> new IllegalStateException("Processamento não encontrado para o lote"));

        // Acumula os novos valores do chunk
        progresso.setLinhasSucesso(progresso.getLinhasSucesso() + event.linhasSucesso());
        progresso.setLinhasErro(progresso.getLinhasErro() + event.linhasErro());
        progresso.setTotalLinhas(progresso.getTotalLinhas() + event.linhasSucesso() + event.linhasErro());
        progresso.setTempoProcessamentoMs(progresso.getTempoProcessamentoMs() + event.tempoProcessamentoMs());

        // O @Transactional garante que o Hibernate faça o UPDATE no banco automaticamente
        processamentoRepository.save(progresso);
    }

    @KafkaHandler
    @Transactional
    public void handleLoteFinalizado(LoteFinalizadoEvent event) {
        log.info("Kafka consumiu evento: Lote {} totalmente finalizado.", event.loteId());

        Lote lote = loteRepository.findById(event.loteId())
                .orElseThrow(() -> new IllegalStateException("Lote não encontrado"));

        lote.setStatus(StatusLote.CONCLUIDO);
        loteRepository.save(lote);
    }

    // É uma boa prática ter um fallback para eventos desconhecidos
    @KafkaHandler(isDefault = true)
    public void handleEventoDesconhecido(Object object) {
        log.warn("Kafka consumiu um evento de tipo desconhecido: {}", object.getClass());
    }
}