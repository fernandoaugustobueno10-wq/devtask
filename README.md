
# DevTask

**Aplicação web full-stack para gerenciamento de tarefas, desenvolvida com Angular, Spring Boot e PostgreSQL.**

O DevTask é um projeto pessoal de portfólio criado para praticar desenvolvimento web, APIs REST, persistência relacional, autenticação, autorização, testes automatizados e segurança de aplicações. Cada usuário pode gerenciar suas próprias tarefas e acompanhar o progresso em um dashboard.

> **Disponibilidade:** o projeto é apresentado no GitHub e pode ser executado localmente. A demonstração anteriormente hospedada no Netlify e na Railway foi desativada para evitar custos recorrentes. Não é necessário contratar hospedagem para avaliar ou executar o código.

## Demonstração por imagens

### Dashboard

![Dashboard do DevTask](docs/images/dashboard.png)

### Login

![Tela de login](docs/images/login.png)

### Cadastro

![Tela de cadastro](docs/images/cadastro.png)

As imagens registram a interface durante o desenvolvimento. Para interagir com o sistema, siga as instruções de execução local abaixo.

## Funcionalidades

- Cadastro de usuários, login e logout com sessões HTTP.
- Gerenciamento de tarefas restrito ao usuário autenticado.
- Criação, listagem, edição, exclusão e consulta de tarefas.
- Prioridades baixa, média e alta; status pendente, em andamento e concluída.
- Filtros por prioridade e status, paginação e ordenação.
- Dashboard com totais por status e percentual de conclusão.
- Interface responsiva com tema escuro, validações e melhorias de acessibilidade.

## Tecnologias

| Camada | Tecnologias |
|---|---|
| Frontend | Angular 22, TypeScript 6, HTML5, CSS3 e RxJS |
| Backend | Java 17, Spring Boot 4.1.2 e Maven |
| API | Spring Web MVC e Springdoc OpenAPI |
| Segurança | Spring Security, BCrypt, proteção CSRF e sessões HTTP |
| Persistência | Spring Data JPA e Hibernate |
| Banco | PostgreSQL 18 |
| Migrações | Flyway |
| Testes | JUnit e ferramentas de teste do Spring |
| Versionamento | Git e GitHub |

## Arquitetura local

```text
Navegador
   |
   v
Angular (http://localhost:4200)
   |
   | /api/* pelo proxy de desenvolvimento
   v
Spring Boot (http://localhost:8080)
   |
   | JDBC / Spring Data JPA
   v
PostgreSQL (local; porta configurável)
```

O frontend acessa a API por caminhos relativos `/api/...`. O proxy de desenvolvimento definido em `frontend/proxy.conf.json` encaminha as requisições ao backend local. O backend acessa um banco PostgreSQL da própria máquina.

## Como executar no Windows (PowerShell)

### 1. Pré-requisitos

Instale **Git, JDK 17, Maven, Node.js/npm compatíveis com Angular 22 e PostgreSQL 18**. Os comandos abaixo pressupõem que `git`, `java`, `mvn`, `node`, `npm` e `psql` estejam acessíveis pelo terminal; se `psql` não estiver no `PATH`, utilize o executável da instalação do PostgreSQL.

Não é necessário Docker, WSL, Railway ou Netlify.

Confira as ferramentas:

```powershell
git --version
java -version
mvn -version
node --version
npm --version
psql --version
```

### Inicialização simplificada

Após instalar os pré-requisitos e preparar o PostgreSQL, o DevTask pode ser iniciado utilizando os scripts da pasta `scripts`.

**Terminal 1 — Backend Spring Boot**

Na raiz do repositório:

```powershell
.\scripts\iniciar-backend.ps1
```

O script solicita a senha do PostgreSQL local e inicia o backend na porta `8080`.

**Terminal 2 — Frontend Angular**

Em outro PowerShell:

```powershell
.\scripts\iniciar-frontend.ps1
```

O script verifica as dependências do frontend, instala os pacotes quando necessário e inicia o Angular.

**Acesse no navegador:**

http://localhost:4200

Os scripts utilizam, por padrão, o PostgreSQL local na porta `5433`, com banco `devtask_db` e usuário `devtask_app`. Caso utilize outra configuração, ajuste as variáveis no script de inicialização do backend.

Não é necessário utilizar Docker, WSL, Railway ou Netlify.

Para encerrar o sistema, pressione `Ctrl+C` em cada terminal.


### 2. Clonar o repositório

```powershell
git clone https://github.com/fernandoaugustobueno10-wq/devtask.git
cd devtask
```

### 3. Criar um banco PostgreSQL vazio

Para uma **instalação nova**, conecte-se como administrador ao PostgreSQL local. O exemplo utiliza a porta padrão `5432`; substitua pela porta configurada na sua máquina. **Não execute a criação se já existir um banco DevTask com dados que deseja preservar.**

```powershell
psql -h 127.0.0.1 -p 5432 -U postgres -d postgres
```

No prompt do `psql`, execute:

```sql
CREATE ROLE devtask_app LOGIN;
\password devtask_app
CREATE DATABASE devtask_db OWNER devtask_app;
\q
```

O comando `\password` solicita uma senha sem colocá-la no histórico de comandos. Use uma senha forte e exclusiva. Caso o usuário ou o banco já existam, **não os sobrescreva**: adapte a configuração de conexão ao ambiente existente.

### 4. Configurar as variáveis do backend

Abra um terminal PowerShell na raiz do repositório. Neste exemplo, utilizamos PostgreSQL local na porta `5432`:

