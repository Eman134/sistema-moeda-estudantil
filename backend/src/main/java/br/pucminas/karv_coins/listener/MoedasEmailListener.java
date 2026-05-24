package br.pucminas.karv_coins.listener;

import br.pucminas.karv_coins.event.MoedasEnviadasEvent;
import br.pucminas.karv_coins.service.EmailNotificationService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class MoedasEmailListener {
    private final EmailNotificationService emailNotificationService;

    public MoedasEmailListener(EmailNotificationService emailNotificationService) {
        this.emailNotificationService = emailNotificationService;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoEnviarMoedas(MoedasEnviadasEvent event) {
        emailNotificationService.notificarEnvioMoedas(event.transacoes());
    }
}
