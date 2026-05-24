package br.pucminas.karv_coins.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.mail")
public record MailProperties(
        boolean enabled,
        boolean validateOnStartup,
        String from,
        String appName
) {
}
