package com.rabbit.confirm.manual;

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


            channel.queueDeclare("manualQueue",true,false,false,null);
            channel.exchangeDeclare("manualDirectExchange","direct",true);
            channel.queueBind("manualQueue","manualDirectExchange","manualRoutingKey");
            String message="manual事务的测试消息";
            for(int i=0;i<1000;i++){
                channel.basicPublish("manualDirectExchange","manualRoutingKey",null,message.getBytes("utf-8"));
            }

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
