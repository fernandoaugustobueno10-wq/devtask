
import {
  Component,
  OnInit,
  computed,
  inject,
  signal
} from '@angular/core';

import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';

import {
  Tarefa,
  TarefaService,
  StatusTarefa,
  PrioridadeTarefa
} from '../../services/tarefa.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class Dashboard implements OnInit {
  private readonly tarefaService = inject(TarefaService);
  private readonly router = inject(Router);

  readonly tarefas = signal<Tarefa[]>([]);
  readonly carregando = signal(true);
  readonly mensagemErro = signal('');

  readonly filtroStatus = signal<StatusTarefa | 'TODAS'>(
    'TODAS'
  );

  readonly filtroPrioridade = signal<
    PrioridadeTarefa | 'TODAS'
  >('TODAS');

  readonly totalTarefas = computed(() => {
    return this.tarefas().length;
  });

  readonly totalPendentes = computed(() => {
    return this.tarefas().filter(
      (tarefa) => tarefa.status === 'PENDENTE'
    ).length;
  });

  readonly totalEmAndamento = computed(() => {
    return this.tarefas().filter(
      (tarefa) => tarefa.status === 'EM_ANDAMENTO'
    ).length;
  });

  readonly totalConcluidas = computed(() => {
    return this.tarefas().filter(
      (tarefa) => tarefa.status === 'CONCLUIDA'
    ).length;
  });

  readonly percentualConclusao = computed(() => {
    const total = this.totalTarefas();

    if (total === 0) {
      return 0;
    }

    return Math.round(
      (this.totalConcluidas() / total) * 100
    );
  });

  readonly tarefasFiltradas = computed(() => {
    const status = this.filtroStatus();
    const prioridade = this.filtroPrioridade();

    return this.tarefas().filter((tarefa) => {
      const correspondeStatus =
        status === 'TODAS' ||
        tarefa.status === status;

      const correspondePrioridade =
        prioridade === 'TODAS' ||
        tarefa.prioridade === prioridade;

      return correspondeStatus && correspondePrioridade;
    });
  });

  ngOnInit(): void {
    this.carregarTarefas();
  }

  carregarTarefas(): void {
    if (this.carregando() && this.mensagemErro()) {
      return;
    }

    this.carregando.set(true);
    this.mensagemErro.set('');

    this.tarefaService.listar().subscribe({
      next: (tarefas) => {
        if (!Array.isArray(tarefas)) {
          this.tarefas.set([]);
          this.mensagemErro.set(
            'O servidor retornou uma resposta inesperada.'
          );
          this.carregando.set(false);
          return;
        }

        this.tarefas.set(tarefas);
        this.carregando.set(false);
      },

      error: (erro: unknown) => {
        this.carregando.set(false);
        this.tarefas.set([]);

        if (erro instanceof HttpErrorResponse) {
          if (
            erro.status === 401 ||
            erro.status === 403
          ) {
            this.mensagemErro.set(
              'Sua sessão expirou. Faça login novamente.'
            );

            void this.router.navigate(['/login']);
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
          'Não foi possível carregar suas tarefas. ' +
          'Tente novamente.'
        );
      }
    });
  }

  alterarFiltroStatus(valor: string): void {
    const valoresPermitidos: Array<
      StatusTarefa | 'TODAS'
    > = [
      'TODAS',
      'PENDENTE',
      'EM_ANDAMENTO',
      'CONCLUIDA'
    ];

    if (
      valoresPermitidos.includes(
        valor as StatusTarefa | 'TODAS'
      )
    ) {
      this.filtroStatus.set(
        valor as StatusTarefa | 'TODAS'
      );
    }
  }

  alterarFiltroPrioridade(valor: string): void {
    const valoresPermitidos: Array<
      PrioridadeTarefa | 'TODAS'
    > = [
      'TODAS',
      'BAIXA',
      'MEDIA',
      'ALTA'
    ];

    if (
      valoresPermitidos.includes(
        valor as PrioridadeTarefa | 'TODAS'
      )
    ) {
      this.filtroPrioridade.set(
        valor as PrioridadeTarefa | 'TODAS'
      );
    }
  }

  nomeStatus(status: StatusTarefa): string {
    const nomes: Record<StatusTarefa, string> = {
      PENDENTE: 'Pendente',
      EM_ANDAMENTO: 'Em andamento',
      CONCLUIDA: 'Concluída'
    };

    return nomes[status] ?? 'Desconhecido';
  }

  nomePrioridade(
    prioridade: PrioridadeTarefa
  ): string {
    const nomes: Record<PrioridadeTarefa, string> = {
      BAIXA: 'Baixa',
      MEDIA: 'Média',
      ALTA: 'Alta'
    };

    return nomes[prioridade] ?? 'Desconhecida';
  }
}
