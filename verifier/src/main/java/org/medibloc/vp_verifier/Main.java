package org.medibloc.vp_verifier;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;

public class Main {
    public static void main(String[] args) throws IOException, TimeoutException, URISyntaxException, NoSuchAlgorithmException, KeyManagementException, InterruptedException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUri("amqps://b-431011bb-b4af-4910-adee-62d8c7713851.mq.ap-northeast-2.amazonaws.com:5671");
        factory.setUsername("test");
        factory.setPassword("test123");

        Connection conn = factory.newConnection();
        try {
            Channel channel = conn.createChannel();
            try {
                String corrId = UUID.randomUUID().toString();

                String replyQueueName = channel.queueDeclare().getQueue();
                AMQP.BasicProperties props = new AMQP.BasicProperties
                        .Builder()
                        .correlationId(corrId)
                        .replyTo(replyQueueName)
                        .build();

                channel.basicPublish("", "rpc_queue", props, "I'm a client".getBytes(StandardCharsets.UTF_8));

                final BlockingQueue<String> response = new ArrayBlockingQueue<>(1);

                String ctag = channel.basicConsume(replyQueueName, true, ((consumerTag, message) -> {
                    if (message.getProperties().getCorrelationId().equals(corrId)) {
                        response.offer(new String(message.getBody(), "UTF-8"));
                    }
                }), consumerTag -> {
                });

                String resp = response.take();
                channel.basicCancel(ctag);
                System.out.println("response: " + resp);
            } finally {
                channel.close();
            }
        } finally {
            conn.close();
        }
    }
}
