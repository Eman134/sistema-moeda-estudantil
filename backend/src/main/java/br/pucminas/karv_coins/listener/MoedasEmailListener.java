package br.pucminas.karv_coins.listener;

import br.pucminas.karv_coins.config.RabbitMQConfig;
import br.pucminas.karv_coins.dto.notification.TransacaoNotificacaoDto;
import br.pucminas.karv_coins.event.MoedasEnviadasEvent;
import br.pucminas.karv_coins.service.EmailNotificationService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class MoedasEmailListener {
    private static final Logger log = LoggerFactory.getLogger(MoedasEmailListener.class);
    private final AmqpTemplate amqpTemplate;
    private final EmailNotificationService emailNotificationService;

    public MoedasEmailListener(AmqpTemplate amqpTemplate, EmailNotificationService emailNotificationService) {
        this.amqpTemplate = amqpTemplate;
        this.emailNotificationService = emailNotificationService;
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
                    RabbitMQConfig.EXCHANGE_NOTIFICACAO,
                    RabbitMQConfig.ROUTING_KEY,
                    transacao
            );
        }
    }

    @RabbitListener(queues = RabbitMQConfig.FILA_NOTIFICACAO)
    public void processarNotificacao(TransacaoNotificacaoDto transacao) {
        if (transacao == null) {
            log.warn("Mensagem de notificacao de moedas recebida sem conteudo.");
            return;
        }
        emailNotificationService.notificarEnvioMoedas(List.of(transacao));
    }
}
