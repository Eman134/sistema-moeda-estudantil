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
        String detalhe,
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
                detalheUtilizacao(resgate),
                -resgate.getValorMoedas(),
                resgate.getData()
        );
    }

    public static ExtratoItemResponseDto usoResgateAluno(Resgate resgate) {
        return new ExtratoItemResponseDto(
                resgate.getId(),
                "RESGATE",
                "Uso de vantagem: %s".formatted(resgate.getVantagem().getDescricao()),
                resgate.getVantagem().getEmpresa().getNome(),
                resgate.getCodigoCupom(),
                "Comprado por: %s".formatted(resgate.getAluno().getNome()),
                0.0,
                resgate.getData()
        );
    }

    private static String detalheUtilizacao(Resgate resgate) {
        if (resgate.getUtilizadoPor() == null || resgate.getUtilizadoPor().getId().equals(resgate.getAluno().getId())) {
            return null;
        }

        return "Utilizado por: %s".formatted(resgate.getUtilizadoPor().getNome());
    }
}
