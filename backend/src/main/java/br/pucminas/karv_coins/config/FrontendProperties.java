package br.pucminas.karv_coins.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.frontend")
public record FrontendProperties(String baseUrl) {
    public String verificationUrl(String codigoCupom) {
        String base = baseUrl == null || baseUrl.isBlank() ? "http://localhost:4200" : baseUrl.strip();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base + "/resgates/verificar/" + codigoCupom;
    }
}
