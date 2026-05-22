package br.pucminas.karv_coins.dto.response;

import br.pucminas.karv_coins.model.Vantagem;

public record VantagemResponseDto(
        Long id,
        String descricao,
        String fotoUrl,
        Double custoMoedas,
        Long empresaId,
        String nomeEmpresa
) {
    public static VantagemResponseDto from(Vantagem vantagem) {
        return new VantagemResponseDto(
                vantagem.getId(),
                vantagem.getDescricao(),
                vantagem.getFotoUrl(),
                vantagem.getCustoMoedas(),
                vantagem.getEmpresa().getId(),
                vantagem.getEmpresa().getNome()
        );
    }
}
