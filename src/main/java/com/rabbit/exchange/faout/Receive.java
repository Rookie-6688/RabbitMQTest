package com.rabbit.exchange.fanout;

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

            /**这里因为是fanout类型，所以不需要指定队列名，又因为是广播的消息传播，所以使用随机的队列名
             *
             * 没有参数的queueDeclare方法会创建一个名字随机的队列
             * 这个队列的数据是非持久化的，排外的（同一时间只允许一个消费者监听当前队列），自动删除的（当没有任何消费者监听时这个队列会自动删除）
             *
             *getQueue()：获取这个随机的队列名
             *
             * 这里的队列名可以指定为特定的值，也就和direct的队列一样来指定，但是这样就需要将send类里面的队列和绑定指定队列名，总之和direct差不多，
             * 这样做也可以使得在发送消息后再打开receive接收不会造成消息的丢失（和direct效果一样），但是这样就达不到广播的效果了，其他的receive都需要指定为相同的队列，也就失去了fanout的意义。
             */

            String queueName = channel.queueDeclare().getQueue();//创建队列
            channel.exchangeDeclare("fanoutExchange", "fanout", true);                    //创建交换机
            //监听某个队列并获取队列中的数据，由于是fanout类型的交换器所以不需要使用RoutingKey进行绑定
            channel.queueBind(queueName, "fanoutExchange", "");     //创建bind连接

            /**
             * 监听某个队列并获取队列中的数据
             * 注意：
             *      当前被讲定的队列必须已经存在并正确绑定到了某个交换机中
             */
            channel.basicConsume(queueName, true, "", new DefaultConsumer(channel) {
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    String message = new String(body, "utf-8");
                    System.out.println("fanout交换机1处理的消息：" + message);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }


    }
}