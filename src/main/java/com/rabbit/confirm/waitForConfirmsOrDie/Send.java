package com.rabbit.confirm.waitForConfirmsOrDie;
//发送者确认模式，批量确认，批量检查消息是否确认
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


            channel.queueDeclare("confirmOrDieQueue",true,false,false,null);
            channel.exchangeDeclare("confirmOrDieExchange","direct",true);
            channel.queueBind("confirmOrDieQueue","confirmOrDieExchange","confirmOrDieRoutingKey");
            String message="confirm普通发送者确认模式的测试消息";

            channel.confirmSelect();                //启动发送确认模式
            channel.basicPublish("confirmOrDieExchange","confirmOrDieRoutingKey",null,message.getBytes("utf-8"));

            //通过这个方法可以判断消息是否发送成功，成功的话返回true，没有成功返回false，但是返回false并不是就一定是发送失败
            //可以为这个方法指定一个毫秒用于确认我们需要等待服务确认的等待时间
            //如果超过了等待时间就会抛出异常，同样抛出异常也并不是一定是发送失败。这时我们可以补发消息，具体实现是可以递归补发或者将消息缓存到redis利用定时任务补发
            boolean b = channel.waitForConfirms();          //返回是否发送成功
            System.out.println("消息发送："+b);
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

