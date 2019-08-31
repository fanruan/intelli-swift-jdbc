package com.fr.swift.jdbc.rpc.invoke;

import com.fr.swift.basic.SwiftRequest;
import com.fr.swift.basic.SwiftResponse;
import com.fr.swift.jdbc.exception.Exceptions;
import com.fr.swift.jdbc.rpc.JdbcExecutor;
import com.fr.swift.jdbc.rpc.connection.JdbcNettyConnector;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author yee
 * @version 1.1
 * Created by yee on 2019-08-30
 */
@ChannelHandler.Sharable
public class JdbcNettyExecutor extends SimpleChannelInboundHandler<SwiftResponse> implements JdbcExecutor {
    private JdbcNettyConnector connector;
    private Channel channel;
    private Lock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();
    private SwiftResponse response;
    private long timeout;

    public JdbcNettyExecutor(JdbcNettyConnector connector, long timeout) {
        this.connector = connector;
        this.timeout = timeout;
    }

    @Override
    public void onRpcResponse(SwiftResponse rpcResponse) {

    }

    @Override
    public SwiftResponse send(SwiftRequest rpcRequest) {
        lock.lock();
        try {
            channel.writeAndFlush(rpcRequest);
            condition.await(timeout, TimeUnit.MILLISECONDS);
            if (null == response) {
                throw Exceptions.timeout("connection timeout");
            }
            return response;
        } catch (InterruptedException e) {
            throw Exceptions.timeout("connection timeout");
        } finally {
            lock.unlock();
        }

    }

    @Override
    public void start() {
        this.connector.registerExecutor(this);
        this.connector.start();
    }

    @Override
    public void stop() {
        connector.stop();
    }

    @Override
    public void handlerException(Exception e) {
        connector.handlerException(e);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, SwiftResponse o) throws Exception {
        this.response = o;
        lock.lock();
        try {
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        this.channel = ctx.channel();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
    }
}
