
import { Component, inject, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { UsuarioService } from '../../services/usuario.service';

@Component({
  selector: 'app-cadastro',
  standalone: true,
  imports: [FormsModule, RouterLink],
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

          <form
            #formCadastro="ngForm"
            (ngSubmit)="cadastrar()"
            novalidate
          >
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
                [(ngModel)]="nome"
                #campoNome="ngModel"
                [disabled]="cadastrando()"
                [attr.aria-invalid]="campoNome.invalid && campoNome.touched"
              />

              @if (campoNome.invalid && campoNome.touched) {
                <small class="field-error">
                  Informe seu nome completo.
                </small>
              }
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
                email
                [(ngModel)]="email"
                #campoEmail="ngModel"
                [disabled]="cadastrando()"
                [attr.aria-invalid]="campoEmail.invalid && campoEmail.touched"
              />

              @if (campoEmail.invalid && campoEmail.touched) {
                <small class="field-error">
                  Informe um e-mail válido.
                </small>
              }
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
                minlength="12"
                [(ngModel)]="senha"
                #campoSenha="ngModel"
                [disabled]="cadastrando()"
                [attr.aria-invalid]="campoSenha.invalid && campoSenha.touched"
                aria-describedby="senha-orientacao"
              />

              <small id="senha-orientacao">
                Use uma senha exclusiva com pelo menos
                12 caracteres e até 72 bytes em UTF-8.
              </small>

              @if (campoSenha.invalid && campoSenha.touched) {
                <small class="field-error">
                  A senha deve conter pelo menos 12 caracteres.
                </small>
              }

              @if (senha.length > 0 && senhaEmBytes() > 72) {
                <small class="field-error">
                  A senha ultrapassa o limite de 72 bytes em UTF-8.
                </small>
              }
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
                [(ngModel)]="confirmarSenha"
                #campoConfirmacao="ngModel"
                [disabled]="cadastrando()"
                [attr.aria-invalid]="
                  campoConfirmacao.touched &&
                  (campoConfirmacao.invalid || senha !== confirmarSenha)
                "
              />

              @if (
                campoConfirmacao.touched &&
                confirmarSenha.length > 0 &&
                senha !== confirmarSenha
              ) {
                <small class="field-error">
                  As senhas informadas não coincidem.
                </small>
              }
            </div>

            @if (mensagemErro()) {
              <div class="error-alert" role="alert">
                {{ mensagemErro() }}
              </div>
            }

            <button
              class="submit-button"
              type="submit"
              [disabled]="
                cadastrando() ||
                formCadastro.invalid ||
                senha !== confirmarSenha ||
                senhaEmBytes() > 72
              "
            >
              @if (cadastrando()) {
                Criando sua conta...
              } @else {
                Criar minha conta
                <span aria-hidden="true">→</span>
              }
            </button>
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

    input[aria-invalid="true"] {
      border-color: #ef7985;
    }

    input:disabled {
      opacity: 0.65;
      cursor: not-allowed;
    }

    small {
      color: #8e9bb3;
      font-size: 11px;
      line-height: 1.5;
    }

    .field-error {
      color: #fca5a5;
    }

    .error-alert {
      padding: 13px 15px;
      border: 1px solid #7f3640;
      border-radius: 10px;
      color: #fecaca;
      background: #48252f;
      font-size: 13px;
      line-height: 1.5;
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
      cursor: pointer;
      transition: background 0.2s, opacity 0.2s;
    }

    .submit-button:hover:not(:disabled) {
      background: #584be0;
    }

    .submit-button:disabled {
      opacity: 0.55;
      cursor: not-allowed;
    }

    .submit-button:focus-visible,
    .form-footer a:focus-visible {
      outline: 2px solid #aaa3ff;
      outline-offset: 3px;
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

    @media (prefers-reduced-motion: reduce) {
      input,
      .submit-button {
        transition: none;
      }
    }
  `]
})
export class Cadastro {
  private readonly usuarioService = inject(UsuarioService);
  private readonly router = inject(Router);

  readonly cadastrando = signal(false);
  readonly mensagemErro = signal('');

  nome = '';
  email = '';
  senha = '';
  confirmarSenha = '';

  senhaEmBytes(): number {
    return new TextEncoder().encode(this.senha).length;
  }

  cadastrar(): void {
    if (this.cadastrando()) {
      return;
    }

    this.mensagemErro.set('');

    const nome = this.nome.trim();
    const email = this.email.trim().toLowerCase();
    const senha = this.senha;
    const confirmarSenha = this.confirmarSenha;

    if (nome.length === 0 || nome.length > 120) {
      this.mensagemErro.set(
        'Informe um nome com até 120 caracteres.'
      );
      return;
    }

    if (
      email.length === 0 ||
      email.length > 254 ||
      !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)
    ) {
      this.mensagemErro.set('Informe um e-mail válido.');
      return;
    }

    if (Array.from(senha).length < 12) {
      this.mensagemErro.set(
        'A senha deve conter pelo menos 12 caracteres.'
      );
      return;
    }

    if (!senha.trim()) {
      this.mensagemErro.set('Informe uma senha válida.');
      return;
    }

    if (this.senhaEmBytes() > 72) {
      this.mensagemErro.set(
        'A senha ultrapassa 72 bytes em UTF-8.'
      );
      return;
    }

    if (senha !== confirmarSenha) {
      this.mensagemErro.set(
        'As senhas informadas não coincidem.'
      );
      return;
    }

    this.cadastrando.set(true);

    this.usuarioService.cadastrar({
      nome,
      email,
      senha
    }).subscribe({
      next: () => {
        this.cadastrando.set(false);
        this.senha = '';
        this.confirmarSenha = '';

        void this.router.navigateByUrl('/login', {
          replaceUrl: true
        }).then((navegou) => {
          if (!navegou) {
            this.mensagemErro.set(
              'Conta criada, mas não foi possível abrir o login.'
            );
          }
        }).catch(() => {
          this.mensagemErro.set(
            'Conta criada, mas houve um erro ao abrir o login.'
          );
        });
      },

      error: (erro: unknown) => {
        this.cadastrando.set(false);

        if (erro instanceof HttpErrorResponse) {
          if (erro.status === 409) {
            this.mensagemErro.set(
              'Este e-mail já possui uma conta cadastrada.'
            );
            return;
          }

          if (erro.status === 400) {
            this.mensagemErro.set(
              'Os dados informados são inválidos. ' +
              'Confira os campos e tente novamente.'
            );
            return;
          }

          if (erro.status === 403) {
            this.mensagemErro.set(
              'Não foi possível validar a proteção de segurança. ' +
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
          erro instanceof Error &&
          !(erro instanceof HttpErrorResponse)
            ? erro.message
            : 'Não foi possível criar sua conta. Tente novamente.'
        );
      }
    });
  }
}
