
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import {
  Observable,
  map,
  switchMap,
  throwError
} from 'rxjs';

import { AuthService } from './auth.service';

export interface CadastrarUsuarioRequest {
  nome: string;
  email: string;
  senha: string;
}

@Injectable({
  providedIn: 'root'
})
export class UsuarioService {
  private readonly http = inject(HttpClient);
  private readonly authService = inject(AuthService);

  private readonly apiUrl = '/api/usuarios/cadastro';

  cadastrar(
    dados: CadastrarUsuarioRequest
  ): Observable<void> {
    const nome = dados.nome?.trim();
    const email = dados.email?.trim().toLowerCase();
    const senha = dados.senha;

    if (
      typeof nome !== 'string' ||
      nome.length === 0 ||
      nome.length > 120
    ) {
      return throwError(
        () => new Error(
          'O nome deve conter entre 1 e 120 caracteres.'
        )
      );
    }

    if (
      typeof email !== 'string' ||
      email.length === 0 ||
      email.length > 254 ||
      !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)
    ) {
      return throwError(
        () => new Error('Informe um e-mail válido.')
      );
    }

    if (
      typeof senha !== 'string' ||
      senha.trim().length === 0
    ) {
      return throwError(
        () => new Error('Informe uma senha válida.')
      );
    }

    if (Array.from(senha).length < 12) {
      return throwError(
        () => new Error(
          'A senha deve conter no mínimo 12 caracteres.'
        )
      );
    }

    const bytesSenha = new TextEncoder().encode(senha);

    if (bytesSenha.length > 72) {
      return throwError(
        () => new Error(
          'A senha não pode ultrapassar 72 bytes em UTF-8.'
        )
      );
    }

    const payload: CadastrarUsuarioRequest = {
      nome,
      email,
      senha
    };

    return this.authService.obterCabecalhosCsrf().pipe(
      switchMap((headers) => {
        return this.http.post<unknown>(
          this.apiUrl,
          payload,
          {
            headers,
            withCredentials: true
          }
        );
      }),
      map(() => undefined)
    );
  }
}
