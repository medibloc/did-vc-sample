package org.medibloc.vc_holder;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.StringRpcServer;

import java.io.IOException;
import java.net.URISyntaxException;
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

                StringRpcServer rpcServer = new StringRpcServer(channel, "rpc_queue") {
                    public String handleStringCall(String request) {
                        System.out.println("request: " + request);
                        return "I'm a server";
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
