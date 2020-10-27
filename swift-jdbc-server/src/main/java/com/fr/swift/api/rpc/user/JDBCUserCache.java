package com.fr.swift.api.rpc.user;

import com.fr.swift.log.SwiftLoggers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Hoky
 * @date 2020/10/26
 */
public enum JDBCUserCache {
    INSTANCE;
    private long CACHE_TIME = 36000000L;
    private final Map<String, JDBCUserInfo> USER_MAP = new ConcurrentHashMap<>();
    private ScheduledExecutorService scheduledPool = new ScheduledThreadPoolExecutor(1);
    private PriorityBlockingQueue<String> userQueue = new PriorityBlockingQueue<>();

    JDBCUserCache() {
        scheduledPool.scheduleWithFixedDelay(() -> {
            long now = System.currentTimeMillis();
            while (true) {
                String userName = userQueue.peek();
                if (userName == null || USER_MAP.get(userName).getCreateTime().getTime() + CACHE_TIME > now) {
                    return;
                }
                USER_MAP.remove(userName);
                String deleteUser = userQueue.poll();
                SwiftLoggers.getLogger().info("delete user in cache:" + deleteUser);
            }
        }, 1, 1, TimeUnit.MINUTES);
    }

    public void put(String userName, JDBCUserInfo jdbcUserInfo) {
        if (USER_MAP.containsKey(userName)) {
            USER_MAP.put(userName, jdbcUserInfo);
            userQueue.remove(userName);
        }
        USER_MAP.put(userName, jdbcUserInfo);
        userQueue.add(userName);
    }

    public JDBCUserInfo get(String userName) {
        JDBCUserInfo jdbcUserInfo = USER_MAP.get(userName);
        if (jdbcUserInfo != null && jdbcUserInfo.getCreateTime().getTime() + CACHE_TIME > System.currentTimeMillis()) {
            return jdbcUserInfo;
        }
        return null;
    }

    public boolean containUser(String userName) {
        if (get(userName) != null) {
            return true;
        }
        return false;
    }

}
