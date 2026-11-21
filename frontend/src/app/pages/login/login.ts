
import { Component, inject, signal } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';

import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  imports: [FormsModule, RouterLink],
  template: `
    <main class="login-page">
      <section class="presentation">
        <div class="brand">
          <span class="brand-icon" aria-hidden="true">✓</span>
          <span>DevTask</span>
        </div>

        <div class="presentation-content">
          <div class="eyebrow">SUA PRODUTIVIDADE, ORGANIZADA</div>

          <h1>
            Organize suas ideias.
            <span>Conquiste seus objetivos.</span>
          </h1>

          <p>
            Gerencie suas tarefas, acompanhe seu progresso e
            mantenha o foco no que realmente importa.
          </p>

          <div class="features">
            <div class="feature">
              <span class="feature-icon" aria-hidden="true">✓</span>
              <span>Organize suas tarefas em um só lugar</span>
            </div>

            <div class="feature">
              <span class="feature-icon" aria-hidden="true">✓</span>
              <span>Acompanhe suas prioridades</span>
            </div>

            <div class="feature">
              <span class="feature-icon" aria-hidden="true">✓</span>
              <span>Tenha mais controle sobre seu dia</span>
            </div>
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

          <div class="form-heading">
            <span class="section-label">BEM-VINDO DE VOLTA</span>
            <h2>Entre na sua conta</h2>
            <p>
              Acesse seu espaço de trabalho e continue
              de onde parou.
            </p>
          </div>

          <form
            #loginForm="ngForm"
            (ngSubmit)="entrar(loginForm)"
          >
            <div class="form-field">
              <label for="email">E-mail</label>

              <input
                id="email"
                name="email"
                type="email"
                placeholder="seuemail@exemplo.com"
                autocomplete="username"
                maxlength="254"
                [(ngModel)]="email"
                #campoEmail="ngModel"
                email
                required
                [disabled]="carregando()"
                [attr.aria-invalid]="
                  campoEmail.invalid && campoEmail.touched
                "
              />

              @if (campoEmail.invalid && campoEmail.touched) {
                <small class="field-error">
                  Informe um e-mail válido.
                </small>
              }
            </div>

            <div class="form-field">
              <label for="password">Senha</label>

              <input
                id="password"
                name="password"
                type="password"
                placeholder="Digite sua senha"
                autocomplete="current-password"
                [(ngModel)]="senha"
                #campoSenha="ngModel"
                required
                [disabled]="carregando()"
                [attr.aria-invalid]="
                  campoSenha.invalid && campoSenha.touched
                "
              />

              @if (campoSenha.invalid && campoSenha.touched) {
                <small class="field-error">
                  Informe sua senha.
                </small>
              }
            </div>

            @if (mensagemErro()) {
              <div class="feedback error" role="alert">
                <span aria-hidden="true">!</span>
                <p>{{ mensagemErro() }}</p>
              </div>
            }

            @if (mensagemSucesso()) {
              <div class="feedback success" role="status">
                <span aria-hidden="true">✓</span>
                <p>{{ mensagemSucesso() }}</p>
              </div>
            }

            <button
              type="submit"
              class="submit-button"
              [disabled]="carregando() || loginForm.invalid"
            >
              @if (carregando()) {
                <span class="spinner" aria-hidden="true"></span>
                Entrando...
              } @else {
                Entrar na minha conta
                <span aria-hidden="true">→</span>
              }
            </button>
          </form>

          <div class="form-footer">
            <p>Primeira vez por aqui?</p>
            <a routerLink="/cadastro">Criar minha conta</a>
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

    .login-page {
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
      color: #ffffff;
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
      color: #a9b4cc;
      font-size: 17px;
      line-height: 1.8;
    }

    .features {
      display: grid;
      gap: 19px;
      margin-top: 45px;
    }

    .feature {
      display: flex;
      align-items: center;
      gap: 13px;
      color: #d8deed;
      font-size: 14px;
    }

    .feature-icon {
      display: grid;
      place-items: center;
      width: 27px;
      height: 27px;
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
      justify-content: center;
      align-items: center;
      gap: 50px;
      padding: 60px 32px 30px;
      background: #0c1426;
    }

    .form-container {
      width: min(100%, 420px);
    }

    .mobile-brand {
      display: none;
    }

    .form-heading {
      margin-bottom: 37px;
    }

    h2 {
      margin: 14px 0 10px;
      font-size: 34px;
      letter-spacing: -1px;
    }

    .form-heading p {
      margin: 0;
      color: #8e9bb3;
      line-height: 1.7;
      font-size: 14px;
    }

    form {
      display: grid;
      gap: 23px;
    }

    .form-field {
      display: grid;
      gap: 10px;
    }

    label {
      color: #dce3f1;
      font-size: 13px;
      font-weight: 650;
    }

    input {
      width: 100%;
      height: 52px;
      padding: 0 17px;
      border: 1px solid #293652;
      border-radius: 12px;
      outline: none;
      background: #121e35;
      color: #ffffff;
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

    input[aria-invalid="true"] {
      border-color: #ef737d;
    }

    .field-error {
      color: #f49ca4;
      font-size: 12px;
    }

    .submit-button {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 14px;
      width: 100%;
      min-height: 53px;
      margin-top: 5px;
      border: 0;
      border-radius: 12px;
      background: #695cf1;
      color: white;
      font: inherit;
      font-weight: 700;
      cursor: pointer;
      transition: background 0.2s, opacity 0.2s;
    }

    .submit-button:hover:not(:disabled) {
      background: #8175ff;
    }

    .submit-button:disabled {
      cursor: not-allowed;
      opacity: 0.55;
    }

    .submit-button:focus-visible,
    .form-footer a:focus-visible {
      outline: 2px solid #aaa3ff;
      outline-offset: 4px;
    }

    .feedback {
      display: flex;
      align-items: flex-start;
      gap: 12px;
      padding: 14px 16px;
      border: 1px solid;
      border-radius: 12px;
    }

    .feedback span {
      display: grid;
      place-items: center;
      width: 22px;
      height: 22px;
      flex-shrink: 0;
      border-radius: 50%;
      font-size: 13px;
      font-weight: 800;
    }

    .feedback p {
      margin: 0;
      font-size: 13px;
      line-height: 1.6;
    }

    .feedback.error {
      background: rgba(230, 81, 94, 0.09);
      border-color: rgba(230, 81, 94, 0.3);
      color: #ffb3ba;
    }

    .feedback.error span {
      background: rgba(230, 81, 94, 0.2);
    }

    .feedback.success {
      background: rgba(60, 190, 140, 0.09);
      border-color: rgba(60, 190, 140, 0.3);
      color: #a0efd0;
    }

    .feedback.success span {
      background: rgba(60, 190, 140, 0.2);
    }

    .spinner {
      width: 17px;
      height: 17px;
      border: 2px solid rgba(255, 255, 255, 0.35);
      border-top-color: white;
      border-radius: 50%;
      animation: girar 0.7s linear infinite;
    }

    @keyframes girar {
      to {
        transform: rotate(360deg);
      }
    }

    .form-footer {
      margin-top: 38px;
      padding-top: 27px;
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
      color: #c4bfff;
      text-decoration: underline;
    }

    .security-note {
      margin: 0;
      color: #65738d;
      font-size: 11px;
      text-align: center;
    }

    @media (max-width: 850px) {
      .login-page {
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
        margin-bottom: 60px;
      }
    }

    @media (max-width: 480px) {
      h2 {
        font-size: 29px;
      }
    }

    @media (prefers-reduced-motion: reduce) {
      .spinner {
        animation-duration: 1.5s;
      }
    }
  `]
})
export class Login {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  email = '';
  senha = '';

