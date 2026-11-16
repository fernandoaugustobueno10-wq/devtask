
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

import { AuthService } from '../../services/auth.service';

import {
  AtualizarTarefaRequest,
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
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly injector = inject(Injector);
  private readonly document = inject(DOCUMENT);

  private elementoFocadoAnterior: HTMLElement | null = null;
  private elementoFocadoExclusao: HTMLElement | null = null;

  @ViewChild('modalNovaTarefa')
  private modalNovaTarefa?: ElementRef<HTMLElement>;

  @ViewChild('campoTituloNovaTarefa')
  private campoTituloNovaTarefa?: ElementRef<HTMLInputElement>;

  @ViewChild('modalConfirmacaoExclusao')
  private modalConfirmacaoExclusao?: ElementRef<HTMLElement>;

  @ViewChild('botaoCancelarExclusao')
  private botaoCancelarExclusao?: ElementRef<HTMLButtonElement>;

  readonly tarefas = signal<Tarefa[]>([]);
  readonly carregando = signal(true);
  readonly mensagemErro = signal('');

  readonly saindo = signal(false);
  readonly erroLogout = signal('');

  readonly filtroStatus = signal<StatusTarefa | 'TODAS'>(
    'TODAS'
  );

  readonly filtroPrioridade = signal<
    PrioridadeTarefa | 'TODAS'
  >('TODAS');

  readonly exibindoFormulario = signal(false);
  readonly salvandoTarefa = signal(false);
  readonly tarefaEmEdicaoId = signal<number | null>(null);

  readonly erroCadastro = signal('');
  readonly sucessoCadastro = signal('');

  readonly tarefaParaExcluir = signal<Tarefa | null>(null);
  readonly excluindoTarefa = signal(false);
  readonly erroExclusao = signal('');

  tituloNovaTarefa = '';
  descricaoNovaTarefa = '';
  prioridadeNovaTarefa: PrioridadeTarefa = 'MEDIA';
  statusTarefa: StatusTarefa = 'PENDENTE';

  readonly modoEdicao = computed(
    () => this.tarefaEmEdicaoId() !== null
  );

  readonly tituloFormulario = computed(
    () => this.modoEdicao() ? 'Editar tarefa' : 'Nova tarefa'
  );

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
    if (this.saindo()) {
      return;
    }

    this.carregando.set(true);
    this.mensagemErro.set('');

    this.tarefaService.listar().subscribe({
      next: (tarefas) => {
        if (this.saindo()) {
          return;
        }

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
        if (this.saindo()) {
          return;
        }

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
              'Não foi possível conectar ao servidor.'
            );
            return;
          }
        }

        this.mensagemErro.set(
          'Não foi possível carregar suas tarefas.'
        );
      }
    });
  }

  sair(): void {
    if (
      this.saindo() ||
      this.salvandoTarefa() ||
      this.excluindoTarefa() ||
      this.exibindoFormulario() ||
      this.tarefaParaExcluir() !== null
    ) {
      return;
    }

    this.saindo.set(true);
    this.erroLogout.set('');
    this.sucessoCadastro.set('');

    this.authService.logout().subscribe({
      next: () => {
        this.tarefas.set([]);
        this.mensagemErro.set('');
        this.carregando.set(false);

        void this.router.navigateByUrl(
          '/login',
          { replaceUrl: true }
        ).then((navegou) => {
          if (!navegou) {
            this.erroLogout.set(
              'A sessão foi encerrada, mas não foi possível ' +
              'abrir a tela de login.'
            );
          }

          this.saindo.set(false);
        }).catch(() => {
          this.saindo.set(false);
          this.erroLogout.set(
            'A sessão foi encerrada, mas houve um erro ' +
            'ao abrir a tela de login.'
          );
        });
      },

      error: (erro: unknown) => {
        this.saindo.set(false);

        if (erro instanceof HttpErrorResponse) {
          if (erro.status === 0) {
            this.erroLogout.set(
              'Não foi possível conectar ao servidor ' +
              'para encerrar a sessão.'
            );
            return;
          }

          if (erro.status === 401 || erro.status === 403) {
            this.erroLogout.set(
              'Não foi possível confirmar o logout. ' +
              'Atualize a página ou faça login novamente.'
            );
            return;
          }
        }

        this.erroLogout.set(
          'Não foi possível encerrar a sessão. ' +
          'Tente novamente.'
        );
      }
    });
  }

  abrirFormulario(): void {
    if (
      this.saindo() ||
      this.salvandoTarefa() ||
      this.excluindoTarefa() ||
      this.tarefaParaExcluir()
    ) {
      return;
    }

    this.limparFormulario();
    this.tarefaEmEdicaoId.set(null);
    this.abrirModal();
  }

  editarTarefa(tarefa: Tarefa): void {
    if (
      this.saindo() ||
      this.salvandoTarefa() ||
      this.excluindoTarefa() ||
      this.tarefaParaExcluir()
    ) {
      return;
    }

    if (!Number.isSafeInteger(tarefa.id) || tarefa.id <= 0) {
      this.mensagemErro.set(
        'Não foi possível identificar a tarefa.'
      );
      return;
    }

    this.tituloNovaTarefa = tarefa.titulo;
    this.descricaoNovaTarefa = tarefa.descricao ?? '';
    this.prioridadeNovaTarefa = tarefa.prioridade;
    this.statusTarefa = tarefa.status;

    this.tarefaEmEdicaoId.set(tarefa.id);
    this.abrirModal();
  }

  private abrirModal(): void {
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
    this.tarefaEmEdicaoId.set(null);
    this.exibindoFormulario.set(false);
    this.restaurarFoco();
  }

  abrirConfirmacaoExclusao(tarefa: Tarefa): void {
    if (
      this.saindo() ||
      this.exibindoFormulario() ||
      this.salvandoTarefa() ||
      this.excluindoTarefa() ||
      this.tarefaParaExcluir()
    ) {
      return;
    }

    if (
      !Number.isSafeInteger(tarefa.id) ||
      tarefa.id <= 0
    ) {
      this.mensagemErro.set(
        'Não foi possível identificar a tarefa.'
      );
      return;
    }

    const elementoAtivo = this.document.activeElement;

    this.elementoFocadoExclusao =
      elementoAtivo instanceof HTMLElement
        ? elementoAtivo
        : null;

    this.erroExclusao.set('');
    this.sucessoCadastro.set('');
    this.tarefaParaExcluir.set(tarefa);

    afterNextRender(
      () => {
        if (this.tarefaParaExcluir()) {
          this.botaoCancelarExclusao?.nativeElement.focus();
        }
      },
      { injector: this.injector }
    );
  }

  cancelarExclusao(): void {
    if (this.excluindoTarefa()) {
      return;
    }

    this.tarefaParaExcluir.set(null);
    this.erroExclusao.set('');
    this.restaurarFocoExclusao();
  }

  confirmarExclusao(): void {
    const tarefa = this.tarefaParaExcluir();

    if (
      !tarefa ||
      this.excluindoTarefa() ||
      this.saindo()
    ) {
      return;
    }

    this.erroExclusao.set('');
    this.excluindoTarefa.set(true);

    this.tarefaService.excluir(tarefa.id).subscribe({
      next: () => {
        this.excluindoTarefa.set(false);
        this.tarefaParaExcluir.set(null);
        this.erroExclusao.set('');

        this.tarefas.update((tarefas) =>
          tarefas.filter((item) => item.id !== tarefa.id)
        );

        this.sucessoCadastro.set(
          'Tarefa excluída com sucesso!'
        );

        this.restaurarFocoExclusao();
        this.carregarTarefas();
      },

      error: (erro: unknown) => {
        this.excluindoTarefa.set(false);

        if (erro instanceof HttpErrorResponse) {
          if (erro.status === 401 || erro.status === 403) {
            this.erroExclusao.set(
              'Sessão expirada ou operação não autorizada. ' +
              'Faça login novamente.'
            );
            return;
          }

          if (erro.status === 404) {
            this.erroExclusao.set(
              'A tarefa não foi encontrada ou não ' +
              'pertence à sua conta.'
            );
            return;
          }

          if (erro.status === 0) {
            this.erroExclusao.set(
              'Não foi possível conectar ao servidor.'
            );
            return;
          }
        }

        this.erroExclusao.set(
          'Não foi possível excluir a tarefa. Tente novamente.'
        );
      }
    });
  }

  private restaurarFocoExclusao(): void {
    const elemento = this.elementoFocadoExclusao;
    this.elementoFocadoExclusao = null;

    afterNextRender(
      () => {
        if (elemento?.isConnected) {
          elemento.focus();
        }
      },
      { injector: this.injector }
    );
  }

  @HostListener('document:keydown', ['$event'])
  aoPressionarTecla(evento: KeyboardEvent): void {
    if (this.tarefaParaExcluir()) {
      if (evento.key === 'Escape') {
        evento.preventDefault();
        this.cancelarExclusao();
        return;
      }

      if (evento.key === 'Tab') {
        this.controlarFoco(
          evento,
          this.modalConfirmacaoExclusao?.nativeElement
        );
      }

      return;
    }

    if (!this.exibindoFormulario()) {
      return;
    }

    if (evento.key === 'Escape') {
      evento.preventDefault();
      this.fecharFormulario();
      return;
    }

    if (evento.key === 'Tab') {
      this.controlarFoco(
        evento,
        this.modalNovaTarefa?.nativeElement
      );
    }
  }

  private controlarFoco(
    evento: KeyboardEvent,
    modal: HTMLElement | undefined
  ): void {
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
    this.salvarTarefa();
  }

  salvarTarefa(): void {
    if (
      this.saindo() ||
      this.salvandoTarefa() ||
      !this.exibindoFormulario()
    ) {
      return;
    }

    this.erroCadastro.set('');
    this.sucessoCadastro.set('');

    const titulo = this.tituloNovaTarefa.trim();
    const descricao = this.descricaoNovaTarefa.trim();
    const prioridade = this.prioridadeNovaTarefa;
    const status = this.statusTarefa;

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

    if (
      status !== 'PENDENTE' &&
      status !== 'EM_ANDAMENTO' &&
      status !== 'CONCLUIDA'
    ) {
      this.erroCadastro.set(
        'Selecione um status válido.'
      );
      return;
    }

    const tarefaId = this.tarefaEmEdicaoId();

    const dados: CriarTarefaRequest = {
      titulo,
      descricao: descricao || null,
      prioridade
    };

    this.salvandoTarefa.set(true);

    if (tarefaId !== null) {
      const dadosAtualizados: AtualizarTarefaRequest = {
        ...dados,
        status
      };

      this.tarefaService.atualizar(
        tarefaId,
        dadosAtualizados
      ).subscribe({
        next: () => {
          this.finalizarSalvamento(
            'Tarefa atualizada com sucesso!'
          );
        },
        error: (erro: unknown) => {
          this.tratarErroSalvamento(erro);
        }
      });

      return;
    }

    this.tarefaService.criar(dados).subscribe({
      next: () => {
        this.finalizarSalvamento(
          'Tarefa cadastrada com sucesso!'
        );
      },
      error: (erro: unknown) => {
        this.tratarErroSalvamento(erro);
      }
    });
  }

  private finalizarSalvamento(mensagem: string): void {
    this.salvandoTarefa.set(false);
    this.limparFormulario();
    this.tarefaEmEdicaoId.set(null);
    this.exibindoFormulario.set(false);
    this.restaurarFoco();

    this.filtroStatus.set('TODAS');
    this.filtroPrioridade.set('TODAS');
    this.sucessoCadastro.set(mensagem);

    this.carregarTarefas();
  }

  private tratarErroSalvamento(erro: unknown): void {
    this.salvandoTarefa.set(false);

    if (erro instanceof HttpErrorResponse) {
      if (erro.status === 400) {
        this.erroCadastro.set(
          'Dados inválidos. Confira os campos informados.'
        );
        return;
      }

      if (erro.status === 401 || erro.status === 403) {
        this.erroCadastro.set(
          'Não foi possível validar sua sessão ou ' +
          'sua autorização. Faça login novamente.'
        );
        return;
      }

      if (erro.status === 404) {
        this.erroCadastro.set(
          'Tarefa não encontrada ou indisponível ' +
          'para sua conta.'
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
      'Não foi possível salvar a tarefa. Tente novamente.'
    );
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
    this.statusTarefa = 'PENDENTE';
    this.erroCadastro.set('');
  }

  alterarFiltroStatus(valor: string): void {
    const permitidos: Array<StatusTarefa | 'TODAS'> = [
      'TODAS',
      'PENDENTE',
      'EM_ANDAMENTO',
      'CONCLUIDA'
    ];

    if (
      permitidos.includes(valor as StatusTarefa | 'TODAS')
    ) {
      this.filtroStatus.set(
        valor as StatusTarefa | 'TODAS'
      );
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
