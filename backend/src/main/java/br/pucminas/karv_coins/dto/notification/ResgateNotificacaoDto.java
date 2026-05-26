package br.pucminas.karv_coins.dto.notification;

import java.io.Serializable;
import java.time.LocalDateTime;

public record ResgateNotificacaoDto(
        String alunoEmail,
        String alunoNome,
        Double saldoAluno,
        String empresaEmail,
        String empresaNome,
        String vantagemDescricao,
        Double valorMoedas,
        String codigoCupom,
        LocalDateTime data,
        String urlVerificacao
) implements Serializable {}
