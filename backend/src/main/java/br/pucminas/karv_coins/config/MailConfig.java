package br.pucminas.karv_coins.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
@EnableConfigurationProperties({MailProperties.class, FrontendProperties.class})
public class MailConfig {
    private static final Logger log = LoggerFactory.getLogger(MailConfig.class);

    @Bean
    ApplicationRunner mailConfigurationValidator(
            MailProperties appMailProperties,
            JavaMailSender mailSender
    ) {
        return args -> {
            if (!appMailProperties.enabled()) {
                log.info("Envio de e-mail desabilitado (app.mail.enabled=false).");
                return;
            }

            if (!hasText(appMailProperties.from())) {
                log.error("E-mail habilitado, mas app.mail.from nao foi configurado. Os envios serao cancelados.");
                return;
            }

            if (!(mailSender instanceof JavaMailSenderImpl javaMailSender)) {
                log.warn("JavaMailSender nao e compativel com validacao SMTP. O envio sera tentado em tempo de execucao.");
                return;
            }

            if (!hasText(javaMailSender.getHost())
                    || !hasText(javaMailSender.getUsername())
                    || !hasText(javaMailSender.getPassword())) {
                log.error(
                        "E-mail habilitado, mas spring.mail.host, spring.mail.username ou spring.mail.password nao foi configurado."
                );
                return;
            }

            if (!appMailProperties.validateOnStartup()) {
                log.info("Validacao SMTP no startup desabilitada (app.mail.validate-on-startup=false).");
                return;
            }

            try {
                javaMailSender.testConnection();
                log.info("Conexao SMTP validada com sucesso em {}:{}.", javaMailSender.getHost(), javaMailSender.getPort());
            } catch (MailException ex) {
                log.error(
                        "Falha ao validar SMTP em {}:{}. Verifique usuario/senha SMTP. A aplicacao continuara iniciando.",
                        javaMailSender.getHost(),
                        javaMailSender.getPort(),
                        ex
                );
            }
        };
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
