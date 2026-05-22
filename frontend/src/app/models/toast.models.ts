export type ToastTipo = 'sucesso' | 'aviso' | 'informativo' | 'erro';

export interface Toast {
  id: number;
  tipo: ToastTipo;
  titulo: string;
  mensagem?: string;
  duracaoMs: number;
}
