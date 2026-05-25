package br.pucminas.karv_coins.dto.notification;

import java.io.Serializable;
import java.time.LocalDateTime;

public record TransacaoNotificacaoDto(
        String alunoEmail,
        String alunoNome,
        Double saldoAluno,
        String professorEmail,
        String professorNome,
        Double saldoProfessor,
        Double valor,
        String motivo,
        LocalDateTime data
) implements Serializable {}