
import { Injectable, inject } from '@angular/core';

import {
  HttpClient,
  HttpHeaders,
  HttpParams
} from '@angular/common/http';

import {
  Observable,
  map,
  switchMap,
  throwError
} from 'rxjs';

interface CsrfResponse {
  headerName: string;
  token: string;
}

export interface SessaoResponse {
  autenticado: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly http = inject(HttpClient);

  private obterCsrf(): Observable<CsrfResponse> {
    return this.http.get<CsrfResponse>(
      '/api/auth/csrf',
      {
        withCredentials: true
      }
    ).pipe(
      map((csrf) => {
        if (
          csrf.headerName !== 'X-CSRF-TOKEN' &&
          csrf.headerName !== 'X-XSRF-TOKEN'
        ) {
          throw new Error('Cabeçalho CSRF inválido.');
        }

        if (
          typeof csrf.token !== 'string' ||
          csrf.token.length === 0
        ) {
          throw new Error('Token CSRF inválido.');
        }

        return csrf;
      })
    );
  }

  obterCabecalhosCsrf(): Observable<HttpHeaders> {
    return this.obterCsrf().pipe(
      map((csrf) => {
        return new HttpHeaders().set(
          csrf.headerName,
          csrf.token
        );
      })
    );
  }

  verificarSessao(): Observable<boolean> {
    return this.http.get<SessaoResponse>(
      '/api/auth/sessao',
      {
        withCredentials: true
      }
    ).pipe(
      map((resposta) => resposta.autenticado === true)
    );
  }

  login(email: string, senha: string): Observable<void> {
    const emailNormalizado = email.trim().toLowerCase();

    if (
      !emailNormalizado ||
      emailNormalizado.length > 254 ||
      !senha
    ) {
      return throwError(
        () => new Error('Informe e-mail e senha válidos.')
      );
    }

    const dados = new HttpParams()
      .set('username', emailNormalizado)
      .set('password', senha);

    return this.obterCabecalhosCsrf().pipe(
      switchMap((csrfHeaders) => {
        const headers = csrfHeaders.set(
          'Content-Type',
          'application/x-www-form-urlencoded'
        );

        return this.http.post<void>(
          '/api/auth/login',
          dados.toString(),
          {
            headers,
            withCredentials: true
          }
        );
      }),
      switchMap(() => this.obterCsrf()),
      map(() => undefined)
    );
  }

  logout(): Observable<void> {
    return this.obterCabecalhosCsrf().pipe(
      switchMap((headers) => {
        return this.http.post<void>(
          '/api/auth/logout',
          null,
          {
            headers,
            withCredentials: true
          }
        );
      })
    );
  }
}
