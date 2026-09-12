package com.drey.eventsSports.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "spring.rabbitmq.enabled", havingValue = "true", matchIfMissing = true)
public class RabbitMQConfig {

    public static final String EXCHANGE_MESSAGING = "messaging.direct.exchange";
    
    // Queues
    public static final String QUEUE_EMAIL = "messaging.email.queue";
    public static final String QUEUE_SMS = "messaging.sms.queue";
    public static final String QUEUE_WHATSAPP = "messaging.whatsapp.queue";
    public static final String QUEUE_PUSH = "messaging.push.queue";
    
    // Routing Keys
    public static final String ROUTING_EMAIL = "messaging.email.routing";
    public static final String ROUTING_SMS = "messaging.sms.routing";
    public static final String ROUTING_WHATSAPP = "messaging.whatsapp.routing";
    public static final String ROUTING_PUSH = "messaging.push.routing";

    @Bean
    public DirectExchange messagingExchange() {
        return new DirectExchange(EXCHANGE_MESSAGING);
    }

    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable(QUEUE_EMAIL).build();
    }

    @Bean
    public Queue smsQueue() {
        return QueueBuilder.durable(QUEUE_SMS).build();
    }

    @Bean
    public Queue whatsappQueue() {
        return QueueBuilder.durable(QUEUE_WHATSAPP).build();
    }

    @Bean
    public Queue pushQueue() {
        return QueueBuilder.durable(QUEUE_PUSH).build();
    }

    @Bean
    public Binding emailBinding(Queue emailQueue, DirectExchange messagingExchange) {
        return BindingBuilder.bind(emailQueue).to(messagingExchange).with(ROUTING_EMAIL);
    }

    @Bean
    public Binding smsBinding(Queue smsQueue, DirectExchange messagingExchange) {
        return BindingBuilder.bind(smsQueue).to(messagingExchange).with(ROUTING_SMS);
    }

    @Bean
    public Binding whatsappBinding(Queue whatsappQueue, DirectExchange messagingExchange) {
        return BindingBuilder.bind(whatsappQueue).to(messagingExchange).with(ROUTING_WHATSAPP);
    }

    @Bean
    public Binding pushBinding(Queue pushQueue, DirectExchange messagingExchange) {
        return BindingBuilder.bind(pushQueue).to(messagingExchange).with(ROUTING_PUSH);
    }
}
