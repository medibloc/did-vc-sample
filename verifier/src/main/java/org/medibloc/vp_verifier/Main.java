package org.medibloc.vp_verifier;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

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
                String replyQueueName = channel.queueDeclare().getQueue();

                RpcClientParams rpcClientParams = new RpcClientParams();
                rpcClientParams.exchange("");
                rpcClientParams.routingKey("rpc_queue");
                rpcClientParams.channel(channel);
                rpcClientParams.replyTo(replyQueueName);
                rpcClientParams.correlationIdSupplier(new Supplier<String>() {
                    @Override
                    public String get() {
                        return UUID.randomUUID().toString();
                    }
                });

                RpcClient rpcClient = new RpcClient(rpcClientParams);

                String response = rpcClient.stringCall("I'm a client");
                System.out.println("response: " + response);
            } finally {
                channel.close();
            }
        } finally {
            conn.close();
        }
    }
}
