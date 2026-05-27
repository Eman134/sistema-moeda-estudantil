package br.pucminas.karv_coins.dto.response;

import br.pucminas.karv_coins.model.Resgate;
import java.time.LocalDateTime;

public record ResgateVerificacaoResponseDto(
        Long id,
        String codigoCupom,
        Long vantagemId,
        String descricaoVantagem,
        String nomeEmpresa,
        Long alunoId,
        String nomeAluno,
        String emailAluno,
        Double valorMoedas,
        LocalDateTime data,
        Boolean utilizado,
        Long utilizadoPorId,
        String nomeUtilizadoPor,
        String emailUtilizadoPor
) {
    public static ResgateVerificacaoResponseDto from(Resgate resgate) {
        return new ResgateVerificacaoResponseDto(
                resgate.getId(),
                resgate.getCodigoCupom(),
                resgate.getVantagem().getId(),
                resgate.getVantagem().getDescricao(),
                resgate.getVantagem().getEmpresa().getNome(),
                resgate.getAluno().getId(),
                resgate.getAluno().getNome(),
                resgate.getAluno().getEmail(),
                resgate.getValorMoedas(),
                resgate.getData(),
                resgate.getUtilizadoPor() != null,
                resgate.getUtilizadoPor() == null ? null : resgate.getUtilizadoPor().getId(),
                resgate.getUtilizadoPor() == null ? null : resgate.getUtilizadoPor().getNome(),
                resgate.getUtilizadoPor() == null ? null : resgate.getUtilizadoPor().getEmail()
        );
    }
}
