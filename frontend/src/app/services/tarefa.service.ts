
import { Injectable, inject } from '@angular/core';

import {
  HttpClient,
  HttpParams
} from '@angular/common/http';

import { Observable, switchMap } from 'rxjs';

import { AuthService } from './auth.service';

export type StatusTarefa =
  | 'PENDENTE'
  | 'EM_ANDAMENTO'
  | 'CONCLUIDA';

export type PrioridadeTarefa =
  | 'BAIXA'
  | 'MEDIA'
  | 'ALTA';

export interface Tarefa {
  id: number;
  titulo: string;
  descricao: string | null;
  status: StatusTarefa;
  prioridade: PrioridadeTarefa;
  dataCriacao?: string;
}

export interface CriarTarefaRequest {
  titulo: string;
  descricao: string | null;
  prioridade: PrioridadeTarefa;
}

export interface AtualizarTarefaRequest {
  titulo: string;
  descricao: string | null;
  status: StatusTarefa;
  prioridade: PrioridadeTarefa;
}

export interface PaginaTarefas {
  content: Tarefa[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

export interface FiltroTarefas {
  status?: StatusTarefa;
  prioridade?: PrioridadeTarefa;
  page?: number;
  size?: number;
  ordenarPor?: 'id' | 'titulo' | 'status' |
    'prioridade' | 'dataCriacao';
  direcao?: 'ASC' | 'DESC';
}

@Injectable({
  providedIn: 'root'
})
export class TarefaService {
  private readonly http = inject(HttpClient);
  private readonly authService = inject(AuthService);

  private readonly apiUrl = '/api/tarefas';

  listar(): Observable<Tarefa[]> {
    return this.http.get<Tarefa[]>(
      this.apiUrl,
      {
        withCredentials: true
      }
    );
  }

  consultar(filtros: FiltroTarefas = {}): Observable<PaginaTarefas> {
    let params = new HttpParams();

    if (filtros.status) {
      params = params.set('status', filtros.status);
    }

    if (filtros.prioridade) {
      params = params.set('prioridade', filtros.prioridade);
    }

    if (filtros.page !== undefined) {
      if (
        !Number.isSafeInteger(filtros.page) ||
        filtros.page < 0
      ) {
        throw new RangeError('Número de página inválido.');
      }

      params = params.set(
        'page',
        filtros.page.toString()
      );
    }

    if (filtros.size !== undefined) {
      if (
        !Number.isSafeInteger(filtros.size) ||
        filtros.size < 1 ||
        filtros.size > 100
      ) {
        throw new RangeError(
          'O tamanho da página deve estar entre 1 e 100.'
        );
      }

      params = params.set(
        'size',
        filtros.size.toString()
      );
    }

    if (filtros.ordenarPor) {
      params = params.set(
        'ordenarPor',
        filtros.ordenarPor
      );
    }

    if (filtros.direcao) {
      params = params.set(
        'direcao',
        filtros.direcao
      );
    }

    return this.http.get<PaginaTarefas>(
      `${this.apiUrl}/consulta`,
      {
        params,
        withCredentials: true
      }
    );
  }

  buscarPorId(id: number): Observable<Tarefa> {
    const tarefaId = this.validarId(id);

    return this.http.get<Tarefa>(
      `${this.apiUrl}/${tarefaId}`,
      {
        withCredentials: true
      }
    );
  }

  criar(dados: CriarTarefaRequest): Observable<Tarefa> {
    this.validarDados(
      dados.titulo,
      dados.descricao
    );

    this.validarPrioridade(dados.prioridade);

    const payload: CriarTarefaRequest = {
      titulo: dados.titulo.trim(),
      descricao: dados.descricao?.trim() ?? null,
      prioridade: dados.prioridade
    };

    return this.authService.obterCabecalhosCsrf().pipe(
      switchMap((headers) => {
        return this.http.post<Tarefa>(
          this.apiUrl,
          payload,
          {
            headers,
            withCredentials: true
          }
        );
      })
    );
  }

  atualizar(
    id: number,
    dados: AtualizarTarefaRequest
  ): Observable<Tarefa> {
    const tarefaId = this.validarId(id);

    this.validarDados(
      dados.titulo,
      dados.descricao
    );

    this.validarStatus(dados.status);
    this.validarPrioridade(dados.prioridade);

    const payload: AtualizarTarefaRequest = {
      titulo: dados.titulo.trim(),
      descricao: dados.descricao?.trim() ?? null,
      status: dados.status,
      prioridade: dados.prioridade
    };

    return this.authService.obterCabecalhosCsrf().pipe(
      switchMap((headers) => {
        return this.http.put<Tarefa>(
          `${this.apiUrl}/${tarefaId}`,
          payload,
          {
            headers,
            withCredentials: true
          }
        );
      })
    );
  }

  excluir(id: number): Observable<void> {
    const tarefaId = this.validarId(id);

    return this.authService.obterCabecalhosCsrf().pipe(
      switchMap((headers) => {
        return this.http.delete<void>(
          `${this.apiUrl}/${tarefaId}`,
          {
            headers,
            withCredentials: true
          }
        );
      })
    );
  }

  private validarId(id: number): number {
    if (
      !Number.isSafeInteger(id) ||
      id <= 0
    ) {
      throw new RangeError(
        'Identificador da tarefa inválido.'
      );
    }

    return id;
  }

  private validarDados(
    titulo: string,
    descricao: string | null
  ): void {
    if (
      typeof titulo !== 'string' ||
      titulo.trim().length === 0 ||
      titulo.trim().length > 120
    ) {
      throw new Error(
        'O título deve conter entre 1 e 120 caracteres.'
      );
    }

    if (
      descricao !== null &&
      (
        typeof descricao !== 'string' ||
        descricao.trim().length > 2000
      )
    ) {
      throw new Error(
        'A descrição deve conter no máximo 2000 caracteres.'
      );
    }
  }

  private validarStatus(
    status: StatusTarefa
  ): void {
    const permitidos: StatusTarefa[] = [
      'PENDENTE',
      'EM_ANDAMENTO',
      'CONCLUIDA'
    ];

    if (!permitidos.includes(status)) {
      throw new Error(
        'Status da tarefa inválido.'
      );
    }
  }

  private validarPrioridade(
    prioridade: PrioridadeTarefa
  ): void {
    const permitidas: PrioridadeTarefa[] = [
      'BAIXA',
      'MEDIA',
      'ALTA'
    ];

    if (!permitidas.includes(prioridade)) {
      throw new Error(
        'Prioridade da tarefa inválida.'
      );
    }
  }
}
