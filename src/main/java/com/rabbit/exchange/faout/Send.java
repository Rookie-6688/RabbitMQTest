package com.rabbit.exchange.faout;

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

            //这里因为是faout，所以不建议声明队列和绑定，但需要声明交换机
//            channel.queueDeclare("myDirectQueue",true,false,false,null);
            channel.exchangeDeclare("fanoutExchange","fanout",true);
//            channel.queueBind("myDirectQueue","directExchange","directRoutingKey");


            String message="direct的测试消息2";
            /**
             * 发送消息到指定的消息队列
             * 参数1：交换机的名称
             * 参数2：消息的RoutingKey 如果这个消息的RoutingKey和某个队列与交换机绑定的RoutingKey一致，那么
             * 这个消息就会发送到指定的队列中。
             * 参数3：消息属性信息，设置为空即可
             * 参数4：具体的消息数据的字节数组
             * 注意：
             *      1、发送消息时必须确保交换机已经创建并且确保已经正确的绑定到了某个队列
             */
            channel.basicPublish("fanoutExchange","",null,message.getBytes("utf-8"));
            System.out.println("发送成功");
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
