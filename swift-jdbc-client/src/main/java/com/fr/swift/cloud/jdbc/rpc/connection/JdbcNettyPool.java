package com.fr.swift.cloud.jdbc.rpc.connection;

import com.fr.swift.cloud.jdbc.JdbcProperty;
import com.fr.swift.cloud.jdbc.rpc.invoke.JdbcNettyHandler;
import com.fr.swift.cloud.log.SwiftLoggers;
import org.apache.commons.pool2.KeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;

import java.util.concurrent.TimeUnit;

/**
 * @Author: lucifer
 * @Description:
 * @Date: Created in 2020/12/11
 */
public class JdbcNettyPool implements KeyedObjectPool<String, JdbcNettyHandler> {

    protected GenericKeyedObjectPool keyedObjectPool;

    private static final long IDLE_OBJ_EXPIRE_TIME = TimeUnit.HOURS.toMillis(1);

    private final static JdbcNettyPool INSTANCE = new JdbcNettyPool();

    public static JdbcNettyPool getInstance() {
        return INSTANCE;
    }

    private JdbcNettyPool() {
        JdbcNettyPoolFactory poolFactory = new JdbcNettyPoolFactory();
        GenericKeyedObjectPoolConfig config = new GenericKeyedObjectPoolConfig();
        config.setTimeBetweenEvictionRunsMillis(IDLE_OBJ_EXPIRE_TIME);
        config.setMinEvictableIdleTimeMillis(IDLE_OBJ_EXPIRE_TIME);
        config.setMaxTotalPerKey(JdbcProperty.get().getConnectionPoolSize());
        keyedObjectPool = new GenericKeyedObjectPool(poolFactory, config);
    }

    @Override
    public JdbcNettyHandler borrowObject(String key) throws Exception {
        SwiftLoggers.getLogger().info("borrow jdbc netty thread[{}]", key);
        JdbcNettyHandler handler = (JdbcNettyHandler) keyedObjectPool.borrowObject(key);
        SwiftLoggers.getLogger().info("current thread pool size: max[{}],active[{}],idle[{}],handler[{}]"
                , keyedObjectPool.getMaxTotalPerKey(), keyedObjectPool.getNumActive(), keyedObjectPool.getNumIdle(), handler.getId());
        return handler;
    }

    @Override
    public void returnObject(String key, JdbcNettyHandler handler) {
        SwiftLoggers.getLogger().info("return jdbc netty thread[{}]", key);
        keyedObjectPool.returnObject(key, handler);
        SwiftLoggers.getLogger().info("current thread pool size: max[{}],active[{}],idle[{}],handler[{}]"
                , keyedObjectPool.getMaxTotalPerKey(), keyedObjectPool.getNumActive(), keyedObjectPool.getNumIdle(), handler.getId());
    }

    @Override
    public void invalidateObject(String key, JdbcNettyHandler handler) throws Exception {
        keyedObjectPool.invalidateObject(key, handler);
    }

    @Override
    public void addObject(String key) throws Exception {
        keyedObjectPool.addObject(key);
    }

    @Override
    public int getNumIdle(String key) {
        return keyedObjectPool.getNumIdle(key);
    }

    @Override
    public int getNumActive(String key) {
        return keyedObjectPool.getNumActive(key);
    }

    @Override
    public int getNumIdle() {
        return keyedObjectPool.getNumIdle();
    }

    @Override
    public int getNumActive() {
        return keyedObjectPool.getNumActive();
    }

    @Override
    public void clear() {
        keyedObjectPool.clear();
    }

    @Override
    public void clear(String key) {
        keyedObjectPool.clear(key);
    }

    @Override
    public void close() {
        keyedObjectPool.close();
    }
}
