package com.rabbit;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Send {
    public static void main(String[] args) throws IOException, TimeoutException {
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
            connection=factory.newConnection();
            channel = connection.createChannel();
            /**声明一个队列。
             * 参数1：队列名，可以取任意值
             * 参数2：是否为持久化的队列
             * 参数3：是否排外，如果排外说明这个队列只允许一个消费者监听
             * 参数4：是否自动删除,如果是的话当消息队列没有消息也没有消费者连接时，这个队列就会自动删除
             * 参数5：队列的一些属性设置，设置为null就可以了
             * 注意：
             *  1、声明队列时，这个队列名称如果存在则放弃声明，如果队列不存在则会声明一个新的队列
             *  2、队列名可以取值任意，但是一定要与消息接收时完全一致
             *  3、这行代码（说明队列这行）是可有可无的但是一定要在发送消息前确认队列名已经存在在RabbitMQ中，负责就会出现问题
             */
            channel.queueDeclare("myQueue",true,false,false,null);

            String message="我的RabbitMQ的测试消息223";
            /**
             * 发送消息NQ
             * 参数1：交换机名称 ，这里为空表示不使用交换机
             * 参数2：队列名或RoutingKey，当指定了交换机名称以后这个值就是RoutingKey
             * 参数3：消息属性信息，设置为空即可
             * 参数4：具体的消息数据的字节数组
             */
            channel.basicPublish("","myQueue",null,message.getBytes("utf-8"));
            System.out.println("消息发送成功");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }finally {
            if(channel!=null){
                channel.close();
            }
            if(connection!=null){
                connection.close();
            }
        }
    }
}
