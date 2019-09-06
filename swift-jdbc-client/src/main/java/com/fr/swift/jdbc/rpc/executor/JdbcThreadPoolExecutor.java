package com.fr.swift.jdbc.rpc.executor;

import com.fr.swift.jdbc.thread.JdbcThreadFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author yee
 * @version 1.1
 * Created by yee on 2019-09-06
 */
public class JdbcThreadPoolExecutor extends ThreadPoolExecutor {
    private JdbcThreadPoolExecutor(int poolSize, String namePrefix) {
        super(poolSize, poolSize, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        setThreadFactory(new JdbcThreadFactory.Builder().setName(namePrefix).build());
    }

    public static ExecutorService newInstance(int poolSize, String namePrefix) {
        return new JdbcThreadPoolExecutor(poolSize, namePrefix);
    }
}
