
# DevTask

Aplicação web full stack para gerenciamento de tarefas, desenvolvida com Angular, Spring Boot e PostgreSQL.

O DevTask é um projeto pessoal de portfólio criado para aplicar conhecimentos de desenvolvimento web, APIs REST, banco de dados relacional, autenticação, autorização, testes e segurança da informação.

A aplicação permite que cada usuário organize suas próprias tarefas e acompanhe seu progresso em um dashboard.

## Demonstração

### Dashboard

![Dashboard do DevTask](docs/images/dashboard.png)

### Login

![Tela de login do DevTask](docs/images/login.png)

### Cadastro

![Tela de cadastro do DevTask](docs/images/cadastro.png)

As capturas representam o sistema executado em ambiente local.

## Tecnologias

| Camada | Tecnologias |
|---|---|
| Frontend | Angular 22, TypeScript 6, HTML5, CSS3 e RxJS |
| Backend | Java 17, Spring Boot 4.1.2 e Maven |
| API | Spring Web MVC e Springdoc OpenAPI |
| Segurança | Spring Security e proteção CSRF |
| Persistência | Spring Data JPA e Hibernate |
| Banco de dados | PostgreSQL 18 |
| Testes | JUnit e ferramentas de teste do Spring |

## Funcionalidades

### Usuários e autenticação

- Cadastro de contas
- Login e logout
- Gerenciamento de sessões
- Proteção de rotas e operações
- Separação dos dados por usuário

### Gerenciamento de tarefas

- Cadastro de tarefas
- Listagem e consulta
- Edição de título, descrição, prioridade e status
- Exclusão com confirmação
- Prioridades baixa, média e alta
- Status pendente, em andamento e concluída
- Filtros por status e prioridade

### Dashboard

- Total de tarefas
- Total de tarefas pendentes
- Total de tarefas em andamento
- Total de tarefas concluídas
- Percentual geral de conclusão
- Atualização da listagem

### Interface

- Tema escuro
- Layout responsivo
- Formulários com validação
- Mensagens de erro e sucesso
- Navegação por teclado
- Gerenciamento de foco em modais
- Melhorias de acessibilidade para leitores de tela

## Arquitetura

O frontend Angular apresenta a interface e realiza requisições HTTP ao backend.

O backend Spring Boot implementa as regras de negócio, autenticação, autorização e acesso ao banco.

```text
Usuário
   |
   v
Angular (localhost:4200)
   |
   | HTTP
   v
Spring Boot (localhost:8080)
   |
   | Spring Data JPA / Hibernate
   v
PostgreSQL (localhost:5433)
```

## Estrutura do projeto

```text
devtask/
|
|-- .github/
|
|-- backend/
|   |-- database/
|   |   |-- 001_create_tarefas.sql
|   |   |-- 002_create_usuarios.sql
|   |   |-- 003_adicionar_proprietario_tarefa.sql
|   |   |-- 004_usuario_tarefa_obrigatorio.sql
|   |
|   |-- src/
|   |   |-- main/
|   |   |-- test/
|   |
|   |-- pom.xml
|
|-- frontend/
|   |-- src/
|   |-- package.json
|   |-- proxy.conf.json
|
|-- docs/
|   |-- images/
|       |-- login.png
|       |-- cadastro.png
|       |-- dashboard.png
|
|-- .gitignore
|-- README.md
```

## Segurança

O DevTask implementa controles de segurança, incluindo:

- Autenticação com Spring Security
- Proteção contra CSRF
- Cookies de sessão com HttpOnly
- Configuração SameSite para cookies
- Suporte à configuração Secure para cookies
- Autorização para acessar tarefas do próprio usuário
- Validação de dados recebidos pela API
- Limitação de tentativas de autenticação
- Bloqueio temporário com resposta HTTP 429
- Cabeçalho Retry-After para tentativas bloqueadas
- Senha de conexão com o banco fornecida por variável de ambiente

O controle de tentativas de login é mantido em memória. Para execução com múltiplas instâncias, essa abordagem precisaria ser revista.

A aplicação foi desenvolvida e validada localmente. A implementação desses controles não equivale a uma auditoria ou certificação de segurança.

## Requisitos

- Java 17
- Maven
- Node.js compatível com Angular 22
- npm
- PostgreSQL 18
- Git

Os comandos apresentados a seguir utilizam Windows e PowerShell.

## Executando localmente

### 1. Clonar o repositório

```powershell
git clone https://github.com/fernandoaugustobueno10-wq/devtask.git

cd devtask
```

### 2. Preparar o PostgreSQL

A configuração local utilizada no desenvolvimento é:

| Propriedade | Valor |
|---|---|
| Host | localhost |
| Porta | 5433 |
| Banco | devtask_db |
| Usuário da aplicação | devtask_app |

É necessário criar o banco, configurar o usuário e conceder as permissões apropriadas antes de iniciar o backend.

