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

interface EnvioIndividual {
  aluno: AlunoResumo;
  valor: number | null;
  motivo: string;
}

interface EnvioMultiploAluno {
  aluno: AlunoResumo;
  valor: number | null;
  motivo: string;
}

type EtapaEnvioMultiplo = 'inativo' | 'selecao' | 'preenchimento' | 'confirmacao';

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
  protected readonly envioIndividualAberto = signal<EnvioIndividual | null>(null);
  protected readonly confirmacaoPendente = signal<ConfirmacaoEnvioMoedas | null>(null);
  protected readonly etapaEnvioMultiplo = signal<EtapaEnvioMultiplo>('inativo');
  protected readonly alunosSelecionados = signal<Record<number, EnvioMultiploAluno>>({});
  protected readonly valorPadraoMultiplo = signal<number | null>(null);
  protected readonly motivoPadraoMultiplo = signal('');
  protected readonly padroesMultiploAberto = signal(false);

  protected readonly paginaExibida = computed(() => this.paginaAtual() + 1);
  protected readonly podeVoltarPagina = computed(() => this.paginaAtual() > 0 && !this.carregandoAlunos());
  protected readonly podeAvancarPagina = computed(
    () => this.paginaAtual() + 1 < this.totalPaginas() && !this.carregandoAlunos(),
  );
  protected readonly alunosSelecionadosLista = computed(() => Object.values(this.alunosSelecionados()));
  protected readonly totalSelecionados = computed(() => this.alunosSelecionadosLista().length);
  protected readonly totalEnvioMultiplo = computed(() =>
    this.alunosSelecionadosLista().reduce((total, item) => total + (item.valor ?? 0), 0),
  );
  protected readonly saldoAposEnvioMultiplo = computed(
    () => (this.saldoProfessor() ?? 0) - this.totalEnvioMultiplo(),
  );
  protected readonly envioMultiploValido = computed(() => {
    const selecionados = this.alunosSelecionadosLista();
    const saldo = this.saldoProfessor();

    return Boolean(
      selecionados.length > 0 &&
        saldo !== null &&
        this.totalEnvioMultiplo() <= saldo &&
        selecionados.every((item) => item.valor !== null && item.valor > 0 && item.motivo.trim()),
    );
  });
  protected readonly envioIndividualValido = computed(() => {
    const envio = this.envioIndividualAberto();
    if (!envio) return false;
    const saldo = this.saldoProfessor();
    return Boolean(
      envio.valor !== null && envio.valor > 0 && envio.motivo.trim() && saldo !== null && envio.valor <= saldo,
    );
  });

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

  protected abrirEnvioIndividual(aluno: AlunoResumo): void {
    if (this.etapaEnvioMultiplo() === 'selecao') return;
    this.envioIndividualAberto.set({ aluno, valor: null, motivo: '' });
  }

  protected fecharEnvioIndividual(): void {
    this.envioIndividualAberto.set(null);
  }

  protected atualizarValorIndividual(evento: Event): void {
    const campo = evento.target as HTMLInputElement;
    const valor = campo.value ? Number(campo.value) : null;
    this.envioIndividualAberto.update((envio) => (envio ? { ...envio, valor } : envio));
  }

  protected atualizarMotivoIndividual(evento: Event): void {
    const campo = evento.target as HTMLTextAreaElement;
    this.envioIndividualAberto.update((envio) => (envio ? { ...envio, motivo: campo.value } : envio));
  }

  protected confirmarEnvioIndividual(): void {
    const envio = this.envioIndividualAberto();
    if (!envio || !envio.valor || !envio.motivo.trim()) return;
    this.envioIndividualAberto.set(null);
    this.confirmacaoPendente.set({ aluno: envio.aluno, valor: envio.valor, motivo: envio.motivo.trim() });
  }

  protected iniciarEnvioMultiplo(): void {
    this.etapaEnvioMultiplo.set('selecao');
    this.envioIndividualAberto.set(null);
    this.confirmacaoPendente.set(null);
  }

  protected cancelarEnvioMultiplo(): void {
    if (this.enviandoMoedas()) {
      return;
    }

    this.etapaEnvioMultiplo.set('inativo');
    this.alunosSelecionados.set({});
    this.valorPadraoMultiplo.set(null);
    this.motivoPadraoMultiplo.set('');
  }

  protected alternarSelecaoAluno(aluno: AlunoResumo, evento: Event): void {
    const campo = evento.target as HTMLInputElement;

    this.alunosSelecionados.update((selecionados) => {
      const atualizados = { ...selecionados };

      if (campo.checked) {
        atualizados[aluno.id] = atualizados[aluno.id] ?? { aluno, valor: null, motivo: '' };
      } else {
        delete atualizados[aluno.id];
      }

      return atualizados;
    });
  }

  protected alunoEstaSelecionado(alunoId: number): boolean {
    return Boolean(this.alunosSelecionados()[alunoId]);
  }

  protected limparSelecaoMultipla(): void {
    this.alunosSelecionados.set({});
  }

  protected abrirPreenchimentoMultiplo(): void {
    if (this.totalSelecionados() < 2) {
      this.toastService.aviso('Selecione mais alunos', 'O envio múltiplo precisa de pelo menos dois alunos.');
      return;
    }

    this.etapaEnvioMultiplo.set('preenchimento');
  }

  protected alternarPadroesMultiplo(): void {
    this.padroesMultiploAberto.update((aberto) => !aberto);
  }

  protected atualizarValorPadraoMultiplo(evento: Event): void {
    const campo = evento.target as HTMLInputElement;
    this.valorPadraoMultiplo.set(campo.value ? Number(campo.value) : null);
  }

  protected atualizarMotivoPadraoMultiplo(evento: Event): void {
    const campo = evento.target as HTMLTextAreaElement;
    this.motivoPadraoMultiplo.set(campo.value);
  }

  protected aplicarPadroesMultiplo(): void {
    const valorPadrao = this.valorPadraoMultiplo();
    const motivoPadrao = this.motivoPadraoMultiplo().trim();

    if ((!valorPadrao || valorPadrao <= 0) && !motivoPadrao) {
      this.toastService.aviso('Preencha um padrão', 'Informe um valor, um motivo ou ambos antes de aplicar.');
      return;
    }

    this.alunosSelecionados.update((selecionados) => {
      const atualizados: Record<number, EnvioMultiploAluno> = {};
      for (const [alunoId, item] of Object.entries(selecionados)) {
        atualizados[Number(alunoId)] = {
          ...item,
          valor: valorPadrao && valorPadrao > 0 ? valorPadrao : item.valor,
          motivo: motivoPadrao || item.motivo,
        };
      }
      return atualizados;
    });
  }

  protected atualizarValorMultiplo(alunoId: number, evento: Event): void {
    const campo = evento.target as HTMLInputElement;
    const valor = campo.value ? Number(campo.value) : null;

    this.alunosSelecionados.update((selecionados) => ({
      ...selecionados,
      [alunoId]: { ...selecionados[alunoId], valor },
    }));
  }

  protected atualizarMotivoMultiplo(alunoId: number, evento: Event): void {
    const campo = evento.target as HTMLTextAreaElement;

    this.alunosSelecionados.update((selecionados) => ({
      ...selecionados,
      [alunoId]: { ...selecionados[alunoId], motivo: campo.value },
    }));
  }

  protected voltarParaPreenchimentoMultiplo(): void {
    this.etapaEnvioMultiplo.set('preenchimento');
  }

  protected abrirConfirmacaoMultipla(): void {
    if (!this.envioMultiploValido()) {
      this.toastService.aviso(
        'Revise os envios',
        'Todos os alunos precisam de valor, motivo e o total não pode exceder o saldo disponível.',
      );
      return;
    }

    this.etapaEnvioMultiplo.set('confirmacao');
  }

  protected confirmarEnvioMultiplo(): void {
    if (!this.envioMultiploValido() || this.enviandoMoedas()) {
      return;
    }

    const envios = this.alunosSelecionadosLista().map((item) => ({
      alunoId: item.aluno.id,
      valor: item.valor ?? 0,
      motivo: item.motivo.trim(),
    }));
    const total = this.totalEnvioMultiplo();
    const quantidadeAlunos = this.totalSelecionados();

    this.enviandoMoedas.set(true);
    this.moedasService
      .enviarMoedas({ envios })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.enviandoMoedas.set(false);
          this.toastService.sucesso(
            'Moedas enviadas',
            `${total} moedas distribuídas para ${quantidadeAlunos} aluno${quantidadeAlunos === 1 ? '' : 's'}.`,
          );
          this.cancelarEnvioMultiplo();
          this.carregarAlunos();
        },
        error: (erro: unknown) => {
          this.enviandoMoedas.set(false);
          this.toastService.erro('Erro ao enviar moedas', this.extrairMensagemErro(erro));
        },
      });
  }

  protected obterIniciaisAluno(nome: string): string {
    return nome
      .split(' ')
      .filter((p) => p.length > 0)
      .slice(0, 2)
      .map((p) => p[0].toUpperCase())
      .join('');
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
    this.iniciarEnvioMultiplo();
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
