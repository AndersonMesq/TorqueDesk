# 🔧 TorqueDesk

API RESTful para gestão completa de oficinas mecânicas, com funcionalidades robustas de controle de serviços, agendamento, registro de clientes e gerenciamento de funcionários.

> ⚠️ **Status:** Em desenvolvimento.

---

## 📋 Sobre o Projeto

TorqueDesk é uma solução backend moderna construída com **Spring Boot 3.5.14** e **Java 21**, desenvolvida para automatizar e centralizar todas as operações de uma oficina mecânica.

### ✨ Funcionalidades

- Gestão de Clientes: Registro, atualização e gerenciamento de dados de clientes
- Gestão de Veículos: Cadastro e rastreamento de veículos dos clientes
- Controle de Serviços: Criação, acompanhamento e conclusão de ordens de serviço
- Agendamento: Sistema de agendamento para otimizar a agenda da oficina
- Gestão de Funcionários: Registro e gerenciamento de funcionários
- Autenticação e Autorização: Segurança com JWT e Spring Security
- Documentação API: Swagger/OpenAPI integrado
- Fila de Mensagens: Integração com RabbitMQ para processamento assíncrono
- Persistência: PostgreSQL com migrations automáticas via Flyway
- Cache: Redis para otimização de performance

---

## 🛠️ Stack Tecnológico

| Tecnologia | Versão | Propósito |
|-----------|--------|----------|
| Java | 21 | Linguagem principal |
| Spring Boot | 3.5.14 | Framework web |
| Spring Security | - | Autenticação e autorização |
| Spring Data JPA | - | Persistência de dados |
| PostgreSQL | - | Banco de dados relacional |
| Flyway | - | Versionamento de migrations |
| JWT | 0.12.7 | Token de autenticação |
| RabbitMQ | - | Message broker |
| Redis | - | Cache e sessões |
| MapStruct | 1.6.3 | Mapeamento de objetos |
| Lombok | 1.18.42 | Redução de boilerplate |
| SpringDoc OpenAPI | 2.8.16 | Documentação automática |
| Spring Boot Actuator | - | Monitoramento e métricas |