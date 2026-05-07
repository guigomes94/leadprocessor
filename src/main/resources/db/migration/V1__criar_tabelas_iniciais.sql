-- V1__criar_tabelas_iniciais.sql

-- 1. Tabela Lote
-- Usaremos UUID para o ID do Lote. É uma boa prática para APIs REST,
-- pois gera um identificador único e não sequencial para o frontend consultar o status.
CREATE TABLE lotes
(
    id           UUID PRIMARY KEY,
    nome_arquivo VARCHAR(255) NOT NULL,
    status       VARCHAR(50)  NOT NULL, -- PENDENTE, PROCESSANDO, CONCLUIDO, ERRO
    data_criacao TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. Tabela LoteProcessamento
-- Conforme o requisito[cite: 14, 19], esta tabela guarda as estatísticas.
-- Ela tem uma relação de 1 para 1 com a tabela lote.
CREATE TABLE lotes_processamento
(
    id                     UUID PRIMARY KEY,
    lote_id                UUID    NOT NULL UNIQUE REFERENCES lotes (id) ON DELETE CASCADE,
    total_linhas           INTEGER NOT NULL         DEFAULT 0,
    linhas_sucesso         INTEGER NOT NULL         DEFAULT 0,
    linhas_erro            INTEGER NOT NULL         DEFAULT 0,
    tempo_processamento_ms BIGINT  NOT NULL         DEFAULT 0,
    data_atualizacao       TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 3. Tabela Lead
-- Conforme as colunas esperadas do CSV [cite: 11] e a entidade[cite: 19].
CREATE TABLE leads
(
    id            UUID PRIMARY KEY,
    lote_id       UUID         NOT NULL REFERENCES lotes (id) ON DELETE CASCADE,
    nome          VARCHAR(255) NOT NULL,
    email         VARCHAR(255) NOT NULL,
    telefone      VARCHAR(50),
    origem        VARCHAR(100),
    data_cadastro TIMESTAMP WITH TIME ZONE,

    -- REQUISITO CRÍTICO: "Controlar concorrência sem duplicatas no banco"
    -- Essa constraint de unicidade no e-mail garantirá que o banco rejeite
    -- inserções duplicadas, mesmo que múltiplas threads tentem salvar ao mesmo tempo.
    CONSTRAINT uk_lead_email UNIQUE (email)
);

-- Índices para otimizar os endpoints de listagem e busca
-- O desafio pede um "Endpoint paginado com filtro por nome/email/origem" [cite: 20]
CREATE INDEX idx_leads_email ON leads (email);
CREATE INDEX idx_leads_nome ON leads (nome);
CREATE INDEX idx_leads_origem ON leads (origem);