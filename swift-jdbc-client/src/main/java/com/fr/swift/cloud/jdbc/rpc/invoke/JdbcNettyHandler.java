package com.fr.swift.cloud.jdbc.rpc.invoke;

import com.fr.swift.cloud.basic.Request;
import com.fr.swift.cloud.basic.SwiftRequest;
import com.fr.swift.cloud.basic.SwiftResponse;
import com.fr.swift.cloud.jdbc.JdbcProperty;
import com.fr.swift.cloud.jdbc.exception.Exceptions;
import com.fr.swift.cloud.log.SwiftLoggers;
import com.fr.swift.cloud.util.Strings;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Author: lucifer
 * @Description:
 * @Date: Created in 2020/12/11
 */
public class JdbcNettyHandler extends SimpleChannelInboundHandler<SwiftResponse> {

    private String id = UUID.randomUUID().toString();
    private EventLoopGroup group;
    private Channel channel;
    private Lock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();
    private SwiftResponse response;
    private final static long timeout = JdbcProperty.get().getConnectionTimeout();
    protected String address;
    protected String host;
    protected int port;
    protected SocketAddress remotePeer;


    public JdbcNettyHandler(String address) {
        this.address = address;
        String[] array = Strings.split(address, ":");
        String host = array[0];
        int port = Integer.parseInt(array[1]);
        this.host = host;
        this.port = port;
        this.remotePeer = new InetSocketAddress(host, port);
    }


    public SwiftResponse send(SwiftRequest rpcRequest) throws Exception {
        lock.lock();
        try {
            response = null;
            ChannelFuture channelFuture = channel.writeAndFlush(rpcRequest);
            channelFuture.addListener(new ChannelFutureListener() {
                                          @Override
                                          public void operationComplete(ChannelFuture future) throws Exception {
                                              if (!future.isSuccess()) {
                                                  throw new Exception(future.cause());
                                              }
                                          }
                                      }
            );
            condition.await(timeout, TimeUnit.MILLISECONDS);
            if (null == response) {
                throw Exceptions.timeout(String.format("connection timeout with %s", channelFuture.cause() == null ? Strings.EMPTY : channelFuture.cause().toString()));
            }
            if (response.getException() != null) {
                throw Exceptions.runtime(response.getException().getMessage(), response.getException());
            }
            return response;
        } catch (InterruptedException e) {
            throw Exceptions.timeout("connection timeout");
        } finally {
            lock.unlock();
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, SwiftResponse o) throws Exception {
        lock.lock();
        try {
            this.response = o;
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        this.channel = ctx.channel();
        SwiftLoggers.getLogger().info("Remote address[{}] registered. Channel id is [{}].", ctx.channel().remoteAddress(), ctx.channel().id());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        SwiftLoggers.getLogger().info("Remote address[{}] inactive . Channel id is [{}]", ctx.channel().remoteAddress(), ctx.channel().id());
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        SwiftLoggers.getLogger().error("Remote address[{}] exception . Channel id is [{}]. error [{}]", ctx.channel().remoteAddress(), ctx.channel().id(), cause);
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (!(evt instanceof IdleStateEvent)) {
            super.userEventTriggered(ctx, evt);
        }
        IdleStateEvent e = (IdleStateEvent) evt;
        if (e.state() == IdleState.READER_IDLE) {
            // READ_IDLE_TIME_OUT 没有接收到消息(没有从 Channel 读取到数据), 发送ping
            SwiftRequest swiftRequest = new SwiftRequest();
            swiftRequest.setMethodName(Request.HEART_BEAT);
            ctx.writeAndFlush(swiftRequest);
        } else if (e.state() == IdleState.WRITER_IDLE) {
            // WRITE_IDLE_TIME_OUT 没有写消息, 关闭当前连接
            ctx.close();
        }
    }

    public void setGroup(EventLoopGroup group) {
        this.group = group;
    }

    public void shutdown() {
        group.shutdownGracefully();
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getId() {
        return id;
    }

    public boolean isActive() {
        return channel != null && channel.isActive();
    }
}