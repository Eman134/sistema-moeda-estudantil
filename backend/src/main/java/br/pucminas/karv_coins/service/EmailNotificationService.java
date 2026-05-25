package br.pucminas.karv_coins.service;

import br.pucminas.karv_coins.config.MailProperties;
import br.pucminas.karv_coins.dto.notification.TransacaoNotificacaoDto;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService {
    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);
    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy '\u00e0s' HH:mm");

    private final MailProperties mailProperties;
    private final JavaMailSender mailSender;

    public EmailNotificationService(MailProperties mailProperties, JavaMailSender mailSender) {
        this.mailProperties = mailProperties;
        this.mailSender = mailSender;
    }

    public void notificarEnvioMoedas(List<TransacaoNotificacaoDto> transacoes) {
        if (transacoes == null || transacoes.isEmpty()) {
            log.warn("Solicitada notificacao de envio de moedas sem transacoes.");
            return;
        }
        for (TransacaoNotificacaoDto transacao : transacoes) {
            notificarEnvioMoedas(transacao);
        }
    }

    public void notificarEnvioMoedas(TransacaoNotificacaoDto transacao) {
        if (transacao == null) {
            log.warn("Solicitada notificacao de envio de moedas com transacao nula.");
            return;
        }
        notificarRecebimentoAluno(transacao);
        notificarEnvioProfessor(transacao);
    }

    public void notificarRecebimentoAluno(TransacaoNotificacaoDto transacao) {
        String dataFormatada = transacao.data().format(FORMATO_DATA);
        String assunto = "[%s] Voc\u00ea recebeu moedas".formatted(mailProperties.appName());
        String corpo = """
                Ol\u00e1, %s!

                Voc\u00ea recebeu %.0f moeda(s) de %s.

                Motivo: %s
                Data da transa\u00e7\u00e3o: %s
                Seu saldo atual: %.0f moeda(s)

                Acesse o extrato no KarvCoins para acompanhar suas movimenta\u00e7\u00f5es.
                """.formatted(
                transacao.alunoNome(),
                transacao.valor(),
                transacao.professorNome(),
                transacao.motivo(),
                dataFormatada,
                transacao.saldoAluno()
        );

        enviar(transacao.alunoEmail(), assunto, corpo);
    }

    public void notificarEnvioProfessor(TransacaoNotificacaoDto transacao) {
        String dataFormatada = transacao.data().format(FORMATO_DATA);
        String assunto = "[%s] Confirma\u00e7\u00e3o de envio de moedas".formatted(mailProperties.appName());
        String corpo = """
                Ol\u00e1, %s!

                Seu envio de %.0f moeda(s) para %s foi registrado com sucesso.

                Motivo: %s
                Data da transa\u00e7\u00e3o: %s
                Seu saldo atual: %.0f moeda(s)

                Consulte o extrato no KarvCoins para ver o hist\u00f3rico de distribui\u00e7\u00f5es.
                """.formatted(
                transacao.professorNome(),
                transacao.valor(),
                transacao.alunoNome(),
                transacao.motivo(),
                dataFormatada,
                transacao.saldoProfessor()
        );

        enviar(transacao.professorEmail(), assunto, corpo);
    }

    private void enviar(String destinatario, String assunto, String corpo) {
        if (!mailProperties.enabled()) {
            log.info(
                    "E-mail desabilitado (app.mail.enabled=false). Destinat\u00e1rio: {}, assunto: {}",
                    destinatario,
                    assunto
            );
            return;
        }
        if (mailProperties.from() == null || mailProperties.from().isBlank()) {
            log.error("E-mail habilitado, mas app.mail.from n\u00e3o foi configurado. Envio para {} cancelado.", destinatario);
            return;
        }

        try {
            SimpleMailMessage mensagem = new SimpleMailMessage();
            mensagem.setFrom(mailProperties.from());
            mensagem.setTo(destinatario);
            mensagem.setSubject(assunto);
            mensagem.setText(corpo);
            mailSender.send(mensagem);
            log.info("E-mail enviado para {}", destinatario);
        } catch (Exception e) {
            log.error("Falha ao enviar e-mail para {}: {}", destinatario, e.getMessage(), e);
        }
    }
}
