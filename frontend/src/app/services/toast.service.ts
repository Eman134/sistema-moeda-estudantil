import { Injectable, signal } from '@angular/core';

import { Toast, ToastTipo } from '../models/toast.models';

@Injectable({ providedIn: 'root' })
export class ToastService {
  private proximoId = 1;
  private readonly timers = new Map<number, ReturnType<typeof setTimeout>>();
  private readonly toastsSignal = signal<Toast[]>([]);

  readonly toasts = this.toastsSignal.asReadonly();

  mostrar(tipo: ToastTipo, titulo: string, mensagem?: string, duracaoMs = 5000): void {
    const id = this.proximoId++;
    const toast: Toast = { id, tipo, titulo, mensagem, duracaoMs };

    this.toastsSignal.update((toasts) => [...toasts, toast]);

    if (duracaoMs > 0) {
      const timer = setTimeout(() => this.fechar(id), duracaoMs);
      this.timers.set(id, timer);
    }
  }

  sucesso(titulo: string, mensagem?: string, duracaoMs?: number): void {
    this.mostrar('sucesso', titulo, mensagem, duracaoMs);
  }

  aviso(titulo: string, mensagem?: string, duracaoMs?: number): void {
    this.mostrar('aviso', titulo, mensagem, duracaoMs);
  }

  informativo(titulo: string, mensagem?: string, duracaoMs?: number): void {
    this.mostrar('informativo', titulo, mensagem, duracaoMs);
  }

  erro(titulo: string, mensagem?: string, duracaoMs?: number): void {
    this.mostrar('erro', titulo, mensagem, duracaoMs);
  }

  fechar(id: number): void {
    const timer = this.timers.get(id);
    if (timer) {
      clearTimeout(timer);
      this.timers.delete(id);
    }

    this.toastsSignal.update((toasts) => toasts.filter((toast) => toast.id !== id));
  }

  limparTodos(): void {
    for (const timer of this.timers.values()) {
      clearTimeout(timer);
    }
    this.timers.clear();
    this.toastsSignal.set([]);
  }
}
