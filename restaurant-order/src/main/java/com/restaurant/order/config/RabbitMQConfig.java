package com.restaurant.order.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.order.exchange:order.exchange}")
    private String orderExchange;

    @Value("${rabbitmq.order.queue.order-created:order.created.queue}")
    private String orderCreatedQueue;

    @Value("${rabbitmq.order.queue.payment-completed:payment.completed.queue}")
    private String paymentCompletedQueue;

    /**
     * 订单Topic交换机
     */
    @Bean
    public TopicExchange orderTopicExchange() {
        return new TopicExchange(orderExchange, true, false);
    }

    /**
     * 订单创建队列
     */
    @Bean
    public Queue orderCreatedQueue() {
        return new Queue(orderCreatedQueue, true);
    }

    /**
     * 支付完成队列
     */
    @Bean
    public Queue paymentCompletedQueue() {
        return new Queue(paymentCompletedQueue, true);
    }

    /**
     * 绑定：order.created -> order.created.queue
     */
    @Bean
    public Binding orderCreatedBinding() {
        return BindingBuilder.bind(orderCreatedQueue())
                .to(orderTopicExchange())
                .with("order.created");
    }

    /**
     * 绑定：payment.completed -> payment.completed.queue
     */
    @Bean
    public Binding paymentCompletedBinding() {
        return BindingBuilder.bind(paymentCompletedQueue())
                .to(orderTopicExchange())
                .with("payment.completed");
    }

    /**
     * 显式配置 RabbitTemplate 使用 JSON 序列化（发送端）
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(new Jackson2JsonMessageConverter(objectMapper));
        return template;
    }

    /**
     * 显式配置 Listener 容器使用 JSON 反序列化（消费端）
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(new Jackson2JsonMessageConverter(objectMapper));
        return factory;
    }
}