Não há credenciais de banco incluídas no repositório.

### 3. Criar o esquema

O repositório possui quatro scripts SQL na pasta `backend/database`.

Execute-os na ordem numérica em um banco novo, utilizando uma conta com permissões para criar e alterar tabelas:

```powershell
psql -X -v ON_ERROR_STOP=1 -h localhost -p 5433 -U devtask_app -d devtask_db -f backend/database/001_create_tarefas.sql

psql -X -v ON_ERROR_STOP=1 -h localhost -p 5433 -U devtask_app -d devtask_db -f backend/database/002_create_usuarios.sql

psql -X -v ON_ERROR_STOP=1 -h localhost -p 5433 -U devtask_app -d devtask_db -f backend/database/003_adicionar_proprietario_tarefa.sql

psql -X -v ON_ERROR_STOP=1 -h localhost -p 5433 -U devtask_app -d devtask_db -f backend/database/004_usuario_tarefa_obrigatorio.sql
```

Os comandos pressupõem que `psql` esteja instalado e acessível no PATH e que `devtask_app` possua permissões suficientes. Caso contrário, use uma conta administradora ou proprietária do esquema para a inicialização.

Esses scripts foram revisados quanto à sequência, mas a instalação completa em um banco vazio ainda precisa ser validada separadamente.

**Atenção:** não execute os scripts indiscriminadamente sobre um banco existente com dados de produção. O quarto script exige que todas as tarefas já possuam proprietário.

### 4. Configurar a senha do banco

No PowerShell em que o backend será iniciado:

```powershell
$senhaSegura = Read-Host "Senha do PostgreSQL DevTask" -AsSecureString

try {
    $env:DB_PASSWORD = [System.Net.NetworkCredential]::new(
        "",
        $senhaSegura
    ).Password
}
finally {
    $senhaSegura.Dispose()
    Remove-Variable senhaSegura -ErrorAction SilentlyContinue
}
```

A variável `DB_PASSWORD` é utilizada pela configuração do Spring Boot.

### 5. Iniciar o backend

Na raiz do repositório:

```powershell
mvn -f backend/pom.xml spring-boot:run
```

Backend:

http://localhost:8080

A aplicação utiliza `spring.jpa.hibernate.ddl-auto=validate`, portanto valida o esquema existente em vez de criá-lo automaticamente.

### 6. Instalar as dependências do frontend

Em outro terminal:

```powershell
cd frontend

npm install
```

### 7. Iniciar o Angular

Na pasta `frontend`:

```powershell
npm start -- --proxy-config proxy.conf.json
```

Acesse:

http://localhost:4200

Com o PostgreSQL, o backend e o frontend iniciados, é possível cadastrar uma conta e utilizar o dashboard.

## Testes

O backend contém testes automatizados para regras de negócio, acesso a dados, autenticação, autorização e mecanismos de segurança.

A suíte possui 103 testes automatizados, aprovados na validação da Etapa 39.

### Executar testes do backend

Na raiz do projeto:

```powershell
mvn -f backend/pom.xml clean test
```

Alguns testes de integração exigem que o PostgreSQL esteja iniciado e a variável `DB_PASSWORD` esteja configurada.

### Verificar a compilação do frontend

```powershell
npm --prefix frontend run build
```

### Validações manuais realizadas

Foram verificados fluxos de cadastro, login, CRUD de tarefas, persistência, logout e isolamento de dados entre usuários.

Também foram aprovadas verificações manuais de responsividade e navegação por teclado, incluindo operações com modais.

Esses testes não substituem uma auditoria formal de acessibilidade ou segurança.

## Documentação da API

O projeto utiliza Springdoc OpenAPI e Swagger UI.

Quando habilitada e permitida pelas regras de segurança, a interface Swagger pode ser acessada em:

http://localhost:8080/swagger-ui/index.html

A exposição da documentação deve ser avaliada antes de disponibilizar a aplicação publicamente.

## Melhorias futuras

- Automatizar as migrações do banco com uma ferramenta de versionamento
- Adicionar testes automatizados de interface
- Ampliar as verificações de acessibilidade
- Centralizar o controle de tentativas de login para ambientes distribuídos
- Implementar monitoramento e observabilidade
- Preparar a aplicação para hospedagem com HTTPS
- Publicar uma demonstração online

## Sobre o projeto

O DevTask foi criado como projeto pessoal de estudo e portfólio, com foco na aplicação prática de conhecimentos de desenvolvimento full stack.

O desenvolvimento envolve frontend, backend, persistência relacional, segurança de aplicações web e testes automatizados.

## Autor

**Fernando Augusto Bueno Canquerini**

Estudante de Tecnologia em Análise e Desenvolvimento de Sistemas — Universidade Federal do Paraná (UFPR).

GitHub: https://github.com/fernandoaugustobueno10-wq

---

Projeto pessoal desenvolvido para fins de aprendizado e portfólio.
