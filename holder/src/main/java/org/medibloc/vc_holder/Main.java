package org.medibloc.vc_holder;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

public class Main {
    public static void main(String[] args) throws URISyntaxException, NoSuchAlgorithmException, KeyManagementException, IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUri("amqps://b-431011bb-b4af-4910-adee-62d8c7713851.mq.ap-northeast-2.amazonaws.com:5671");
        factory.setUsername("test");
        factory.setPassword("test123");

        Connection conn = factory.newConnection();
        try {
            Channel channel = conn.createChannel();
            try {
                channel.queueDeclare("rpc_queue", false, false, false, null);
                channel.queuePurge("rpc_queue");
                channel.basicQos(1);

                System.out.println(" [x] Awaiting RPC requests");

                Object monitor = new Object();
                DeliverCallback deliverCallback = ((consumerTag, message) -> {
                    BasicProperties replyProps = new BasicProperties
                            .Builder()
                            .correlationId(message.getProperties().getCorrelationId())
                            .build();

                    try {
                        String request = new String(message.getBody(), "UTF-8");
                        System.out.println("request: " + request);
                    } finally {
                        channel.basicPublish("", message.getProperties().getReplyTo(), replyProps, "I'm a server".getBytes(StandardCharsets.UTF_8));
                        channel.basicAck(message.getEnvelope().getDeliveryTag(), false);
                        // RabbitMq consumer worker thread notifies the RPC server owner thread
                        synchronized (monitor) {
                            monitor.notify();
                        }
                    }
                });

                channel.basicConsume("rpc_queue", false, deliverCallback, (consumerTag -> {}));
                while (true) {
                    synchronized (monitor) {
                        try {
                            monitor.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } finally {
                channel.close();
            }
        } finally {
            conn.close();
        }

    }
}
