package com.rabbit.confirm.manual;
//消费者确认模式，手动确认，手动确认消息从而移除队列
//防重复处理
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
            channel.queueDeclare("manualQueue", true, false, false, null);    //创建队列
            channel.exchangeDeclare("manualDirectExchange", "direct", true);                    //创建交换机
            channel.queueBind("manualQueue", "manualDirectExchange", "manualRoutingKey");     //创建bind连接

            channel.txSelect();

            /**
             *
             * 第二个参数表示是否自动消息确认接收，true确认之后消息就会从队列中移除，
             * 但是在确认时发生异常消息也会移除，并且也因为异常而没有被消费者完全接收，导致消息丢失
             * false表示不会自动确认消息，所以不会自动丢失消息，没有发生异常的话会读取消息但是不会确认消息，也就是队列中的消息不会因为被读取而减少
             */
            channel.basicConsume("manualQueue", false, "", new DefaultConsumer(channel) {
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
//                    int a=1/0;

                    //获取当前消息是否被接收过一次，如果返回值为false，表示消息之前没有被接受过，如果为true表示这个消息已经被接收过，可能也处理完成，所以我们需要进行消息的防重复处理
                    boolean b = envelope.isRedeliver();
                    System.out.println(b);

                    if(!b){
                        //没有被处理过，执行处理代码
                        String message = new String(body, "utf-8");
                        System.out.println("manual交换机处理的消息：" + message);

                        //获取消息编号
                        long deliveryTag = envelope.getDeliveryTag();
                        //获取当前内部类中的通道
                        Channel channel1 = this.getChannel();
                        //手动确认消息，确认后就会将该消息从队列中移除，参数1：消息的序号  参数2：是否确认多个，true表示确认小于等于当前编号的所有消息，false表示值确认当前编号的消息
                        channel1.basicAck(deliveryTag,true);

                        //如果添加了事务，且为手动确认消息模式，那么必须在每次确认消息时提交事务，否则消息无法被确认，越就是无法从队列中被移除（一般情况下不管是否提交都会被确认移除）
                        channel1.txCommit();
                    }else{
                        //进行防重复处理
                        //例如查询数据库中是否已经添加了记录或已经修改了记录
                        //如果经过判断这条消息没有处理则需要重新处理，然后确认；如果处理过则直接确认消息即可。
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }


    }
}
