package br.pucminas.karv_coins.dto.response;

import br.pucminas.karv_coins.model.Transacao;

public record EnvioMoedasItemResponseDto(
        Long transacaoId,
        Long alunoId,
        String nomeAluno,
        Double valor,
        String motivo
) {
    public static EnvioMoedasItemResponseDto from(Transacao transacao) {
        return new EnvioMoedasItemResponseDto(
                transacao.getId(),
                transacao.getAluno().getId(),
                transacao.getAluno().getNome(),
                transacao.getValor(),
                transacao.getMotivo()
        );
    }
}
