package br.pucminas.karv_coins.dto.response;

import br.pucminas.karv_coins.model.Transacao;
import java.time.LocalDateTime;

public record ExtratoItemResponseDto(
        Long id,
        String descricao,
        String contraparte,
        Double quantidadeMoedas,
        LocalDateTime data
) {
    public static ExtratoItemResponseDto creditoAluno(Transacao transacao) {
        return new ExtratoItemResponseDto(
                transacao.getId(),
                transacao.getMotivo(),
                transacao.getProfessor().getNome(),
                transacao.getValor(),
                transacao.getData()
        );
    }

    public static ExtratoItemResponseDto debitoProfessor(Transacao transacao) {
        return new ExtratoItemResponseDto(
                transacao.getId(),
                transacao.getMotivo(),
                transacao.getAluno().getNome(),
                -transacao.getValor(),
                transacao.getData()
        );
    }
}
