package br.pucminas.karv_coins.dto.notification;

import java.io.Serializable;

public record EmailNotificacaoMessage(
        TipoEmailNotificacao tipo,
        TransacaoNotificacaoDto transacao,
        ResgateNotificacaoDto resgate
) implements Serializable {
    public static EmailNotificacaoMessage envioMoedas(TransacaoNotificacaoDto transacao) {
        return new EmailNotificacaoMessage(TipoEmailNotificacao.ENVIO_MOEDAS, transacao, null);
    }

    public static EmailNotificacaoMessage resgateVantagem(ResgateNotificacaoDto resgate) {
        return new EmailNotificacaoMessage(TipoEmailNotificacao.RESGATE_VANTAGEM, null, resgate);
    }
}
