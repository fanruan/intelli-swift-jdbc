package com.fr.swift.jdbc.server;

import com.fr.swift.SwiftContext;
import com.fr.swift.annotation.SwiftApi;
import com.fr.swift.jdbc.server.handler.JdbcServerHandler;
import com.fr.swift.log.SwiftLoggers;
import com.fr.swift.property.SwiftProperty;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yee
 * @version 1.1
 * Created by yee on 2019-09-03
 */
public class JdbcServer {

    private SwiftProperty swiftProperty;
    private String host;
    private int port;
    private Map<String, Object> apiServices;


    public JdbcServer() {
        swiftProperty = SwiftProperty.getProperty();
        this.host = swiftProperty.getJdbcHost();
        this.port = swiftProperty.getJdbcPort();
        initApiServices();
    }

    private void initApiServices() {
        apiServices = new HashMap<>();
        final Map<String, Object> beansByAnnotations = SwiftContext.get().getBeansByAnnotations(SwiftApi.class);
        for (Map.Entry<String, Object> entry : beansByAnnotations.entrySet()) {
            final Object service = entry.getValue();
            final SwiftApi swiftApi = service.getClass().getAnnotation(SwiftApi.class);
            if (swiftApi.enable() && !Void.class.equals(swiftApi.service())) {
                apiServices.put(swiftApi.service().getName(), service);
            }
        }
    }

    public void start() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup);
            bootstrap.channel(NioServerSocketChannel.class);
            bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel channel) {
                    ChannelPipeline pipeline = channel.pipeline();
                    pipeline.addLast(
                            new ObjectDecoder(swiftProperty.getRpcMaxObjectSize(), ClassResolvers
                                    .weakCachingConcurrentResolver(this.getClass()
                                            .getClassLoader())));
                    pipeline.addLast(new ObjectEncoder());
                    pipeline.addLast(new JdbcServerHandler(apiServices)); // 处理 RPC 请求
                }
            });
            bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
            bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture future = bootstrap.bind(host, port).sync();
            SwiftLoggers.getLogger().info("JDBC Server started on ip:" + host + ", port :" + port);
            future.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
