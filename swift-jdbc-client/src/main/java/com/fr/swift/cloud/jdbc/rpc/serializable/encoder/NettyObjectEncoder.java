package com.fr.swift.cloud.jdbc.rpc.serializable.encoder;


import com.fr.swift.cloud.jdbc.rpc.serializable.stream.CompactObjectOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 * @author yee
 * @date 2018/8/26
 */
public class NettyObjectEncoder extends ObjectEncoder {
    private static final byte[] LENGTH_PLACEHOLDER = new byte[4];

    @Override
    public byte[] encode(Object object) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(LENGTH_PLACEHOLDER);
        try (ObjectOutputStream oos = createObjectOutputStream(bos)) {
            oos.writeObject(object);
            byte[] data = bos.toByteArray();
            int length = data.length - LENGTH_PLACEHOLDER.length;
            data[0] = (byte) (length >>> 24);
            data[1] = (byte) (length >>> 16);
            data[2] = (byte) (length >>> 8);
            data[3] = (byte) length;
            return data;
        }
    }

    @Override
    protected ObjectOutputStream createObjectOutputStream(OutputStream os) throws IOException {
        return new CompactObjectOutputStream(os);
    }
}
