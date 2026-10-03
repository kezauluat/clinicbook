package rw.auca.clinicbook.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * clinic.events (topic) --appointment.*--> notifications.email
 *                       --appointment.*--> notifications.sms
 * Failed messages (after retries) --> clinic.dlx --> notifications.dlq
 */
@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "clinic.events";
    public static final String EMAIL_QUEUE = "notifications.email";
    public static final String SMS_QUEUE = "notifications.sms";
    public static final String DLX = "clinic.dlx";
    public static final String DLQ = "notifications.dlq";

    @Bean
    public TopicExchange clinicExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX, true, false);
    }

    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable(EMAIL_QUEUE).deadLetterExchange(DLX).deadLetterRoutingKey("dead").build();
    }

    @Bean
    public Queue smsQueue() {
        return QueueBuilder.durable(SMS_QUEUE).deadLetterExchange(DLX).deadLetterRoutingKey("dead").build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DLQ).build();
    }

    @Bean
    public Binding emailBinding() {
        return BindingBuilder.bind(emailQueue()).to(clinicExchange()).with("appointment.*");
    }

    @Bean
    public Binding smsBinding() {
        return BindingBuilder.bind(smsQueue()).to(clinicExchange()).with("appointment.*");
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange()).with("dead");
    }

    @Bean
    public MessageConverter messageConverter() {
        SimpleMessageConverter converter = new SimpleMessageConverter();
        converter.setAllowedListPatterns(List.of("rw.auca.clinicbook.*", "java.time.*", "java.lang.*", "java.util.*"));
        return converter;
    }
}
