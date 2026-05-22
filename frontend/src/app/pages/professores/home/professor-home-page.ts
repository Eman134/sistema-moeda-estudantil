import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormControl } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { debounceTime, distinctUntilChanged } from 'rxjs';

import { AlunoResumo } from '../../../models/aluno.models';
import { AlunosService } from '../../../services/alunos.service';
import { MoedasService } from '../../../services/moedas.service';
import { ProfessoresService } from '../../../services/professores.service';
import { ToastService } from '../../../services/toast.service';

interface ConfirmacaoEnvioMoedas {
  aluno: AlunoResumo;
  valor: number;
  motivo: string;
}

interface MotivoEnvioMoedas {
  aluno: AlunoResumo;
  valor: number;
  motivo: string;
}

@Component({
  selector: 'app-professor-home-page',
  imports: [ReactiveFormsModule],
  templateUrl: './professor-home-page.html',
  styleUrl: './professor-home-page.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProfessorHomePage {
  private readonly alunosService = inject(AlunosService);
  private readonly moedasService = inject(MoedasService);
  private readonly professoresService = inject(ProfessoresService);
  private readonly toastService = inject(ToastService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly pesquisaControl = new FormControl('', { nonNullable: true });
  protected readonly saldoProfessor = this.professoresService.saldoProfessor;
  protected readonly alunosRelacionados = signal<AlunoResumo[]>([]);
  protected readonly totalAlunosRelacionados = signal(0);
  protected readonly totalPaginas = signal(0);
  protected readonly paginaAtual = signal(0);
  protected readonly tamanhoPagina = signal(5);
  protected readonly carregandoAlunos = signal(false);
  protected readonly enviandoMoedas = signal(false);
  protected readonly motivoPendente = signal<MotivoEnvioMoedas | null>(null);
  protected readonly confirmacaoPendente = signal<ConfirmacaoEnvioMoedas | null>(null);
  protected readonly moedasPorAluno = signal<Record<number, number | null>>({});

  protected readonly paginaExibida = computed(() => this.paginaAtual() + 1);
  protected readonly podeVoltarPagina = computed(() => this.paginaAtual() > 0 && !this.carregandoAlunos());
  protected readonly podeAvancarPagina = computed(
    () => this.paginaAtual() + 1 < this.totalPaginas() && !this.carregandoAlunos(),
  );

  constructor() {
    this.carregarResumoProfessor();
    this.carregarAlunos();

    this.pesquisaControl.valueChanges
      .pipe(debounceTime(500), distinctUntilChanged(), takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.paginaAtual.set(0);
        this.carregarAlunos();
      });
  }

  protected atualizarQuantidadeMoedas(alunoId: number, evento: Event): void {
    const campo = evento.target as HTMLInputElement;
    const quantidade = Number(campo.value);

    this.moedasPorAluno.update((valoresAtuais) => ({
      ...valoresAtuais,
      [alunoId]: campo.value ? quantidade : null,
    }));
  }

  protected obterQuantidadeMoedas(alunoId: number): number | null {
    return this.moedasPorAluno()[alunoId] ?? null;
  }

  protected podeDistribuirMoedas(alunoId: number): boolean {
    const quantidade = this.obterQuantidadeMoedas(alunoId);
    const saldo = this.saldoProfessor();

    return Boolean(quantidade && quantidade > 0 && saldo !== null && quantidade <= saldo);
  }

  protected prepararEnvioMoedas(aluno: AlunoResumo): void {
    const valor = this.obterQuantidadeMoedas(aluno.id);

    if (!valor || valor <= 0) {
      this.toastService.aviso('Revise os dados', 'Informe uma quantidade válida de moedas.');
      return;
    }

    this.motivoPendente.set({ aluno, valor, motivo: '' });
  }

  protected atualizarMotivoPendente(evento: Event): void {
    const campo = evento.target as HTMLTextAreaElement;

    this.motivoPendente.update((motivoAtual) =>
      motivoAtual ? { ...motivoAtual, motivo: campo.value } : motivoAtual,
    );
  }

  protected podeContinuarParaConfirmacao(): boolean {
    return Boolean(this.motivoPendente()?.motivo.trim());
  }

  protected continuarParaConfirmacao(): void {
    const motivo = this.motivoPendente();
    if (!motivo || !motivo.motivo.trim()) {
      return;
    }

    this.motivoPendente.set(null);
    this.confirmacaoPendente.set({ ...motivo, motivo: motivo.motivo.trim() });
  }

  protected cancelarMotivo(): void {
    this.motivoPendente.set(null);
  }

  protected confirmarEnvioMoedas(): void {
    const confirmacao = this.confirmacaoPendente();
    if (!confirmacao || this.enviandoMoedas()) {
      return;
    }

    this.enviandoMoedas.set(true);

    this.moedasService
      .enviarMoedas({
        envios: [
          {
            alunoId: confirmacao.aluno.id,
            valor: confirmacao.valor,
            motivo: confirmacao.motivo,
          },
        ],
      })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.moedasPorAluno.update((valoresAtuais) => ({
            ...valoresAtuais,
            [confirmacao.aluno.id]: null,
          }));
          this.confirmacaoPendente.set(null);
          this.enviandoMoedas.set(false);
          this.toastService.sucesso(
            'Moedas enviadas',
            `${confirmacao.valor} moedas distribuídas para ${confirmacao.aluno.nome}.`,
          );
          this.carregarAlunos();
        },
        error: (erro: unknown) => {
          this.enviandoMoedas.set(false);
          this.toastService.erro('Erro ao enviar moedas', this.extrairMensagemErro(erro));
        },
      });
  }

  protected cancelarConfirmacao(): void {
    if (!this.enviandoMoedas()) {
      this.confirmacaoPendente.set(null);
    }
  }

  protected paginaAnterior(): void {
    if (!this.podeVoltarPagina()) {
      return;
    }
    this.paginaAtual.update((pagina) => pagina - 1);
    this.carregarAlunos();
  }

  protected proximaPagina(): void {
    if (!this.podeAvancarPagina()) {
      return;
    }
    this.paginaAtual.update((pagina) => pagina + 1);
    this.carregarAlunos();
  }

  protected abrirEnvioEmMassa(): void {
    this.toastService.informativo(
      'Em breve',
      'Envio em massa será habilitado na próxima etapa do frontend.',
    );
  }

  protected formatarMoedas(valor: number | null): string {
    return `${valor ?? 0} moedas`;
  }

  private carregarResumoProfessor(): void {
    this.professoresService
      .obterMeuResumo()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        error: (erro: unknown) => {
          this.toastService.erro('Erro ao carregar professor', this.extrairMensagemErro(erro));
        },
      });
  }

  private carregarAlunos(): void {
    this.carregandoAlunos.set(true);
    this.alunosService
      .listarAlunos(this.paginaAtual(), this.tamanhoPagina(), this.pesquisaControl.value)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (pagina) => {
          this.alunosRelacionados.set(pagina.content);
          this.totalAlunosRelacionados.set(pagina.totalElements);
          this.totalPaginas.set(pagina.totalPages);
          this.carregandoAlunos.set(false);
        },
        error: (erro: unknown) => {
          this.carregandoAlunos.set(false);
          this.toastService.erro('Erro ao carregar alunos', this.extrairMensagemErro(erro));
        },
      });
  }

  private extrairMensagemErro(erro: unknown): string {
    if (typeof erro === 'object' && erro !== null && 'error' in erro) {
      const httpError = erro as { error?: unknown };
      if (typeof httpError.error === 'string') {
        return httpError.error;
      }
    }

    return 'Não foi possível completar a operação. Tente novamente.';
  }
}
