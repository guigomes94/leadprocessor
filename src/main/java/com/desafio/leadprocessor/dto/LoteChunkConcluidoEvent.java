package com.desafio.leadprocessor.dto;

import java.util.UUID;

public record LoteChunkConcluidoEvent(
        UUID loteId,
        int linhasSucesso,
        int linhasErro,
        long tempoProcessamentoMs
) {}
