package com.rabbit.exchange.direct;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Receive {

    public static void main(String[] args) {
        //创建连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        /**
         * 配置RabbitMQ的连接相关信息
         */
        factory.setHost("192.168.1.107");
        factory.setPort(5672);
        factory.setUsername("guest");
        factory.setPassword("guest");
        Connection connection = null;
        Channel channel = null;
        try {
            connection = factory.newConnection();
            channel = connection.createChannel();

            //这三步可以省略，但是前提是都已经存在
            channel.queueDeclare("myDirectQueue", true, false, false, null);    //创建队列
            channel.exchangeDeclare("directExchange", "direct", true);                    //创建交换机
            channel.queueBind("myDirectQueue", "directExchange", "directRoutingKey");     //创建bind连接

            /**
             * 监听某个队列并获取队列中的数据
             * 注意：
             *      当前被讲定的队列必须已经存在并正确绑定到了某个交换机中
             */
            channel.basicConsume("myDirectQueue", true, "", new DefaultConsumer(channel) {
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    String message = new String(body, "utf-8");
                    System.out.println("Direct交换机处理的消息：" + message);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }


    }
}
