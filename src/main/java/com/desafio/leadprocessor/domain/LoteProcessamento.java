package com.desafio.leadprocessor.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "lotes_processamento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoteProcessamento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Relação 1 para 1 com o Lote. Lazy loading por padrão para otimizar memória.
    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id", nullable = false, unique = true)
    private Lote lote;

    @Builder.Default
    @Column(name = "total_linhas", nullable = false)
    private Integer totalLinhas = 0;

    @Builder.Default
    @Column(name = "linhas_sucesso", nullable = false)
    private Integer linhasSucesso = 0;

    @Builder.Default
    @Column(name = "linhas_erro", nullable = false)
    private Integer linhasErro = 0;

    @Builder.Default
    @Column(name = "tempo_processamento_ms", nullable = false)
    private Long tempoProcessamentoMs = 0L;

    @UpdateTimestamp
    @Column(name = "data_atualizacao")
    private Instant dataAtualizacao;
}