# 🔧 TorqueDesk

**TorqueDesk** é uma API RESTful para gestão de oficinas mecânicas, desenvolvida para centralizar o gerenciamento de clientes, veículos, ordens de serviço, funcionários e operações da oficina.

O projeto está sendo desenvolvido com **Java 21** e **Spring Boot**, utilizando uma arquitetura monolítica modular e aplicando princípios de Engenharia de Software, segurança, persistência de dados e testes automatizados.

> 🚧 **Status:** Em desenvolvimento

---

## 📋 Sobre o Projeto

O TorqueDesk surgiu com o objetivo de desenvolver uma solução backend para oficinas mecânicas, permitindo centralizar informações e processos que normalmente são administrados de forma descentralizada.

A aplicação está sendo construída com foco em:

- Separação de responsabilidades
- Baixo acoplamento entre módulos
- Segurança e controle de acesso
- Persistência consistente dos dados
- Testabilidade
- Evolução incremental da aplicação

O projeto também serve como ambiente prático para aplicação de conceitos de **desenvolvimento backend com Java e Spring**, incluindo desenvolvimento de APIs REST, autenticação, persistência, validação e testes automatizados.

---

## ✨ Principais Funcionalidades

### 👤 Gestão de Usuários

- Cadastro e gerenciamento de usuários
- Controle de papéis e permissões
- Autenticação baseada em JWT
- Controle de acesso utilizando Spring Security
- Associação de usuários a tenants

### 🏢 Multi-tenancy

- Criação e gerenciamento de tenants
- Isolamento lógico dos dados por tenant
- Controle de status dos tenants
- Provisionamento inicial de recursos

### 👥 Gestão de Clientes

- Cadastro de clientes
- Atualização de dados
- Consulta por identificador
- Busca por e-mail
- Paginação e consultas estruturadas

### 🚗 Gestão de Veículos

- Cadastro de veículos
- Associação de veículos aos clientes
- Consulta e gerenciamento de informações dos veículos

### 🔧 Ordens de Serviço

- Criação e gerenciamento de ordens de serviço
- Associação entre clientes, veículos e serviços
- Controle do ciclo de vida das ordens
- Associação de funcionários às ordens de serviço

### 📚 Documentação da API

- Documentação utilizando OpenAPI
- Interface interativa através do Swagger UI
- Possibilidade de testar os endpoints diretamente pela documentação

---

## 📊 Status do Desenvolvimento

### Código de produção

| Módulo | Status |
|---|---|
| `admin` | ✅ Implementado |
| `authentication` | ✅ Implementado |
| `security` | ✅ Implementado |
| `customer` | ✅ Implementado |
| `vehicle` | ✅ Implementado |
| `service_order` | ✅ Implementado |
| `user` | ✅ Implementado |
| `tenant` | ✅ Implementado |
| `usertenant` | ✅ Implementado |

### Testes

| Módulo | Teste | Status |
|---|---|---|
| `admin.service` | `AdminTenantServiceTest` | ✅ Implementado |
| `admin.service` | `AdminUserServiceTest` | ✅ Implementado |
| `admin.service` | `TenantProvisioningServiceTest` | ✅ Implementado |
| `authentication.service` | `AuthenticationServiceTest` | 🚧 Em desenvolvimento |
| `customer.service` | `CustomerServiceTest` | ✅ Implementado |

**Legenda:**

- ✅ Implementado
- 🚧 Em desenvolvimento
- 📋 Planejado

---

## 🚧 Próximos Passos

O desenvolvimento do TorqueDesk seguirá uma evolução incremental.

### 🧪 Testes e Qualidade

- Finalizar os testes do módulo de autenticação
- Expandir a cobertura dos serviços existentes
- Adicionar testes de integração
- Avaliar cobertura de código e pontos críticos

### ⚙️ Funcionalidades

- Expandir o módulo de ordens de serviço
- Implementar módulo de agendamento
- Implementar gerenciamento de estoque
- Expandir o gerenciamento de funcionários
- Implementar novos fluxos relacionados às operações da oficina

### 🏗️ Infraestrutura

- Containerizar a aplicação utilizando Docker
- Implementar cache utilizando Redis
- Integrar RabbitMQ para processamento assíncrono
- Melhorar observabilidade utilizando Actuator e OpenTelemetry

### 🔄 CI/CD

- Criar pipeline de CI
- Automatizar execução dos testes a cada alteração
- Automatizar build da aplicação
- Integrar análise de qualidade de código
- Criar pipeline de CD
- Automatizar geração e publicação da imagem Docker
- Automatizar deploy da aplicação

### 🚀 Evolução da Aplicação

- Revisar e aprimorar a arquitetura dos módulos
- Melhorar documentação da API
- Expandir validações e tratamento de erros
- Desenvolver frontend para consumo da API

---

## ▶️ Como Executar

### Pré-requisitos

- Java 21
- Maven
- PostgreSQL

### Clone o projeto

```bash
git clone https://github.com/AndersonMesq/TorqueDesk.git
cd TorqueDesk
```

### Configure o banco de dados


- Crie um banco PostgreSQL para o projeto e configure as propriedades de conexão da aplicação.
- As migrations do banco são gerenciadas automaticamente pelo Flyway.

### Execute a aplicação
**Utilizando o Maven Wrapper:**
```bash
./mvnw spring-boot:run
```
**No Windows:**
```bash
mvnw.cmd spring-boot:run
```

### Executando os Testes

**Para executar todos os testes:**
```bash
./mvnw test
```
**No Windows:**
```bash
mvnw.cmd test
```
---

## 🏗️ Arquitetura

O TorqueDesk utiliza uma **arquitetura monolítica modular**, organizada por domínio/feature.

A estrutura busca manter as responsabilidades separadas e permitir que os diferentes módulos evoluam de forma independente dentro da mesma aplicação.

Exemplo simplificado:

```text
src/main/java/com/andersonmesq/TorqueDesk

├── admin/
├── authentication/
├── customer/
├── security/
├── service_order/
├── tenant/
├── user/
├── usertenant/
└── vehicle/