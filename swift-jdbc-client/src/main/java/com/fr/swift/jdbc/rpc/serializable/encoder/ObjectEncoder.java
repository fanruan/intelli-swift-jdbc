package com.fr.swift.jdbc.rpc.serializable.encoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 * @author yee
 * @date 2018/8/26
 */
public class ObjectEncoder extends AbstractSerializableEncoder {
    @Override
    public byte[] encode(Object object) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = createObjectOutputStream(bos)) {
            oos.writeObject(object);
            return bos.toByteArray();
        }
    }

    protected ObjectOutputStream createObjectOutputStream(OutputStream os) throws IOException {
        return new ObjectOutputStream(os);
    }
}
