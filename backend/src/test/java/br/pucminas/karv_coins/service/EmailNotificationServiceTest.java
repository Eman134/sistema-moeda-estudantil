package br.pucminas.karv_coins.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import br.pucminas.karv_coins.config.MailProperties;
import br.pucminas.karv_coins.dto.notification.TransacaoNotificacaoDto;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

class EmailNotificationServiceTest {

    @Test
    void naoEnviaQuandoEmailEstaDesabilitado() {
        FakeJavaMailSender mailSender = new FakeJavaMailSender();
        EmailNotificationService service = new EmailNotificationService(
                new MailProperties(false, true, "noreply@karvcoins.local", "KarvCoins"),
                mailSender
        );

        service.notificarRecebimentoAluno(transacao());

        assertEquals(0, mailSender.mensagensEnviadas.size());
    }

    @Test
    void enviaMensagemQuandoEmailEstaHabilitado() {
        FakeJavaMailSender mailSender = new FakeJavaMailSender();
        EmailNotificationService service = new EmailNotificationService(
                new MailProperties(true, true, "noreply@karvcoins.local", "KarvCoins"),
                mailSender
        );

        service.notificarRecebimentoAluno(transacao());

        assertEquals(1, mailSender.mensagensEnviadas.size());
        SimpleMailMessage mensagem = mailSender.mensagensEnviadas.getFirst();
        assertEquals("noreply@karvcoins.local", mensagem.getFrom());
        assertEquals("aluno@moeda.local", mensagem.getTo()[0]);
        assertEquals("[KarvCoins] Voc\u00ea recebeu moedas", mensagem.getSubject());
    }

    private static TransacaoNotificacaoDto transacao() {
        return new TransacaoNotificacaoDto(
                "aluno@moeda.local",
                "Aluno",
                120.0,
                "professor@moeda.local",
                "Professor",
                80.0,
                10.0,
                "Participacao",
                LocalDateTime.of(2026, 5, 24, 17, 0)
        );
    }

    private static class FakeJavaMailSender implements JavaMailSender {
        private final List<SimpleMailMessage> mensagensEnviadas = new ArrayList<>();

        @Override
        public MimeMessage createMimeMessage() {
            return new MimeMessage((Session) null);
        }

        @Override
        public MimeMessage createMimeMessage(InputStream contentStream) {
            return createMimeMessage();
        }

        @Override
        public void send(MimeMessage mimeMessage) {
        }

        @Override
        public void send(MimeMessage... mimeMessages) {
        }

        @Override
        public void send(MimeMessagePreparator mimeMessagePreparator) {
        }

        @Override
        public void send(MimeMessagePreparator... mimeMessagePreparators) {
        }

        @Override
        public void send(SimpleMailMessage simpleMessage) {
            mensagensEnviadas.add(new SimpleMailMessage(simpleMessage));
        }

        @Override
        public void send(SimpleMailMessage... simpleMessages) {
            for (SimpleMailMessage simpleMessage : simpleMessages) {
                send(simpleMessage);
            }
        }
    }
}
