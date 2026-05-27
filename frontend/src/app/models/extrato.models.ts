import { PaginaResponse } from './paginacao.models';

export interface ExtratoItem {
  id: number;
  tipo: 'CREDITO' | 'DEBITO' | 'RESGATE';
  descricao: string;
  contraparte: string;
  codigoCupom?: string | null;
  detalhe?: string | null;
  quantidadeMoedas: number;
  data: string;
}

export interface ExtratoResponse {
  saldo: number;
  lancamentos: PaginaResponse<ExtratoItem>;
}
