package com.rabbit.transation;

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


            channel.queueDeclare("transactionQueue2",true,false,false,null);
            channel.exchangeDeclare("transactionDirectExchange","direct",true);
            channel.queueBind("transactionQueue2","transactionDirectExchange","transactionRoutingKey");
            String message="direct事务的测试消息";

            channel.txSelect();         //开启事务
            channel.basicPublish("transactionDirectExchange","transactionRoutingKey",null,message.getBytes("utf-8"));

            channel.txCommit();         //提交事务
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }finally {
            if(channel!=null){
                channel.txRollback();       //事务回滚
                channel.close();
            }
            if(connection!=null){
                connection.close();
            }
        }
    }
}

