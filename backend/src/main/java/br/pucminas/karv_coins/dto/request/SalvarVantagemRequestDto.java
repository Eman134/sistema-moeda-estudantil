package br.pucminas.karv_coins.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SalvarVantagemRequestDto(
        @NotBlank(message = "A descrição é obrigatória")
        String descricao,

        @NotBlank(message = "A URL da foto é obrigatória")
        String fotoUrl,

        @NotNull(message = "O custo em moedas é obrigatório")
        @Positive(message = "O custo em moedas deve ser maior que zero")
        Double custoMoedas
) {
}
