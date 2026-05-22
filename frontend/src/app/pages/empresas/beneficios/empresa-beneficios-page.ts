import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

import { Vantagem } from '../../../models/vantagem.models';
import { ToastService } from '../../../services/toast.service';
import { VantagensService } from '../../../services/vantagens.service';

@Component({
  selector: 'app-empresa-beneficios-page',
  imports: [ReactiveFormsModule],
  templateUrl: './empresa-beneficios-page.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EmpresaBeneficiosPage {
  private readonly vantagensService = inject(VantagensService);
  private readonly toastService = inject(ToastService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly beneficiosGerenciados = signal<Vantagem[]>([]);
  protected readonly carregandoBeneficios = signal(false);
  protected readonly salvandoBeneficio = signal(false);
  protected readonly modalAberto = signal(false);
  protected readonly beneficioEmEdicao = signal<Vantagem | null>(null);

  protected readonly beneficioForm = new FormGroup({
    descricao: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required],
    }),
    fotoUrl: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required],
    }),
    custoMoedas: new FormControl<number | null>(null, {
      validators: [Validators.required, Validators.min(1)],
    }),
  });

  protected readonly tituloModal = computed(() =>
    this.beneficioEmEdicao() ? 'Editar benefício' : 'Adicionar benefício',
  );
  protected readonly textoBotaoSalvar = computed(() =>
    this.beneficioEmEdicao() ? 'Salvar alterações' : 'Publicar benefício',
  );

  constructor() {
    this.carregarBeneficios();
  }

  protected abrirCriacaoBeneficio(): void {
    this.beneficioEmEdicao.set(null);
    this.limparFormulario();
    this.modalAberto.set(true);
  }

  protected abrirEdicaoBeneficio(beneficio: Vantagem): void {
    this.beneficioEmEdicao.set(beneficio);
    this.beneficioForm.setValue({
      descricao: beneficio.descricao,
      fotoUrl: beneficio.fotoUrl,
      custoMoedas: beneficio.custoMoedas,
    });
    this.beneficioForm.markAsPristine();
    this.modalAberto.set(true);
  }

  protected fecharModal(): void {
    if (this.salvandoBeneficio()) {
      return;
    }

    this.modalAberto.set(false);
    this.beneficioEmEdicao.set(null);
    this.limparFormulario();
  }

  protected salvarBeneficio(): void {
    this.marcarFormularioComoTocado();

    if (this.formularioInvalido() || this.salvandoBeneficio()) {
      return;
    }

    const valores = this.beneficioForm.getRawValue();
    const request = {
      descricao: valores.descricao.trim(),
      fotoUrl: valores.fotoUrl.trim(),
      custoMoedas: valores.custoMoedas ?? 0,
    };
    const beneficioAtual = this.beneficioEmEdicao();
    const operacao = beneficioAtual
      ? this.vantagensService.atualizarVantagem(beneficioAtual.id, request)
      : this.vantagensService.criarVantagem(request);

    this.salvandoBeneficio.set(true);
    operacao.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (beneficio) => {
        this.salvandoBeneficio.set(false);
        this.modalAberto.set(false);
        this.atualizarBeneficioNaLista(beneficio);
        this.beneficioEmEdicao.set(null);
        this.limparFormulario();
        this.toastService.sucesso(
          beneficioAtual ? 'Benefício atualizado' : 'Benefício publicado',
          'A vantagem já está disponível na listagem da empresa.',
        );
      },
      error: (erro: unknown) => {
        this.salvandoBeneficio.set(false);
        this.toastService.erro('Erro ao salvar benefício', this.extrairMensagemErro(erro));
      },
    });
  }

  protected formularioInvalido(): boolean {
    return this.beneficioForm.invalid;
  }

  protected campoInvalido(campo: keyof typeof this.beneficioForm.controls): boolean {
    const control = this.beneficioForm.controls[campo];

    return control.invalid && (control.dirty || control.touched);
  }

  protected fotoUrlPreview(): string {
    return this.beneficioForm.controls.fotoUrl.value.trim();
  }

  protected formatarMoedas(valor: number): string {
    return `${valor} moedas`;
  }

  private carregarBeneficios(): void {
    this.carregandoBeneficios.set(true);
    this.vantagensService
      .listarMinhasVantagens()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (beneficios) => {
          this.beneficiosGerenciados.set(beneficios);
          this.carregandoBeneficios.set(false);
        },
        error: (erro: unknown) => {
          this.carregandoBeneficios.set(false);
          this.toastService.erro('Erro ao carregar benefícios', this.extrairMensagemErro(erro));
        },
      });
  }

  private atualizarBeneficioNaLista(beneficio: Vantagem): void {
    this.beneficiosGerenciados.update((beneficios) => {
      const beneficioExiste = beneficios.some((item) => item.id === beneficio.id);

      if (!beneficioExiste) {
        return [beneficio, ...beneficios];
      }

      return beneficios.map((item) => (item.id === beneficio.id ? beneficio : item));
    });
  }

  private limparFormulario(): void {
    this.beneficioForm.reset({
      descricao: '',
      fotoUrl: '',
      custoMoedas: null,
    });
  }

  private marcarFormularioComoTocado(): void {
    this.beneficioForm.markAllAsTouched();
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
