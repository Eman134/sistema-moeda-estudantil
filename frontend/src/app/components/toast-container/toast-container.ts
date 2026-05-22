import { ChangeDetectionStrategy, Component, inject } from '@angular/core';

import { Toast, ToastTipo } from '../../models/toast.models';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-toast-container',
  templateUrl: './toast-container.html',
  styleUrl: './toast-container.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ToastContainer {
  private readonly toastService = inject(ToastService);

  protected readonly toasts = this.toastService.toasts;

  protected fecharToast(id: number): void {
    this.toastService.fechar(id);
  }

  protected obterRole(toast: Toast): 'alert' | 'status' {
    return toast.tipo === 'erro' ? 'alert' : 'status';
  }

  protected obterClassesTipo(tipo: ToastTipo): string {
    switch (tipo) {
      case 'sucesso':
        return 'border-emerald-300/30 bg-emerald-500/15 text-emerald-100';
      case 'aviso':
        return 'border-yellow-300/30 bg-yellow-500/15 text-yellow-100';
      case 'erro':
        return 'border-red-300/30 bg-red-500/15 text-red-100';
      case 'informativo':
        return 'border-sky-300/30 bg-sky-500/15 text-sky-100';
    }
  }

  protected obterIndicadorTipo(tipo: ToastTipo): string {
    switch (tipo) {
      case 'sucesso':
        return 'bg-emerald-300';
      case 'aviso':
        return 'bg-yellow-300';
      case 'erro':
        return 'bg-red-300';
      case 'informativo':
        return 'bg-sky-300';
    }
  }
}
