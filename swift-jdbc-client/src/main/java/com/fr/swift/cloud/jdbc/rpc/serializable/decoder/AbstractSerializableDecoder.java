package com.fr.swift.cloud.jdbc.rpc.serializable.decoder;

import java.nio.ByteBuffer;

/**
 * @author yee
 * @date 2018/8/26
 */
public abstract class AbstractSerializableDecoder implements SerializableDecoder {

    @Override
    public Object decode(ByteBuffer buf) throws Exception {
        return decode(buf.array());
    }

}
