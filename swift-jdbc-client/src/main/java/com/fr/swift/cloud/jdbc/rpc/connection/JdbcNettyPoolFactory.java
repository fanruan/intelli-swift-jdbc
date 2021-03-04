package com.fr.swift.cloud.jdbc.rpc.connection;

import com.fr.swift.cloud.jdbc.rpc.invoke.JdbcNettyHandler;
import com.fr.swift.cloud.log.SwiftLoggers;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;


/**
 * @Author: lucifer
 * @Description:
 * @Date: Created in 2020/12/11
 */
public class JdbcNettyPoolFactory extends BaseKeyedPooledObjectFactory<String, JdbcNettyHandler> {

    private static final int MAX_OBJ_SIZE = Integer.MAX_VALUE;

    public JdbcNettyPoolFactory() {
    }

    @Override
    public JdbcNettyHandler create(String address) throws InterruptedException {
        final JdbcNettyHandler jdbcNettyHandler = new JdbcNettyHandler(address);
        ChannelFuture future = bindBootstrap(jdbcNettyHandler);
        return jdbcNettyHandler;
    }

    @Override
    public PooledObject<JdbcNettyHandler> wrap(JdbcNettyHandler handler) {
        return new DefaultPooledObject<>(handler);
    }

    protected ChannelFuture bindBootstrap(final JdbcNettyHandler handler) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup(1);
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel channel) {
                ChannelPipeline pipeline = channel.pipeline();
                pipeline.addLast(
                        new ObjectDecoder(MAX_OBJ_SIZE, ClassResolvers.cacheDisabled(this
                                .getClass().getClassLoader())));
                pipeline.addLast(new ObjectEncoder());
                pipeline.addLast(handler);
            }
        });
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        handler.setGroup(group);
        return bootstrap.connect(handler.getHost(), handler.getPort()).sync();
    }

    @Override
    public void destroyObject(String key, PooledObject<JdbcNettyHandler> pooledObject) throws Exception {
        super.destroyObject(key, pooledObject);
        pooledObject.getObject().shutdown();
        SwiftLoggers.getLogger().debug("Destroy idle object end! [key:" + key + "]");
    }
}
