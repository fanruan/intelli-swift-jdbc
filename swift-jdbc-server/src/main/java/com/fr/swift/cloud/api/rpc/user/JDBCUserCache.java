package com.fr.swift.cloud.api.rpc.user;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Hoky
 * @date 2020/10/26
 */
public enum JDBCUserCache {
    INSTANCE;
    private long cacheTime = TimeUnit.HOURS.toMillis(1);
    private final Map<String, JDBCUserInfo> userMap = new ConcurrentHashMap<>();
    private ScheduledExecutorService scheduledPool = new ScheduledThreadPoolExecutor(1);

    JDBCUserCache() {
        scheduledPool.scheduleWithFixedDelay(() -> {
            long now = System.currentTimeMillis();
            Iterator<Map.Entry<String, JDBCUserInfo>> iterator = userMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, JDBCUserInfo> next = iterator.next();
                if (next.getValue().getCreateTime().getTime() + cacheTime < now) {
                    iterator.remove();
                }
            }
        }, 1, 1, TimeUnit.MINUTES);
    }

    public void put(String userName, JDBCUserInfo jdbcUserInfo) {
        userMap.put(userName, jdbcUserInfo);
    }

    public JDBCUserInfo get(String userName) {
        JDBCUserInfo jdbcUserInfo = userMap.get(userName);
        long now = System.currentTimeMillis();
        if (jdbcUserInfo != null && jdbcUserInfo.getCreateTime().getTime() + cacheTime > now) {
            jdbcUserInfo.setCreateTime(new Date(now));
            return jdbcUserInfo;
        }
        return null;
    }

}
