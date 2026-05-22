export interface Vantagem {
  id: number;
  descricao: string;
  fotoUrl: string;
  custoMoedas: number;
  empresaId: number;
  nomeEmpresa: string;
}

export interface SalvarVantagemRequest {
  descricao: string;
  fotoUrl: string;
  custoMoedas: number;
}
