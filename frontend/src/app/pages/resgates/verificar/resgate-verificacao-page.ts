import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute } from '@angular/router';

import { ResgateVerificacao } from '../../../models/vantagem.models';
import { ToastService } from '../../../services/toast.service';
import { VantagensService } from '../../../services/vantagens.service';

@Component({
  selector: 'app-resgate-verificacao-page',
  templateUrl: './resgate-verificacao-page.html',
  styleUrl: './resgate-verificacao-page.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ResgateVerificacaoPage {
  private readonly route = inject(ActivatedRoute);
  private readonly vantagensService = inject(VantagensService);
  private readonly toastService = inject(ToastService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly resgate = signal<ResgateVerificacao | null>(null);
  protected readonly carregando = signal(true);
  protected readonly erro = signal<string | null>(null);

  constructor() {
    const codigoCupom = this.route.snapshot.paramMap.get('codigoCupom');
    if (!codigoCupom) {
      this.carregando.set(false);
      this.erro.set('Código de cupom não informado.');
      return;
    }

    this.vantagensService
      .verificarResgate(codigoCupom)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (resgate) => {
          this.resgate.set(resgate);
          this.carregando.set(false);
        },
        error: (erro: unknown) => {
          this.carregando.set(false);
          const mensagem = this.extrairMensagemErro(erro);
          this.erro.set(mensagem);
          this.toastService.erro('Resgate não encontrado', mensagem);
        },
      });
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
