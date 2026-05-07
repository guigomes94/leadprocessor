
# Lead Processor 🚀
___

Sistema de alto desempenho para processamento assíncrono de arquivos CSV. O projeto utiliza **Java 21** com **Virtual Threads** para I/O eficiente e **Apache Kafka** para mensageria distribuída.

## 🏗️ Estrutura do Projeto

O repositório está dividido em dois módulos principais:

* **`/leadprocessor`**: Backend construído com Spring Boot 3.4+, Java 21, Spring Data JPA e Kafka.
* **`/lead-processor-front`**: Frontend SPA (Single Page Application) utilizando React, Vite, Tailwind CSS v4 e Lucide Icons.

### Arquitetura de Processamento
1.  **API**: Recebe o CSV, valida o encoding (UTF-8) e gera um ID de Lote.
2.  **Kafka**: O arquivo é particionado em *chunks* e enviado para um tópico do Kafka.
3.  **Consumer**: Utiliza **Virtual Threads** (Project Loom) para processar os registros em paralelo e persistir no PostgreSQL, garantindo que o banco não seja sobrecarregado e mantendo a reatividade do sistema.



## 🛠️ Tecnologias Utilizadas

* **Backend:** Java 21, Spring Boot 3, Spring Data JPA, Hibernate, PostgreSQL, Flyway (Migrations).
* **Mensageria:** Apache Kafka (Modo KRaft).
* **Frontend:** React 18, TypeScript, Vite, Tailwind CSS v4, Axios, React Dropzone.
* **DevOps:** Docker, Docker Compose.
* **Documentação:** OpenAPI 3 (Swagger).

## ⚙️ Configurações Principais

As configurações de ambiente estão centralizadas no arquivo `application.properties` e são sobrescritas pelo `docker-compose.yml` para garantir a comunicação entre os containers.

* **Chunk Size:** O sistema processa lotes de 1000 registros por vez (configurável via `app.lote.chunk-size`).
* **Encoding:** Validação rigorosa de arquivos UTF-8 no upload.
* **Database:** PostgreSQL na porta `5432` com persistência em volume Docker.

## 🚀 Como Rodar a Aplicação

Certifica-te de que tens o **Docker** e o **Docker Compose** instalados na tua máquina.

1.  Navega até a pasta raiz do projeto backend:
    ```bash
    cd leadprocessor
    ```

2.  Executa o comando para subir todo o ecossistema (Banco, Kafka, API e Front):
    ```bash
    docker compose up -d --build
    ```

3.  Aguarde o build das imagens (Maven e Node.js). Após a conclusão, a aplicação estará disponível em:
    * **Frontend:** [http://localhost:3000](http://localhost:3000)
    * **Backend API:** [http://localhost:8080](http://localhost:8080)

## 📖 Documentação da API (Swagger)

A documentação interativa da API foi configurada para ser acessada de forma isolada:

* **Interface Visual (Swagger UI):**
  [http://localhost:8080/leadprocessor/swagger-ui.html](http://localhost:8080/leadprocessor/swagger-ui.html)

* **Especificação Técnica (OpenAPI JSON):**
  [http://localhost:8080/leadprocessor/v3/api-docs](http://localhost:8080/leadprocessor/v3/api-docs)

---

### Endpoints Disponíveis
* `POST /api/lotes`: Upload do ficheiro CSV.
* `GET /api/lotes/{id}/status`: Consulta do progresso do lote em tempo real.
* `GET /api/lotes/stats/global`: Estatísticas gerais (total de leads, lotes e taxa de erro).
* `GET /api/leads`: Listagem paginada de leads com filtros por nome, e-mail e origem.