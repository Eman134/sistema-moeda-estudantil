package br.pucminas.karv_coins.dto.response;

import br.pucminas.karv_coins.model.Resgate;
import java.time.LocalDateTime;

public record ResgateResponseDto(
        Long id,
        Long vantagemId,
        String descricaoVantagem,
        String nomeEmpresa,
        Double valorMoedas,
        String codigoCupom,
        LocalDateTime data,
        Double saldoAtual,
        String urlVerificacao
) {
    public static ResgateResponseDto from(Resgate resgate, Double saldoAtual, String urlVerificacao) {
        return new ResgateResponseDto(
                resgate.getId(),
                resgate.getVantagem().getId(),
                resgate.getVantagem().getDescricao(),
                resgate.getVantagem().getEmpresa().getNome(),
                resgate.getValorMoedas(),
                resgate.getCodigoCupom(),
                resgate.getData(),
                saldoAtual,
                urlVerificacao
        );
    }
}
