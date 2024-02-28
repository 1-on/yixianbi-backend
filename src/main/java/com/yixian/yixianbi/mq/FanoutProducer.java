package com.yixian.yixianbi.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class FanoutProducer {

    private static final String EXCHANGE_NAME = "fanout-exchange";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            // 创建交换机
            channel.exchangeDeclare(EXCHANGE_NAME, "fanout");

            // 创建一个输入扫描器，用于读取控制台输入
            Scanner scanner = new Scanner(System.in);
            // 使用循环，每当用户在控制台输入一行文本，就将其作为消息发送
            while (scanner.hasNext()) {
                // 读取用户在控制台输入的下一行文本
                String message = scanner.nextLine();
                // 发布消息到队列，设置消息持久化
                channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes(StandardCharsets.UTF_8));
                System.out.println(" [x] Sent '" + message + "'");
            }


        }
    }
}