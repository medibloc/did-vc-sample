package org.medibloc.vc_holder;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.StringRpcServer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
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
                String queueName = UUID.randomUUID().toString();
                System.out.println("queueName: " + queueName);

                channel.queueDeclare(queueName, false, false, false, null);
//                channel.queuePurge(queueName);
//                channel.basicQos(1);

                StringRpcServer rpcServer = new StringRpcServer(channel, queueName) {
                    public String handleStringCall(String request) {
                        try {
                            System.out.println("request: " + request);
                            return "I'm a server";
                        } finally {
                            this.terminateMainloop();
                        }
                    }
                };

                rpcServer.mainloop();
            } finally {
                channel.close();
            }
        } finally {
            conn.close();
        }

    }
}
