
import {
  Component,
  ElementRef,
  HostListener,
  Injector,
  OnInit,
  ViewChild,
  afterNextRender,
  computed,
  inject,
  signal
} from '@angular/core';

import { DOCUMENT } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import {
  CriarTarefaRequest,
  PrioridadeTarefa,
  StatusTarefa,
  Tarefa,
  TarefaService
} from '../../services/tarefa.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class Dashboard implements OnInit {
  private readonly tarefaService = inject(TarefaService);
  private readonly router = inject(Router);
  private readonly injector = inject(Injector);
  private readonly document = inject(DOCUMENT);

  private elementoFocadoAnterior: HTMLElement | null = null;

  @ViewChild('modalNovaTarefa')
  private modalNovaTarefa?: ElementRef<HTMLElement>;

  @ViewChild('campoTituloNovaTarefa')
  private campoTituloNovaTarefa?: ElementRef<HTMLInputElement>;

  readonly tarefas = signal<Tarefa[]>([]);
  readonly carregando = signal(true);
  readonly mensagemErro = signal('');

  readonly filtroStatus = signal<StatusTarefa | 'TODAS'>(
    'TODAS'
  );

  readonly filtroPrioridade = signal<
    PrioridadeTarefa | 'TODAS'
  >('TODAS');

  readonly exibindoFormulario = signal(false);
  readonly salvandoTarefa = signal(false);
  readonly erroCadastro = signal('');
  readonly sucessoCadastro = signal('');

  tituloNovaTarefa = '';
  descricaoNovaTarefa = '';
  prioridadeNovaTarefa: PrioridadeTarefa = 'MEDIA';

  readonly totalTarefas = computed(
    () => this.tarefas().length
  );

  readonly totalPendentes = computed(
    () => this.tarefas().filter(
      (tarefa) => tarefa.status === 'PENDENTE'
    ).length
  );

  readonly totalEmAndamento = computed(
    () => this.tarefas().filter(
      (tarefa) => tarefa.status === 'EM_ANDAMENTO'
    ).length
  );

  readonly totalConcluidas = computed(
    () => this.tarefas().filter(
      (tarefa) => tarefa.status === 'CONCLUIDA'
    ).length
  );

  readonly percentualConclusao = computed(() => {
    const total = this.totalTarefas();

    return total === 0
      ? 0
      : Math.round(
          (this.totalConcluidas() / total) * 100
        );
  });

  readonly tarefasFiltradas = computed(() => {
    const status = this.filtroStatus();
    const prioridade = this.filtroPrioridade();

    return this.tarefas().filter((tarefa) => {
      return (
        (status === 'TODAS' || tarefa.status === status) &&
        (
          prioridade === 'TODAS' ||
          tarefa.prioridade === prioridade
        )
      );
    });
  });

  ngOnInit(): void {
    this.carregarTarefas();
  }

  carregarTarefas(): void {
    this.carregando.set(true);
    this.mensagemErro.set('');

    this.tarefaService.listar().subscribe({
      next: (tarefas) => {
        this.carregando.set(false);

        if (!Array.isArray(tarefas)) {
          this.tarefas.set([]);
          this.mensagemErro.set(
            'O servidor retornou uma resposta inesperada.'
          );
          return;
        }

        this.tarefas.set(tarefas);
      },

      error: (erro: unknown) => {
        this.carregando.set(false);
        this.tarefas.set([]);

        if (erro instanceof HttpErrorResponse) {
          if (erro.status === 401 || erro.status === 403) {
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

  abrirFormulario(): void {
    if (this.salvandoTarefa()) {
      return;
    }

    const elementoAtivo = this.document.activeElement;

    this.elementoFocadoAnterior =
      elementoAtivo instanceof HTMLElement
        ? elementoAtivo
        : null;

    this.erroCadastro.set('');
    this.sucessoCadastro.set('');
    this.exibindoFormulario.set(true);

    afterNextRender(
      () => {
        if (this.exibindoFormulario()) {
          this.campoTituloNovaTarefa?.nativeElement.focus();
        }
      },
      { injector: this.injector }
    );
  }

  fecharFormulario(): void {
    if (this.salvandoTarefa()) {
      return;
    }

    this.limparFormulario();
    this.exibindoFormulario.set(false);
    this.restaurarFoco();
  }

  @HostListener('document:keydown', ['$event'])
  aoPressionarTecla(evento: KeyboardEvent): void {
    if (!this.exibindoFormulario()) {
      return;
    }

    if (evento.key === 'Escape') {
      evento.preventDefault();
      this.fecharFormulario();
      return;
    }

    if (evento.key !== 'Tab') {
      return;
    }

    const modal = this.modalNovaTarefa?.nativeElement;

    if (!modal) {
      return;
    }

    const elementos = Array.from(
      modal.querySelectorAll<HTMLElement>(
        'button:not([disabled]), ' +
        'input:not([disabled]), ' +
        'textarea:not([disabled]), ' +
        'select:not([disabled]), ' +
        '[tabindex]:not([tabindex="-1"])'
      )
    );

    if (elementos.length === 0) {
      evento.preventDefault();
      modal.focus();
      return;
    }

    const primeiro = elementos[0];
    const ultimo = elementos[elementos.length - 1];
    const ativo = this.document.activeElement;

    if (
      evento.shiftKey &&
      (ativo === primeiro || !modal.contains(ativo))
    ) {
      evento.preventDefault();
      ultimo.focus();
      return;
    }

    if (
      !evento.shiftKey &&
      (ativo === ultimo || !modal.contains(ativo))
    ) {
      evento.preventDefault();
      primeiro.focus();
    }
  }

  salvarNovaTarefa(): void {
    if (this.salvandoTarefa()) {
      return;
    }

    this.erroCadastro.set('');
    this.sucessoCadastro.set('');

    const titulo = this.tituloNovaTarefa.trim();
    const descricao = this.descricaoNovaTarefa.trim();
    const prioridade = this.prioridadeNovaTarefa;

    if (titulo.length === 0 || titulo.length > 120) {
      this.erroCadastro.set(
        'O título deve conter entre 1 e 120 caracteres.'
      );
      return;
    }

    if (descricao.length > 2000) {
      this.erroCadastro.set(
        'A descrição deve conter no máximo 2000 caracteres.'
      );
      return;
    }

    if (
      prioridade !== 'BAIXA' &&
      prioridade !== 'MEDIA' &&
      prioridade !== 'ALTA'
    ) {
      this.erroCadastro.set(
        'Selecione uma prioridade válida.'
      );
      return;
    }

    const dados: CriarTarefaRequest = {
      titulo,
      descricao: descricao || null,
      prioridade
    };

    this.salvandoTarefa.set(true);

    this.tarefaService.criar(dados).subscribe({
      next: () => {
        this.salvandoTarefa.set(false);
        this.limparFormulario();
        this.exibindoFormulario.set(false);
        this.restaurarFoco();

        this.filtroStatus.set('TODAS');
        this.filtroPrioridade.set('TODAS');

        this.sucessoCadastro.set(
          'Tarefa cadastrada com sucesso!'
        );

        this.carregarTarefas();
      },

      error: (erro: unknown) => {
        this.salvandoTarefa.set(false);

        if (erro instanceof HttpErrorResponse) {
          if (erro.status === 400) {
            this.erroCadastro.set(
              'Dados inválidos. Confira os campos.'
            );
            return;
          }

          if (erro.status === 401 || erro.status === 403) {
            this.erroCadastro.set(
              'Sessão ou token de segurança inválido. ' +
              'Faça login novamente.'
            );
            return;
          }

          if (erro.status === 0) {
            this.erroCadastro.set(
              'Não foi possível conectar ao servidor.'
            );
            return;
          }
        }

        this.erroCadastro.set(
          'Não foi possível cadastrar a tarefa. ' +
          'Tente novamente.'
        );
      }
    });
  }

  private restaurarFoco(): void {
    const elemento = this.elementoFocadoAnterior;
    this.elementoFocadoAnterior = null;

    afterNextRender(
      () => {
        if (elemento?.isConnected) {
          elemento.focus();
        }
      },
      { injector: this.injector }
    );
  }

  private limparFormulario(): void {
    this.tituloNovaTarefa = '';
    this.descricaoNovaTarefa = '';
    this.prioridadeNovaTarefa = 'MEDIA';
    this.erroCadastro.set('');
  }

  alterarFiltroStatus(valor: string): void {
    const permitidos: Array<StatusTarefa | 'TODAS'> = [
      'TODAS',
      'PENDENTE',
      'EM_ANDAMENTO',
      'CONCLUIDA'
    ];

    if (permitidos.includes(valor as StatusTarefa | 'TODAS')) {
      this.filtroStatus.set(valor as StatusTarefa | 'TODAS');
    }
  }

  alterarFiltroPrioridade(valor: string): void {
    const permitidos: Array<PrioridadeTarefa | 'TODAS'> = [
      'TODAS',
      'BAIXA',
      'MEDIA',
      'ALTA'
    ];

    if (
      permitidos.includes(
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

  nomePrioridade(prioridade: PrioridadeTarefa): string {
    const nomes: Record<PrioridadeTarefa, string> = {
      BAIXA: 'Baixa',
      MEDIA: 'Média',
      ALTA: 'Alta'
    };

    return nomes[prioridade] ?? 'Desconhecida';
  }
}
