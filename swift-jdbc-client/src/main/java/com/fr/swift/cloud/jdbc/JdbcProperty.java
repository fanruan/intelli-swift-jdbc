package com.fr.swift.cloud.jdbc;

import com.fr.swift.cloud.config.ConfigInputUtil;
import com.fr.swift.cloud.jdbc.rpc.serializable.clazz.CachingClassResolver;
import com.fr.swift.cloud.jdbc.rpc.serializable.clazz.ClassLoaderClassResolver;
import com.fr.swift.cloud.jdbc.rpc.serializable.clazz.ClassResolver;
import com.fr.swift.cloud.rpc.compress.CompressMode;
import com.fr.swift.cloud.rpc.serialize.SerializeProtocol;
import com.fr.swift.cloud.util.Strings;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yee
 * @date 2018/11/16
 */
public final class JdbcProperty {

    private static final JdbcProperty PROPERTY = new JdbcProperty();

    public JdbcProperty() {
        initProperties();
    }

    private String connectionSchema;
    private int majorVersion;
    private int minorVersion;
    private long connectionTimeout;
    private int statementMaxIdle;
    private boolean compliant;
    private ClassResolver resolver;
    private int connectionPoolSize;
    private long readIdleTimeout;
    private long writeIdleTimeout;
    private int rpcMaxObjectSize;
    private SerializeProtocol serializeProtocol;
    private CompressMode compressMode;

    public static JdbcProperty get() {
        return PROPERTY;
    }

    private void initProperties() {
        Properties properties = new Properties();
        ClassResolver resolver = new ClassLoaderClassResolver(Thread.currentThread().getContextClassLoader());
        this.resolver = new CachingClassResolver(resolver, new ConcurrentHashMap<>());
        try (InputStream inputStream = ConfigInputUtil.getConfigInputStream("swift-cloud-jdbc.properties")) {
            properties.load(inputStream);
            connectionSchema = properties.getProperty("connection.schema", "jdbc:swift");
            majorVersion = Integer.parseInt(properties.getProperty("driver.version.major", "10"));
            minorVersion = Integer.parseInt(properties.getProperty("driver.version.minor", "1"));
            connectionTimeout = Long.parseLong(properties.getProperty("connection.timeout", "30000"));
            statementMaxIdle = Integer.parseInt(properties.getProperty("statement.maxIdle", "200"));
            compliant = Boolean.parseBoolean(properties.getProperty("driver.compliant", "false"));
            connectionPoolSize = Integer.parseInt(properties.getProperty("connection.pool.size", "64"));
            readIdleTimeout = Long.parseLong(properties.getProperty("read.idle.timeout", "1800000"));
            writeIdleTimeout = Long.parseLong(properties.getProperty("write.idle.timeout", "1800000"));
            rpcMaxObjectSize = Integer.parseInt((String) properties.getOrDefault("rpcMaxObjectSize", "2147483647"));
            serializeProtocol = SerializeProtocol.getEnum((String) properties.getOrDefault("serialize.protocol", "jdk"));
            compressMode = CompressMode.getEnum((String) properties.getOrDefault("compress.algorithm", Strings.EMPTY));
        } catch (IOException ignore) {
            connectionSchema = "jdbc:swift";
            majorVersion = 10;
            minorVersion = 1;
            connectionTimeout = 30000;
            statementMaxIdle = 200;
            compliant = false;
            connectionPoolSize = 64;
            readIdleTimeout = 1800000;
            writeIdleTimeout = 1800000;
            serializeProtocol = SerializeProtocol.getEnum("jdk");
            compressMode = CompressMode.getEnum(Strings.EMPTY);
        }
    }

    public String getConnectionSchema() {
        return connectionSchema;
    }

    public int getMajorVersion() {
        return majorVersion;
    }

    public int getMinorVersion() {
        return minorVersion;
    }

    public boolean isCompliant() {
        return compliant;
    }

    public Long getConnectionTimeout() {
        return connectionTimeout;
    }

    public Integer getStatementMaxIdle() {
        return statementMaxIdle;
    }


    public ClassResolver getClassResolver() {
        return resolver;
    }

    public int getConnectionPoolSize() {
        return connectionPoolSize;
    }

    public long getReadIdleTimeout() {
        return readIdleTimeout;
    }

    public int getRpcMaxObjectSize() {
        return rpcMaxObjectSize;
    }

    public long getWriteIdleTimeout() {
        return writeIdleTimeout;
    }

    public SerializeProtocol getSerializeProtocol() {
        return serializeProtocol;
    }

    public CompressMode getCompressMode() {
        return compressMode;
    }
}
