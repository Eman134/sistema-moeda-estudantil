import { PaginaResponse } from './paginacao.models';

export interface ExtratoItem {
  id: number;
  descricao: string;
  contraparte: string;
  quantidadeMoedas: number;
  data: string;
}

export interface ExtratoResponse {
  saldo: number;
  lancamentos: PaginaResponse<ExtratoItem>;
}
