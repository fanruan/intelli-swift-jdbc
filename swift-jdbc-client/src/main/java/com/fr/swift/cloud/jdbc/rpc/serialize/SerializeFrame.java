package com.fr.swift.cloud.jdbc.rpc.serialize;

import com.fr.swift.cloud.jdbc.rpc.handler.JdkHandler;
import com.fr.swift.cloud.jdbc.rpc.handler.KryoHandler;
import com.fr.swift.cloud.rpc.compress.CompressMode;
import com.fr.swift.cloud.rpc.handler.RpcMessageHandler;
import com.fr.swift.cloud.rpc.serialize.SerializeProtocol;
import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;
import io.netty.channel.ChannelPipeline;


/**
 * @author Heng.J
 * @date 2021/7/30
 * @description
 * @since swift-1.2.0
 */
public class SerializeFrame {

    private static final ClassToInstanceMap<RpcMessageHandler> HANDLER = MutableClassToInstanceMap.create();

    static {
        HANDLER.putInstance(JdkHandler.class, new JdkHandler());
        HANDLER.putInstance(KryoHandler.class, new KryoHandler());
    }

    public static void select(SerializeProtocol protocol, CompressMode compressMode, ChannelPipeline pipeline) {
        switch (protocol) {
            //others :
            case KRYO_SERIALIZE: {
                HANDLER.getInstance(KryoHandler.class).handle(compressMode, pipeline);
                break;
            }
            case JDK_SERIALIZE:
            default: {
                HANDLER.getInstance(JdkHandler.class).handle(compressMode, pipeline);
                break;
            }
        }
    }
}

