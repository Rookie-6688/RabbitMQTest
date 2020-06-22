package com.rabbit;

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
        Connection connection=null;
        Channel channel=null;
        try {
            connection = factory.newConnection();
            channel = connection.createChannel();

            channel.queueDeclare("myQueue",true,false,false,null);
            /**
             * 接受消息
             * 参数1、当前消=消费者需要监听的队列名，队列名必须要与发送时的队列名完全一致负责接收不到信息
             * 参数2、信息是否自动确认，true表示自动确认接收然后将消息从队列中移除
             * 参数3、消息接收者的标签用于多个消费者同时监听一个队列时用于确认不同的消费者，通常为空字符串
             * 参数4、信息接收的回调方法，也就是定义具体对消息的处理
             *
             * 注意：使用了basicConsume方法以后，会启动一个线程在持续的监听队列，如果队列中有消息就会自动接受消息
             *      但是如果关闭连接对象和通道对象，那么就接受不到信息数据了。
             */
            channel.basicConsume("myQueue",true,"",new DefaultConsumer(channel){
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    String message=new String(body,"utf-8");
                    System.out.println("接收到消息："+message);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }
}
