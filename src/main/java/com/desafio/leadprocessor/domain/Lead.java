package com.desafio.leadprocessor.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "leads")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lead {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // Muitos Leads pertencem a um Lote. Usamos LAZY para não carregar o Lote à toa.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id", nullable = false)
    private Lote lote;

    @Column(nullable = false)
    private String nome;

    // A unique constraint já está no banco, mas declaramos aqui também.
    @Column(nullable = false, unique = true)
    private String email;

    private String telefone;

    private String origem;

    @Column(name = "data_cadastro")
    private Instant dataCadastro;
}