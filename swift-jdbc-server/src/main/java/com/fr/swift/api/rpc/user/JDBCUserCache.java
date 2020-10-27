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
    private long cacheTime = 36000000L;
    private final Map<String, JDBCUserInfo> userMap = new ConcurrentHashMap<>();
    private ScheduledExecutorService scheduledPool = new ScheduledThreadPoolExecutor(1);
    private PriorityBlockingQueue<String> userQueue = new PriorityBlockingQueue<>();

    JDBCUserCache() {
        scheduledPool.scheduleWithFixedDelay(() -> {
            long now = System.currentTimeMillis();
            while (true) {
                String userName = userQueue.peek();
                if (userName == null || userMap.get(userName).getCreateTime().getTime() + cacheTime > now) {
                    return;
                }
                userMap.remove(userName);
                String deleteUser = userQueue.poll();
                SwiftLoggers.getLogger().info("delete user in cache:" + deleteUser);
            }
        }, 1, 1, TimeUnit.MINUTES);
    }

    public void put(String userName, JDBCUserInfo jdbcUserInfo) {
        if (userMap.containsKey(userName)) {
            userMap.put(userName, jdbcUserInfo);
            userQueue.remove(userName);
        }
        userMap.put(userName, jdbcUserInfo);
        userQueue.add(userName);
    }

    public JDBCUserInfo get(String userName) {
        JDBCUserInfo jdbcUserInfo = userMap.get(userName);
        if (jdbcUserInfo != null && jdbcUserInfo.getCreateTime().getTime() + cacheTime > System.currentTimeMillis()) {
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
