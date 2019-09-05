package com.fr.swift.jdbc.rpc.connection;

import com.fr.swift.jdbc.rpc.JdbcExecutor;
import com.fr.swift.jdbc.rpc.JdbcSelector;
import com.fr.swift.jdbc.rpc.invoke.BaseConnector;
import com.fr.swift.jdbc.rpc.selector.RpcNioSelector;
import com.fr.swift.jdbc.rpc.serializable.decoder.NioForNettyServerDecoder;
import com.fr.swift.jdbc.rpc.serializable.decoder.SerializableDecoder;
import com.fr.swift.jdbc.rpc.serializable.encoder.NioForNettyServerEncoder;
import com.fr.swift.jdbc.rpc.serializable.encoder.SerializableEncoder;
import com.fr.swift.jdbc.rpc.util.SocketChannelUtils;
import com.fr.swift.log.SwiftLoggers;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

/**
 * @author yee
 * @date 2018/8/26
 */
public class RpcNioConnector extends BaseConnector {
    private SocketChannel channel;
    private JdbcSelector selector;


    public RpcNioConnector(String address, SerializableEncoder encoder, SerializableDecoder decoder) {
        super(address);
        this.selector = new RpcNioSelector(encoder, decoder);
    }

    public RpcNioConnector(String address) {
        this(address, new NioForNettyServerEncoder(), new NioForNettyServerDecoder());
    }

    public RpcNioConnector(String host, int port) {
        this(host, port, new NioForNettyServerEncoder(), new NioForNettyServerDecoder());
    }

    public RpcNioConnector(String host, int port, SerializableEncoder encoder, SerializableDecoder decoder) {
        super(host, port);
        this.selector = new RpcNioSelector(encoder, decoder);
    }

    public RpcNioConnector(SocketChannel channel, JdbcSelector selector) {
        this.channel = channel;
        this.selector = selector;
    }

    public SocketChannel getChannel() {
        return channel;
    }

    @Override
    public void notifySend() {
        selector.notifySend();
    }

    @Override
    public void start() {
        if (channel == null) {
            try {
                channel = SocketChannelUtils.wrapSocketOptions(SocketChannel.open(), Integer.MAX_VALUE);
                channel.configureBlocking(false);
                channel.connect(new InetSocketAddress(host, port));
                while (!channel.finishConnect()) {
                }
                selector.start();
                selector.register(this);
            } catch (IOException e) {
                SwiftLoggers.getLogger().error(e);
            }
        }
    }

    @Override
    public void stop() {
        if (channel.isConnected()) {
            try {
                channel.close();
                selector.stop();
                while (channel.isConnected()) {
                    // 等待channel关闭
                }
            } catch (IOException e) {
            }
            for (JdbcExecutor rpcExecutor : rpcExecutors) {
                rpcExecutor.stop();
            }
        }
    }
}