```powershell
$env:DB_URL = 'jdbc:postgresql://127.0.0.1:5432/devtask_db'
$env:DB_USERNAME = 'devtask_app'
$env:FLYWAY_ENABLED = 'true'

$senhaSegura = Read-Host 'Senha do PostgreSQL DevTask' -AsSecureString
try {
    $env:DB_PASSWORD = [System.Net.NetworkCredential]::new('', $senhaSegura).Password
}
finally {
    $senhaSegura.Dispose()
    Remove-Variable senhaSegura -ErrorAction SilentlyContinue
}
```

Essas variáveis valem somente para o processo atual do terminal. **Não salve senhas no código, no README ou em arquivos versionados.**

> **Atenção às migrações:** utilize `FLYWAY_ENABLED=true` **somente em um banco novo e vazio**, permitindo que o Flyway execute `backend/src/main/resources/db/migration/V1__criar_estrutura_inicial.sql`. Para um banco já inicializado com o esquema antigo, mantenha o Flyway desativado e verifique a compatibilidade antes de migrar. Não execute a migração inicial por cima de tabelas existentes.

### 5. Iniciar o backend

No mesmo terminal que contém as variáveis:

```powershell
mvn -f backend/pom.xml spring-boot:run
```

O backend deverá escutar em `http://localhost:8080`. O Hibernate utiliza `ddl-auto=validate`, isto é, valida o esquema em vez de criá-lo automaticamente.

### 6. Iniciar o frontend

Abra **outro** PowerShell, na raiz do repositório:

```powershell
cd frontend
npm ci
npm start -- --proxy-config proxy.conf.json
```

Abra **http://localhost:4200** no navegador. Cadastre uma conta local de demonstração e teste as tarefas.

`npm ci` utiliza o arquivo de lock para instalar as dependências de forma reproduzível. Se precisar mudar dependências, faça isso conscientemente e atualize o lockfile correspondente.

### 7. Encerrar a execução

Pressione `Ctrl+C` nos terminais do backend e frontend. O banco PostgreSQL local pode ser parado separadamente pelo gerenciador de serviços, se desejado. O código e os dados locais permanecem no computador.

## Banco de dados e migrações

As tabelas principais são `usuario` e `tarefa`. Cada tarefa possui referência obrigatória ao seu proprietário (`usuario_id`). A migração Flyway V1 cria a estrutura necessária para um banco novo.

A pasta `backend/database` contém scripts SQL históricos utilizados durante o desenvolvimento. Eles **não devem ser executados em conjunto com a migração consolidada em um banco novo**, pois isso pode criar conflitos. Use o procedimento Flyway descrito acima para instalações novas.

## Segurança

O backend implementa autenticação com Spring Security, hashes de senha BCrypt, proteção CSRF, sessões HTTP, validação de entradas e autorização para acesso às tarefas do próprio usuário. Há também um mecanismo de limitação de tentativas de login, mantido em memória, com bloqueios temporários e respostas HTTP 429 quando aplicável.

Cookies de sessão utilizam HttpOnly e SameSite. A opção `SESSION_COOKIE_SECURE` deve ser ativada quando a aplicação for publicada por HTTPS; para desenvolvimento HTTP local, a configuração padrão é `false`.

Credenciais são fornecidas por variáveis de ambiente. O código foi submetido a verificações de dependências com npm audit e OWASP Dependency-Check durante o desenvolvimento. **Esses controles não representam uma certificação ou garantia de ausência de vulnerabilidades.**

## Testes

Na última validação local registrada durante o desenvolvimento, a suíte do backend executou **104 testes automatizados**, sem falhas ou erros. Para executar novamente, deixe o banco de testes necessário disponível e configure as variáveis de ambiente pertinentes:

```powershell
mvn -f backend/pom.xml clean test
```

Para validar a compilação Angular:

```powershell
npm --prefix frontend run build
```

Durante a publicação temporária na nuvem, foram testados manualmente cadastro, login, criação, edição, consulta e exclusão de tarefas, logout e persistência (8 fluxos aprovados). Esses resultados são **históricos** e não indicam que existe uma demonstração pública ativa.

## Documentação da API

Quando habilitada no ambiente local, a interface Swagger UI pode ser acessada em:

`http://localhost:8080/swagger-ui/index.html`

Em uma eventual publicação pública, avalie desabilitar Swagger UI e os endpoints de documentação, além de habilitar HTTPS e cookies Secure.

## Histórico da implantação

O DevTask também foi implantado temporariamente com **Angular no Netlify, Spring Boot na Railway e PostgreSQL na Railway**, utilizando HTTPS, proxy `/api/*`, variáveis de ambiente e migrações Flyway. Essa experiência faz parte do histórico técnico do projeto. Os serviços foram desativados/removidos para evitar consumo recorrente de hospedagem; o repositório permanece executável localmente.

O arquivo `frontend/public/_redirects` registra a configuração usada nessa implantação e aponta para o domínio antigo da Railway. **Não é necessário para executar o projeto localmente.** Uma nova publicação exigirá configurar um backend e um banco acessíveis e atualizar a regra de proxy.

## Melhorias futuras

- Simplificar ainda mais a instalação local com scripts de verificação e inicialização.
- Adicionar testes automatizados de ponta a ponta para o frontend.
- Criar um vídeo curto de demonstração usando apenas dados fictícios.
- Ampliar os testes de acessibilidade e a observabilidade.
- Preparar uma estratégia de backups e de gerenciamento de sessões para novos ambientes de hospedagem.

## Autor

**Fernando Augusto Bueno Canquerini**  
Estudante de Tecnologia em Análise e Desenvolvimento de Sistemas — Universidade Federal do Paraná (UFPR).

GitHub: https://github.com/fernandoaugustobueno10-wq

---

Projeto pessoal para aprendizado e portfólio em desenvolvimento de software.
