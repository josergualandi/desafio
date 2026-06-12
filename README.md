# Backend Java - Portfólio de Projetos

API Spring Boot para o desafio técnico de gerenciamento de portfólio de projetos.

## Sumário

1. [Visão geral](#visão-geral)
2. [Stack e arquitetura](#stack-e-arquitetura)
3. [Requisitos atendidos](#requisitos-atendidos)
4. [Execução rápida (1 comando)](#execução-rápida-1-comando)
5. [Configuração de banco e bootstrap automático](#configuração-de-banco-e-bootstrap-automático)
6. [Como executar em modos diferentes](#como-executar-em-modos-diferentes)
7. [Autenticação](#autenticação)
8. [Swagger](#swagger)
9. [Endpoints](#endpoints)
10. [Exemplos de payload](#exemplos-de-payload)
11. [Testes e cobertura](#testes-e-cobertura)
12. [Troubleshooting](#troubleshooting)

## Visão geral

O projeto implementa:

- CRUD completo de projetos
- Regras de negócio de transição de status
- Cálculo dinâmico de risco
- Regras de alocação de membros
- Relatório resumido do portfólio
- API mockada para cadastro e consulta de membros externos
- Segurança com Basic Auth
- Documentação com OpenAPI/Swagger
- Testes unitários e validação de cobertura

## Stack e arquitetura

- Java 21
- Spring Boot 3.3.5
- Spring Web
- Spring Data JPA + Hibernate
- Spring Security
- PostgreSQL
- OpenAPI (springdoc)
- JUnit + Mockito + JaCoCo

Estrutura por camadas:

- Controller: entrada/saída HTTP
- Service: regras de negócio
- Repository: acesso a dados
- DTO/Mapper: contrato e mapeamento

## Requisitos atendidos

### Projetos

- Campos completos de projeto
- CRUD
- Filtros por nome e status
- Paginação padrão Spring

### Regras de negócio

- Risco dinâmico:
	- Baixo: orçamento <= 100000 e prazo <= 3 meses
	- Médio: demais casos intermediários
	- Alto: orçamento > 500000 ou prazo > 6 meses
- Transição de status em sequência lógica
- Cancelamento permitido fora da sequência linear
- Bloqueio de exclusão para status iniciado, em andamento e encerrado
- Alocação:
	- Somente membros com atribuição FUNCIONARIO
	- 1 a 10 membros por projeto
	- Máximo de 3 projetos ativos por membro

### Relatório

Endpoint de resumo com:

- Quantidade de projetos por status
- Total orçado por status
- Média de duração dos encerrados
- Total de membros únicos alocados

### Infra e qualidade

- Swagger/OpenAPI
- Tratamento global de exceções
- Segurança com usuário/senha em memória
- Testes unitários
- JaCoCo com check mínimo no pacote de regras de negócio

## Execução rápida (1 comando)

Na pasta do backend:

```powershell
.\start-local.ps1
```

Esse script automatiza:

1. Sobe/garante container PostgreSQL local em 5433
2. Cria o banco desafio_db se não existir
3. Garante schema principal (tabelas e FKs)
4. Builda o backend
5. Sobe a aplicação

## Configuração de banco e bootstrap automático

Arquivo de configuração padrão: `src/main/resources/application.yml`

Defaults locais:

- URL: `jdbc:postgresql://localhost:5433/desafio_db`
- Usuário: `ceiuser`
- Senha: `ceipass`

Bootstrap automático no startup:

- Classe: `src/main/java/br/com/desafio/app/config/DataSourceConfig.java`
- Comportamento:
	- Se a URL for PostgreSQL, tenta criar o banco informado na URL
	- Em seguida, o Hibernate (`ddl-auto: update`) ajusta entidades/tabelas

Bootstrap adicional por script:

- Script: `start-local.ps1`
- Schema base: `schema-postgres.sql`

## Como executar em modos diferentes

### 1) Fluxo recomendado

```powershell
.\start-local.ps1
```

### 2) Apenas garantir banco e tabelas (sem subir API)

```powershell
.\start-local.ps1 -OnlyDatabase
```

### 3) Rodar jar manualmente

```powershell
java -jar target/backend-java-0.0.1-SNAPSHOT.jar
```

### 4) Apontar para outro PostgreSQL

```powershell
$env:SPRING_DATASOURCE_URL='jdbc:postgresql://localhost:5432/desafio_db'
$env:SPRING_DATASOURCE_USERNAME='ceiuser'
$env:SPRING_DATASOURCE_PASSWORD='ceipass'
java -jar target/backend-java-0.0.1-SNAPSHOT.jar
```

## Autenticação

Basic Auth configurado em memória:

- Usuário: `admin`
- Senha: `admin123`

## Swagger

- URL: `http://localhost:8082/swagger-ui/index.html`
- OpenAPI docs: `http://localhost:8082/v3/api-docs`

## Endpoints

### Membros externos (mock)

- `POST /api/externo/membros`
- `GET /api/externo/membros`
- `GET /api/externo/membros/{id}`

### Projetos

- `POST /api/projetos`
- `GET /api/projetos`
- `GET /api/projetos/{id}`
- `PUT /api/projetos/{id}`
- `DELETE /api/projetos/{id}`
- `GET /api/projetos/relatorio/resumo`

Parâmetros da listagem de projetos:

- `nome`
- `status`
- `page`, `size`, `sort`

## Exemplos de payload

### Criar membro gerente

```json
{
	"nome": "Maria Gerente",
	"atribuicao": "GERENTE"
}
```

### Criar membro funcionário

```json
{
	"nome": "João Dev",
	"atribuicao": "FUNCIONARIO"
}
```

### Criar projeto

```json
{
	"nome": "Projeto XPTO",
	"dataInicio": "2026-06-11",
	"previsaoTermino": "2026-09-11",
	"dataRealTermino": null,
	"orcamentoTotal": 150000.00,
	"descricao": "Projeto para validar o desafio",
	"gerenteResponsavelId": 1,
	"statusAtual": "EM_ANALISE",
	"membrosIds": [2]
}
```

## Testes e cobertura

Rodar testes:

```powershell
mvn test
```

Rodar testes + gerar relatório de cobertura JaCoCo:

```powershell
mvn test jacoco:report
```

Rodar validação completa (inclui check de cobertura):

```powershell
mvn verify
```

Relatório JaCoCo:

- `target/site/jacoco/index.html`

Como abrir o relatório no Windows (a partir da pasta `backend-java`):

```powershell
start .\target\site\jacoco\index.html
```

Cobertura da última execução local:

- Instruções: 79.33%
- Linhas: 79.34%

## Troubleshooting

### Erro ao subir com maven-clean-plugin dizendo que não consegue deletar jar

Causa comum: já existe um processo Java rodando com o jar aberto.

Solução:

1. Finalize o processo Java da API
2. Rode novamente `start-local.ps1`

### Conflito de porta 5432 no Windows

Causa comum: PostgreSQL do Windows e PostgreSQL Docker ao mesmo tempo.

Solução aplicada neste projeto:

- usar default na porta 5433 para o Docker local

### start-local.ps1 funcionou antes e depois parou

Verifique:

1. Docker Desktop está em execução
2. Container PostgreSQL está ativo
3. Não há API anterior rodando e travando o jar

### Quero confirmar que banco e tabelas foram criados

Exemplo com Docker:

```powershell
docker exec -e PGPASSWORD=ceipass desafio-pg-5433 psql -U ceiuser -d postgres -tAc "SELECT datname FROM pg_database ORDER BY datname"
docker exec -e PGPASSWORD=ceipass desafio-pg-5433 psql -U ceiuser -d desafio_db -tAc "SELECT table_name FROM information_schema.tables WHERE table_schema='public' ORDER BY table_name"
```

Resultado esperado para tabelas principais:

- `membros`
- `projeto_membros`
- `projetos`