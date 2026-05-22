export interface EnvioMoedasItemRequest {
  alunoId: number;
  valor: number;
  motivo: string;
}

export interface EnviarMoedasRequest {
  envios: EnvioMoedasItemRequest[];
}

export interface EnvioMoedasItemResponse {
  transacaoId: number;
  alunoId: number;
  nomeAluno: string;
  valor: number;
  motivo: string;
}

export interface EnviarMoedasResponse {
  saldoProfessor: number;
  totalEnviado: number;
  envios: EnvioMoedasItemResponse[];
}
