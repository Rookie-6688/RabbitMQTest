package com.rabbit.transation;

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
            channel.queueDeclare("transactionQueue2", true, false, false, null);    //创建队列
            channel.exchangeDeclare("transactionDirectExchange", "direct", true);                    //创建交换机
            channel.queueBind("transactionQueue2", "transactionDirectExchange", "transactionRoutingKey");     //创建bind连接

            /**
             *
             * 开启事务
             * 当消费者开启事务以后，即使不作为事务的提交，那么依然可以获取队列中的
             * 消息并且将消息从队列中移除
             * 注意：
             *      暂时事务队列接收者没有任何影响
             *
             */
            channel.txSelect();

            channel.basicConsume("transactionQueue2", true, "", new DefaultConsumer(channel) {
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    String message = new String(body, "utf-8");
                    System.out.println("Direct交换机处理的消息：" + message);
                }
            });
            channel.txRollback();             //回滚会导致获取消息失效,并且消息也会从队列中消失，也就是先从队列中取出然后回滚将消息丢掉了
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }


    }
}
