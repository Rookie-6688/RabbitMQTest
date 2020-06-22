package com.rabbit.confirm.addConfirmListener;
//发送者确认模式，异步确认，编写异步消息监听确认方法以及没有确认的方法
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmListener;
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


            channel.queueDeclare("confirmListenerQueue",true,false,false,null);
            channel.exchangeDeclare("confirmListenerExchange","direct",true);
            channel.queueBind("confirmListenerQueue","confirmListenerExchange","confirmListenerRoutingKey");
            String message="confirm普通发送者确认模式的测试消息";

            channel.confirmSelect();                //启动发送确认模式

            /**
             *异步消息监听确认，需要在消息发送前启动
             */
            channel.addConfirmListener(new ConfirmListener() {
                //消息确认以后的回调方法
                //参数1：确认消息的编号，从1开始   参数2：当前消息是否同时确认多个
                //参数2为true表示批量确认，也就是同时确认了当前的编号以及当前编号上一个确认编号之间的所有编号的消息，为false表示只确认当前编号的消息
                public void handleAck(long l, boolean b) throws IOException {
                    System.out.println("消息被确认了，消息编号："+l+"，是否确认了多条:"+b);
                }

                //没有确认的回调方法，用于进行消息补发
                //参数1：没有确认消息的编号，从1开始         参数2：当前消息是否同时没有确认多个
                //参数2为true，小于当前编号的所有消息可能都没有发送成功需要进行消息的补发，为false表示当前编号的消息没有发送成功需要进行补发
                public void handleNack(long l, boolean b) throws IOException {
                    System.out.println("消息没有被确认，消息编号："+l+"，是否确认了多条:"+b);
                }
            });
            for(int i=0;i<10000;i++){
                channel.basicPublish("confirmListenerExchange","confirmListenerRoutingKey",null,message.getBytes("utf-8"));
            }
            
            System.out.println("消息发送成功");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }  finally {
            if(channel!=null){
                channel.close();
            }
            if(connection!=null){
                connection.close();
            }
        }
    }
}


