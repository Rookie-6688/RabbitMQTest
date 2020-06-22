package com.rabbit.confirm.waitForConfirms;
//发送者确认模式，普通确认，检查单个消息是否确认
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


            channel.queueDeclare("confirmQueue",true,false,false,null);
            channel.exchangeDeclare("confirmExchange","direct",true);
            channel.queueBind("confirmQueue","confirmExchange","confirmRoutingKey");
            String message="confirm普通发送者确认模式的测试消息";

            channel.confirmSelect();                //启动发送确认模式
            channel.basicPublish("confirmExchange","confirmRoutingKey",null,message.getBytes("utf-8"));

            /**
             * 确认批量发送是否成功，没有返回值，如果服务器中一条消息没有发送成功或没有发送成功但是没有正常返回确认都会认为发送失败，抛出异常，随后
             * 进行补发，批量判断抛出异常无法确定是哪一个发送失败，所以需要批量补发，这是非常耗时的。但是总体的检查会比一条一条的检查（也就是waitForConfirms方法检查）快
             * ，但是一条一条的检查发生消息异常时可以快速定位然后进行补发，而批量检查不可以。工作中一般使用批量检查，因为大部分情况下不会发生异常，应该以系统的效率为首位
             */
            channel.waitForConfirmsOrDie();          //返回是否批量发送成功
            System.out.println("消息发送成功");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if(channel!=null){
                channel.close();
            }
            if(connection!=null){
                connection.close();
            }
        }
    }
}

