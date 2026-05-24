package br.pucminas.karv_coins.event;

import br.pucminas.karv_coins.dto.notification.TransacaoNotificacaoDto;
import java.util.List;

public record MoedasEnviadasEvent(List<TransacaoNotificacaoDto> transacoes) {
}
