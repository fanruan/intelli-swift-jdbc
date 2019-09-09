package com.fr.swift.jdbc.rpc.connection;

import com.fr.swift.jdbc.rpc.JdbcExecutor;
import com.fr.swift.jdbc.rpc.invoke.BaseConnector;
import com.fr.swift.log.SwiftLoggers;
import com.fr.swift.util.Assert;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandler;
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

/**
 * @author yee
 * @version 1.1
 * Created by yee on 2019-08-30
 */
public class JdbcNettyConnector extends BaseConnector {
    private EventLoopGroup group;
    private static final int MAX_OBJ_SIZE = 1000000000;

    public JdbcNettyConnector(String address) {
        super(address);
    }

    public JdbcNettyConnector(String host, int port) {
        super(host, port);
    }

    @Override
    public void notifySend() {
        rpcExecutors.get(0).send(getRequest());
    }

    @Override
    public void start() {
        group = new NioEventLoopGroup(1);
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
                final JdbcExecutor jdbcExecutor = rpcExecutors.get(0);
                Assert.isTrue(jdbcExecutor instanceof ChannelHandler);

                pipeline.addLast((ChannelHandler) jdbcExecutor);
            }
        });
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        try {
            bootstrap.connect(host, port).sync();
        } catch (InterruptedException e) {
            SwiftLoggers.getLogger().error(e);
        }
    }

    @Override
    public void stop() {
        if (!group.isShutdown() && !group.isShuttingDown()) {
            group.shutdownGracefully();
        }

    }
}
