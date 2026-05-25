package br.pucminas.karv_coins.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitMQConfig {
    public static final String FILA_NOTIFICACAO = "notificacao-moedas";
    public static final String EXCHANGE_NOTIFICACAO = "notificacao-exchange";
    public static final String ROUTING_KEY = "notificacao-moedas-key";

    @Bean
    public Queue filaNotificacao() {
        return new Queue(FILA_NOTIFICACAO, true);
    }

    @Bean
    public DirectExchange exchangeNotificacao() {
        return new DirectExchange(EXCHANGE_NOTIFICACAO);
    }

    @Bean
    public Binding binding(Queue filaNotificacao, DirectExchange exchangeNotificacao) {
        return BindingBuilder
                .bind(filaNotificacao)
                .to(exchangeNotificacao)
                .with(ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        SimpleMessageConverter converter = new SimpleMessageConverter();
        converter.addAllowedListPatterns(
                "br.pucminas.karv_coins.*",
                "java.time.*",
                "java.util.*",
                "java.lang.*",
                "java.math.*"
        );
        return converter;
    }

    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }

    @Bean
    public AmqpAdmin amqpAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public RabbitListenerContainerFactory<?> rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter jsonMessageConverter
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter);
        return factory;
    }
}
