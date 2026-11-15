
import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-cadastro',
  imports: [RouterLink],
  template: `
    <main class="cadastro-page">
      <section class="presentation">
        <div class="brand">
          <span class="brand-icon" aria-hidden="true">✓</span>
          DevTask
        </div>

        <div class="presentation-content">
          <span class="eyebrow">COMECE A SE ORGANIZAR</span>

          <h1>
            Seus objetivos merecem
            <span>um plano melhor.</span>
          </h1>

          <p>
            Crie sua conta e prepare seu espaço para organizar
            tarefas, acompanhar prioridades e transformar
            planos em resultados.
          </p>

          <div class="feature">
            <span class="feature-icon" aria-hidden="true">✓</span>
            <span>Um espaço pessoal para suas tarefas</span>
          </div>

          <div class="feature">
            <span class="feature-icon" aria-hidden="true">✓</span>
            <span>Organização por prioridades e status</span>
          </div>

          <div class="feature">
            <span class="feature-icon" aria-hidden="true">✓</span>
            <span>Seus dados separados dos demais usuários</span>
          </div>
        </div>

        <p class="presentation-footer">
          DevTask — Organização que acompanha você.
        </p>
      </section>

      <section class="form-area">
        <div class="form-container">
          <div class="mobile-brand">
            <span class="brand-icon" aria-hidden="true">✓</span>
            DevTask
          </div>

          <span class="section-label">CRIE SEU ESPAÇO</span>
          <h2>Crie sua conta</h2>

          <p class="form-description">
            Preencha seus dados para começar a utilizar o DevTask.
          </p>

          <form>
            <div class="form-field">
              <label for="nome">Nome completo</label>
              <input
                id="nome"
                name="nome"
                type="text"
                placeholder="Como podemos chamar você?"
                autocomplete="name"
                maxlength="120"
                required
              />
            </div>

            <div class="form-field">
              <label for="email">E-mail</label>
              <input
                id="email"
                name="email"
                type="email"
                placeholder="seuemail@exemplo.com"
                autocomplete="email"
                maxlength="254"
                required
              />
            </div>

            <div class="form-field">
              <label for="senha">Senha</label>
              <input
                id="senha"
                name="senha"
                type="password"
                placeholder="Crie uma senha"
                autocomplete="new-password"
                required
              />
              <small>
                Utilize uma senha forte e exclusiva.
              </small>
            </div>

            <div class="form-field">
              <label for="confirmarSenha">Confirmar senha</label>
              <input
                id="confirmarSenha"
                name="confirmarSenha"
                type="password"
                placeholder="Digite a senha novamente"
                autocomplete="new-password"
                required
              />
            </div>

            <button
              class="submit-button"
              type="button"
              disabled
              title="Cadastro em desenvolvimento"
            >
              Criar minha conta
              <span aria-hidden="true">→</span>
            </button>

            <p class="integration-notice">
              O cadastro ficará disponível após a integração
              com o backend.
            </p>
          </form>

          <div class="form-footer">
            <p>Já possui uma conta?</p>
            <a routerLink="/login">Entrar na minha conta</a>
          </div>
        </div>

        <p class="security-note">
          Desenvolvido com Angular e Spring Boot
        </p>
      </section>
    </main>
  `,
  styles: [`
    :host {
      display: block;
      min-height: 100vh;
      color: #e9edf8;
    }

    .cadastro-page {
      display: grid;
      grid-template-columns: 1.05fr 0.95fr;
      min-height: 100vh;
    }

    .presentation {
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      padding: 56px clamp(32px, 6vw, 100px);
      background:
        radial-gradient(
          circle at 15% 80%,
          rgba(93, 76, 220, 0.20),
          transparent 44%
        ),
        linear-gradient(145deg, #111c37, #091122 75%);
    }

    .brand,
    .mobile-brand {
      display: flex;
      align-items: center;
      gap: 12px;
      font-size: 24px;
      font-weight: 800;
      letter-spacing: -0.8px;
    }

    .brand-icon {
      display: inline-grid;
      place-items: center;
      width: 39px;
      height: 39px;
      border-radius: 12px;
      background: #695cf1;
      color: white;
      font-size: 22px;
    }

    .presentation-content {
      max-width: 580px;
    }

    .eyebrow,
    .section-label {
      color: #9d95ff;
      font-size: 12px;
      font-weight: 750;
      letter-spacing: 2px;
    }

    h1 {
      margin: 28px 0 24px;
      font-size: clamp(36px, 4vw, 60px);
      line-height: 1.15;
      letter-spacing: -2px;
    }

    h1 span {
      display: block;
      color: #aaa3ff;
    }

    .presentation-content > p {
      max-width: 490px;
      margin-bottom: 45px;
      color: #a9b4cc;
      font-size: 17px;
      line-height: 1.8;
    }

    .feature {
      display: flex;
      align-items: center;
      gap: 13px;
      margin-top: 19px;
      color: #d8deed;
      font-size: 14px;
    }

    .feature-icon {
      display: grid;
      place-items: center;
      width: 27px;
      height: 27px;
      flex-shrink: 0;
      border-radius: 8px;
      background: rgba(133, 121, 255, 0.16);
      color: #aaa3ff;
    }

    .presentation-footer {
      margin: 0;
      color: #7785a2;
      font-size: 12px;
    }

    .form-area {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 40px;
      padding: 48px 32px 28px;
      background: #0c1426;
    }

    .form-container {
      width: min(100%, 420px);
    }

    .mobile-brand {
      display: none;
    }

    h2 {
      margin: 14px 0 10px;
      font-size: 34px;
      letter-spacing: -1px;
    }

    .form-description {
      margin: 0 0 28px;
      color: #8e9bb3;
      line-height: 1.7;
      font-size: 14px;
    }

    form {
      display: grid;
      gap: 18px;
    }

    .form-field {
      display: grid;
      gap: 9px;
    }

    label {
      color: #dce3f1;
      font-size: 13px;
      font-weight: 650;
    }

    input {
      width: 100%;
      height: 50px;
      padding: 0 17px;
      border: 1px solid #293652;
      border-radius: 12px;
      outline: none;
      background: #121e35;
      color: white;
      font: inherit;
      transition: border-color 0.2s, box-shadow 0.2s;
    }

    input::placeholder {
      color: #687793;
    }

    input:focus {
      border-color: #8477ff;
      box-shadow: 0 0 0 3px rgba(132, 119, 255, 0.14);
    }

    small {
      color: #8e9bb3;
      font-size: 11px;
    }

    .submit-button {
      display: flex;
      justify-content: center;
      align-items: center;
      gap: 14px;
      width: 100%;
      min-height: 53px;
      margin-top: 7px;
      border: 0;
      border-radius: 12px;
      background: #695cf1;
      color: white;
      font: inherit;
      font-weight: 700;
      opacity: 0.65;
      cursor: not-allowed;
    }

    .integration-notice {
      margin: 0;
      color: #8e9bb3;
      font-size: 12px;
      line-height: 1.6;
      text-align: center;
    }

    .form-footer {
      margin-top: 30px;
      padding-top: 23px;
      border-top: 1px solid #26314a;
      text-align: center;
    }

    .form-footer p {
      margin: 0 0 8px;
      color: #aab6cd;
      font-size: 13px;
    }

    .form-footer a {
      color: #aaa3ff;
      font-size: 13px;
      font-weight: 650;
      text-decoration: none;
    }

    .form-footer a:hover {
      text-decoration: underline;
    }

    .security-note {
      margin: 0;
      color: #65738d;
      font-size: 11px;
      text-align: center;
    }

    @media (max-width: 850px) {
      .cadastro-page {
        grid-template-columns: 1fr;
      }

      .presentation {
        display: none;
      }

      .form-area {
        min-height: 100vh;
        padding: 38px 24px;
      }

      .mobile-brand {
        display: flex;
        margin-bottom: 45px;
      }
    }

    @media (max-width: 480px) {
      h2 {
        font-size: 29px;
      }
    }
  `]
})
export class Cadastro {}
