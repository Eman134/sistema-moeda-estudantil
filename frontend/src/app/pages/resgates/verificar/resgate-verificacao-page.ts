import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { debounceTime, distinctUntilChanged } from 'rxjs';

import { AlunoSelecao } from '../../../models/aluno.models';
import { ResgateVerificacao } from '../../../models/vantagem.models';
import { AlunosService } from '../../../services/alunos.service';
import { ToastService } from '../../../services/toast.service';
import { VantagensService } from '../../../services/vantagens.service';

@Component({
  selector: 'app-resgate-verificacao-page',
  imports: [ReactiveFormsModule],
  templateUrl: './resgate-verificacao-page.html',
  styleUrl: './resgate-verificacao-page.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ResgateVerificacaoPage {
  private readonly route = inject(ActivatedRoute);
  private readonly alunosService = inject(AlunosService);
  private readonly vantagensService = inject(VantagensService);
  private readonly toastService = inject(ToastService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly codigoCupom = this.route.snapshot.paramMap.get('codigoCupom');

  protected readonly pesquisaControl = new FormControl('', { nonNullable: true });
  protected readonly resgate = signal<ResgateVerificacao | null>(null);
  protected readonly alunos = signal<AlunoSelecao[]>([]);
  protected readonly alunoSelecionado = signal<AlunoSelecao | null>(null);
  protected readonly carregando = signal(true);
  protected readonly carregandoAlunos = signal(false);
  protected readonly aprovando = signal(false);
  protected readonly erro = signal<string | null>(null);
  protected readonly totalAlunos = signal(0);
  protected readonly totalPaginas = signal(0);
  protected readonly paginaAtual = signal(0);
  protected readonly tamanhoPagina = signal(5);

  protected readonly resgateUtilizado = computed(() => Boolean(this.resgate()?.utilizado));
  protected readonly paginaExibida = computed(() => this.paginaAtual() + 1);
  protected readonly podeVoltarPagina = computed(() => this.paginaAtual() > 0 && !this.carregandoAlunos());
  protected readonly podeAvancarPagina = computed(
    () => this.paginaAtual() + 1 < this.totalPaginas() && !this.carregandoAlunos(),
  );

  constructor() {
    if (!this.codigoCupom) {
      this.carregando.set(false);
      this.erro.set('Código de cupom não informado.');
      return;
    }

    this.pesquisaControl.valueChanges
      .pipe(debounceTime(500), distinctUntilChanged(), takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.paginaAtual.set(0);
        this.alunoSelecionado.set(null);
        this.carregarAlunos();
      });

    this.vantagensService
      .verificarResgate(this.codigoCupom)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (resgate) => {
          this.resgate.set(resgate);
          this.carregando.set(false);
          if (!resgate.utilizado) {
            this.carregarAlunos();
          }
        },
        error: (erro: unknown) => {
          this.carregando.set(false);
          const mensagem = this.extrairMensagemErro(erro);
          this.erro.set(mensagem);
          this.toastService.erro('Resgate não encontrado', mensagem);
        },
      });
  }

  protected selecionarAluno(aluno: AlunoSelecao): void {
    if (this.resgateUtilizado() || this.aprovando()) return;
    this.alunoSelecionado.set(aluno);
  }

  protected alunoEstaSelecionado(alunoId: number): boolean {
    return this.alunoSelecionado()?.id === alunoId;
  }

  protected aprovarUtilizacao(): void {
    const aluno = this.alunoSelecionado();
    if (!this.codigoCupom || !aluno || this.aprovando() || this.resgateUtilizado()) return;

    this.aprovando.set(true);
    this.vantagensService
      .aprovarUtilizacaoResgate(this.codigoCupom, { alunoId: aluno.id })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (resgate) => {
          this.resgate.set(resgate);
          this.aprovando.set(false);
          this.toastService.sucesso('Resgate aprovado', `${aluno.nome} foi marcado como usuário da vantagem.`);
        },
        error: (erro: unknown) => {
          this.aprovando.set(false);
          this.toastService.erro('Erro ao aprovar resgate', this.extrairMensagemErro(erro));
        },
      });
  }

  protected paginaAnterior(): void {
    if (!this.podeVoltarPagina()) return;
    this.paginaAtual.update((pagina) => pagina - 1);
    this.carregarAlunos();
  }

  protected proximaPagina(): void {
    if (!this.podeAvancarPagina()) return;
    this.paginaAtual.update((pagina) => pagina + 1);
    this.carregarAlunos();
  }

  protected obterIniciaisAluno(nome: string): string {
    return nome
      .split(' ')
      .filter((parte) => parte.length > 0)
      .slice(0, 2)
      .map((parte) => parte[0].toUpperCase())
      .join('');
  }

  protected formatarMoedas(valor: number): string {
    return `${valor} moedas`;
  }

  protected formatarData(valor: string): string {
    return new Intl.DateTimeFormat('pt-BR', {
      dateStyle: 'short',
      timeStyle: 'short',
    }).format(new Date(valor));
  }

  private carregarAlunos(): void {
    if (this.resgateUtilizado()) return;

    this.carregandoAlunos.set(true);
    this.alunosService
      .listarAlunosParaSelecao(this.paginaAtual(), this.tamanhoPagina(), this.pesquisaControl.value)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (pagina) => {
          this.alunos.set(pagina.content);
          this.totalAlunos.set(pagina.totalElements);
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

    return 'Não foi possível verificar este resgate para a empresa autenticada.';
  }
}