  readonly carregando = signal(false);
  readonly mensagemErro = signal('');
  readonly mensagemSucesso = signal('');

  entrar(formulario: NgForm): void {
    this.mensagemErro.set('');
    this.mensagemSucesso.set('');

    if (this.carregando()) {
      return;
    }

    if (
      formulario.invalid ||
      !this.email.trim() ||
      !this.senha
    ) {
      formulario.control.markAllAsTouched();

      this.mensagemErro.set(
        'Preencha corretamente seu e-mail e sua senha.'
      );

      return;
    }

    this.carregando.set(true);

    this.authService.login(this.email, this.senha)
      .pipe(
        finalize(() => {
          this.carregando.set(false);
        })
      )
      .subscribe({
        next: () => {
          this.senha = '';

          void this.router.navigate(['/dashboard']).then(
            (navegou) => {
              if (!navegou) {
                this.mensagemErro.set(
                  'Login realizado, mas não foi possível abrir o painel. ' +
                  'Tente acessar o dashboard novamente.'
                );
              }
            }
          ).catch(() => {
            this.mensagemErro.set(
              'Não foi possível abrir o painel. Tente novamente.'
            );
          });
        },
        error: (erro: unknown) => {
          this.senha = '';

          if (erro instanceof HttpErrorResponse) {
            if (erro.status === 401) {
              this.mensagemErro.set(
                'E-mail ou senha incorretos.'
              );
              return;
            }

            if (erro.status === 429) {
              const retryAfter = erro.headers.get('Retry-After');

              const segundos =
                retryAfter !== null &&
                /^\d{1,5}$/.test(retryAfter)
                  ? Number(retryAfter)
                  : 0;

              if (segundos > 0 && segundos <= 86400) {
                const minutos = Math.ceil(segundos / 60);
                const periodo =
                  minutos === 1 ? 'minuto' : 'minutos';

                this.mensagemErro.set(
                  `Muitas tentativas de login. Aguarde aproximadamente ${minutos} ${periodo} antes de tentar novamente.`
                );
              } else {
                this.mensagemErro.set(
                  'Muitas tentativas de login. Aguarde alguns minutos antes de tentar novamente.'
                );
              }

              return;
            }

            if (erro.status === 403) {
              this.mensagemErro.set(
                'A verificação de segurança falhou. ' +
                'Atualize a página e tente novamente.'
              );
              return;
            }

            if (erro.status === 0) {
              this.mensagemErro.set(
                'Não foi possível conectar ao servidor. ' +
                'Verifique se o backend está funcionando.'
              );
              return;
            }
          }

          this.mensagemErro.set(
            'Não foi possível concluir o login. ' +
            'Tente novamente.'
          );
        }
      });
  }
}
