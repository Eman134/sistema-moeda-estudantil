package br.pucminas.karv_coins.listener;

import br.pucminas.karv_coins.config.RabbitMQConfig;
import br.pucminas.karv_coins.dto.notification.EmailNotificacaoMessage;
import br.pucminas.karv_coins.dto.notification.TransacaoNotificacaoDto;
import br.pucminas.karv_coins.event.MoedasEnviadasEvent;
import br.pucminas.karv_coins.event.ResgateCriadoEvent;
import br.pucminas.karv_coins.service.EmailNotificationService;
import br.pucminas.karv_coins.service.QrCodeService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class MoedasEmailListener {
    private static final Logger log = LoggerFactory.getLogger(MoedasEmailListener.class);
    private final AmqpTemplate amqpTemplate;
    private final EmailNotificationService emailNotificationService;
    private final QrCodeService qrCodeService;
    private final MessageConverter messageConverter;

    public MoedasEmailListener(
            AmqpTemplate amqpTemplate,
            EmailNotificationService emailNotificationService,
            QrCodeService qrCodeService,
            MessageConverter messageConverter
    ) {
        this.amqpTemplate = amqpTemplate;
        this.emailNotificationService = emailNotificationService;
        this.qrCodeService = qrCodeService;
        this.messageConverter = messageConverter;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoEnviarMoedas(MoedasEnviadasEvent event) {
        if (event.transacoes() == null || event.transacoes().isEmpty()) {
            log.warn("Evento de envio de moedas recebido sem transacoes para notificar.");
            return;
        }
        for (TransacaoNotificacaoDto transacao : event.transacoes()) {
            amqpTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_EMAIL,
                    RabbitMQConfig.ROUTING_KEY_EMAIL,
                    EmailNotificacaoMessage.envioMoedas(transacao)
            );
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoCriarResgate(ResgateCriadoEvent event) {
        if (event.resgate() == null) {
            log.warn("Evento de resgate recebido sem dados para notificar.");
            return;
        }
        amqpTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_EMAIL,
                RabbitMQConfig.ROUTING_KEY_EMAIL,
                EmailNotificacaoMessage.resgateVantagem(event.resgate())
        );
    }

    @RabbitListener(queues = RabbitMQConfig.FILA_EMAIL)
    public void processarNotificacao(Message mensagem) {
        if (mensagem == null) {
            log.warn("Mensagem de notificacao de e-mail recebida sem conteudo.");
            return;
        }
        try {
            Object payload = messageConverter.fromMessage(mensagem);
            processarPayload(payload);
        } catch (Exception e) {
            log.error("Falha ao converter mensagem de e-mail da fila: {}", e.getMessage(), e);
        }
    }

    private void processarPayload(Object payload) {
        if (payload == null) {
            log.warn("Mensagem de notificacao de e-mail convertida sem conteudo.");
            return;
        }
        if (payload instanceof EmailNotificacaoMessage notificacao) {
            processarNotificacaoEmail(notificacao);
            return;
        }
        if (payload instanceof TransacaoNotificacaoDto transacao) {
            emailNotificationService.notificarEnvioMoedas(List.of(transacao));
            return;
        }
        log.warn("Tipo de mensagem de e-mail não suportado: {}", payload.getClass().getName());
    }

    private void processarNotificacaoEmail(EmailNotificacaoMessage notificacao) {
        switch (notificacao.tipo()) {
            case ENVIO_MOEDAS -> emailNotificationService.notificarEnvioMoedas(List.of(notificacao.transacao()));
            case RESGATE_VANTAGEM -> {
                byte[] qrCode = qrCodeService.gerarPng(notificacao.resgate().urlVerificacao());
                emailNotificationService.notificarResgateVantagem(notificacao.resgate(), qrCode);
            }
        }
    }
}
