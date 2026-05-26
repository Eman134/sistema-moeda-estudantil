package br.pucminas.karv_coins.dto.response;

import br.pucminas.karv_coins.model.Resgate;
import br.pucminas.karv_coins.model.Transacao;
import java.time.LocalDateTime;

public record ExtratoItemResponseDto(
        Long id,
        String tipo,
        String descricao,
        String contraparte,
        String codigoCupom,
        Double quantidadeMoedas,
        LocalDateTime data
) {
    public static ExtratoItemResponseDto creditoAluno(Transacao transacao) {
        return new ExtratoItemResponseDto(
                transacao.getId(),
                "CREDITO",
                transacao.getMotivo(),
                transacao.getProfessor().getNome(),
                null,
                transacao.getValor(),
                transacao.getData()
        );
    }

    public static ExtratoItemResponseDto debitoProfessor(Transacao transacao) {
        return new ExtratoItemResponseDto(
                transacao.getId(),
                "DEBITO",
                transacao.getMotivo(),
                transacao.getAluno().getNome(),
                null,
                -transacao.getValor(),
                transacao.getData()
        );
    }

    public static ExtratoItemResponseDto resgateAluno(Resgate resgate) {
        return new ExtratoItemResponseDto(
                resgate.getId(),
                "RESGATE",
                "Resgate de vantagem: %s".formatted(resgate.getVantagem().getDescricao()),
                resgate.getVantagem().getEmpresa().getNome(),
                resgate.getCodigoCupom(),
                -resgate.getValorMoedas(),
                resgate.getData()
        );
    }
}
