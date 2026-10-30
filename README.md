# TorqueDesk

**TorqueDesk** é uma API REST para gestão de oficinas mecânicas. O projeto busca reunir em um único sistema os dados e processos usados no dia a dia de uma oficina, como clientes, veículos, ordens de serviço, usuários e unidades de negócio.

Este é um projeto de portfólio e estudo em desenvolvimento contínuo. Seu objetivo é demonstrar, na prática, a construção de um backend com Java e Spring: modelagem por domínio, APIs REST, autenticação e autorização, persistência relacional, testes automatizados e evolução incremental de arquitetura.

> **Status:** backend em desenvolvimento. Os status das tecnologias e funcionalidades abaixo refletem o código e a configuração presentes no repositório.

**Legenda:** ✅ implementado; 🟡 dependência presente ou integração parcial; ⏳ planejado.

## Objetivos do projeto

- Centralizar operações essenciais de uma oficina em uma API.
- Separar responsabilidades por domínio em um monólito modular.
- Aplicar práticas de segurança, validação, persistência e tratamento de erros.
- Exercitar testes unitários e de integração em uma aplicação Spring.
- Evoluir gradualmente a solução, documentando o que já existe e o que ainda está planejado.

## Funcionalidades

| Domínio | Funcionalidades | Status |
|---|---|---|
| Administração | Provisionar tenants e administrar usuários | ✅ Implementado |
| Autenticação e segurança | Login, tokens JWT, seleção de workspace e controle de acesso | ✅ Implementado |
| Multi-tenancy | Gerenciar tenants, status e associação de usuários aos workspaces | ✅ Implementado |
| Clientes | Criar, atualizar, consultar e pesquisar clientes | ✅ Implementado |
| Veículos | Cadastrar, atualizar e consultar veículos associados aos clientes | ✅ Implementado |
| Ordens de serviço | Criar e consultar ordens, controlar seu ciclo de vida e gerenciar atribuições | ✅ Implementado |
| Agendamento | Agendar serviços e organizar a agenda da oficina | ⏳ Planejado |
| Estoque | Controlar peças e movimentações de estoque | ⏳ Planejado |
| Funcionários | Ampliar a gestão de funcionários e suas operações | ⏳ Planejado |
| Interface web | Criar uma interface para consumir a API | ⏳ Planejado; tecnologia ainda não definida |

## Stack tecnológica

### Implementada no backend

| Tecnologia | Uso no projeto | Status |
|---|---|---|
| Java 21 | Linguagem e plataforma da aplicação | ✅ Implementada |
| Spring Boot 3.5.14 | Configuração e execução do backend | ✅ Implementado |
| Spring Web / Spring MVC | Construção dos endpoints REST | ✅ Implementado |
| Spring Data JPA e Hibernate | Persistência e mapeamento objeto-relacional | ✅ Implementados |
| PostgreSQL | Banco de dados relacional | ✅ Implementado |
| Flyway | Versionamento e execução das migrations | ✅ Implementado |
| Spring Security | Autenticação e autorização dos endpoints | ✅ Implementado |
| JJWT | Criação e validação de tokens JWT | ✅ Implementado |
| Bean Validation | Validação de requisições | ✅ Implementada |
| Springdoc OpenAPI / Swagger UI | Documentação interativa da API | ✅ Implementados |
| Maven e Maven Wrapper | Gerenciamento de dependências e build | ✅ Implementados |
| JUnit Jupiter e Mockito | Testes unitários e suporte a testes | ✅ Implementados |
| Testcontainers PostgreSQL | Banco PostgreSQL isolado para testes de integração | ✅ Implementado |
| Lombok e MapStruct | Redução de código repetitivo e mapeamento de objetos | ✅ Implementados |

### Dependências presentes, integração funcional pendente

Estas bibliotecas já aparecem no `pom.xml`, mas a integração correspondente ainda não está implementada no fluxo da aplicação. A presença de uma dependência, por si só, não significa que a funcionalidade esteja pronta.

| Tecnologia | Uso previsto | Status |
|---|---|---|
| Spring Session Data Redis | Sessões e cache com Redis | 🟡 Dependência adicionada; integração pendente |
| Spring AMQP e RabbitMQ | Processamento assíncrono por mensagens | 🟡 Dependência adicionada; integração pendente |
| Spring Boot Actuator | Endpoints de saúde e métricas operacionais | 🟡 Dependência adicionada; configuração e uso operacional pendentes |
| Testcontainers RabbitMQ | Testes com broker RabbitMQ | 🟡 Dependência de teste adicionada; uso em testes pendente |
| Spring Boot Docker Compose | Integração do Spring Boot com serviços locais via Compose | 🟡 Dependência adicionada; arquivo Compose ainda não configurado |

### Planejada para evoluções futuras

| Tecnologia / frente | Objetivo | Status |
|---|---|---|
| Docker | Containerizar a aplicação e seus serviços de apoio | ⏳ Planejado |
| OpenTelemetry | Ampliar a instrumentação e a observabilidade | ⏳ Planejado |
| CI/CD | Automatizar build, testes, análise de qualidade e deploy | ⏳ Planejado |
| Frontend | Criar uma aplicação web para consumir a API | ⏳ Planejado; stack ainda não definida |

## Arquitetura e qualidade

A aplicação segue uma **arquitetura de monólito modular**, organizada por domínio. Os módulos atuais incluem `admin`, `authentication`, `security`, `customer`, `vehicle`, `service_order`, `user`, `tenant` e `usertenant`.

Há testes unitários de serviços e testes de integração organizados por API, serviço e repositório. As classes de integração e o suporte a Testcontainers estão no repositório; a execução automatizada de toda a suíte de integração e a ampliação da cobertura continuam como pontos de evolução.

## Próximos passos

- Expandir os fluxos de ordens de serviço e a gestão de funcionários.
- Desenvolver os módulos de agendamento e estoque.
- Configurar Docker e Docker Compose para execução local reproduzível.
- Integrar Redis e RabbitMQ a casos de uso concretos.
- Configurar observabilidade com Actuator e OpenTelemetry.
- Automatizar build, testes e análise de qualidade em CI/CD.
- Desenvolver um frontend, definindo sua stack quando essa etapa começar.
- Evoluir a documentação da API, as validações e a cobertura de testes.

## Como executar

### Pré-requisitos

- Java 21
- PostgreSQL
- Maven (opcional, o repositório inclui o Maven Wrapper)

Crie um banco PostgreSQL para a aplicação e configure as propriedades de conexão e a chave JWT no ambiente ou na configuração local. Não use credenciais reais em arquivos versionados. As migrations do schema são gerenciadas pelo Flyway.

### Clonar e iniciar

```bash
git clone https://github.com/AndersonMesq/TorqueDesk.git
cd TorqueDesk
```

No macOS/Linux:

```bash
./mvnw spring-boot:run
```

No Windows:

```bat
mvnw.cmd spring-boot:run
```

### Executar os testes unitários

No macOS/Linux:

```bash
./mvnw test
```

No Windows:

```bat
mvnw.cmd test
```

Os testes de integração que usam Testcontainers requerem um ambiente compatível com Docker; a configuração do ciclo de execução automatizada dessa suíte ainda está em evolução.

## Documentação da API

Com a aplicação em execução, a interface Swagger UI fica disponível em:

```text
http://localhost:8080/swagger-ui.html
```
