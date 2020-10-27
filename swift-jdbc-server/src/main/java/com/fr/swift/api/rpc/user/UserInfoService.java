package com.fr.swift.api.rpc.user;

import com.fr.swift.config.dao.SwiftDao;
import com.fr.swift.config.dao.SwiftDaoImpl;
import com.fr.swift.config.entity.SwiftUserInfo;
import com.fr.swift.config.entity.user.UserPermission;
import com.fr.swift.log.SwiftLoggers;
import com.fr.swift.source.core.MD5Utils;

import java.util.List;

/**
 * @author Hoky
 * @date 2020/10/21
 */
public class UserInfoService {
    private static final SwiftDao<SwiftUserInfo> dao = new SwiftDaoImpl<>(SwiftUserInfo.class);
    private static JDBCUserCache userCache = JDBCUserCache.INSTANCE;

    public static boolean verify(String username, String password) {
        String md5String = MD5Utils.getMD5String(new String[]{password});
        // TODO: 2020/10/26 如果修改了密码，需要删除usermap对应记录(暂时没有提供修改接口)
        if (userCache.get(username) != null) {
            SwiftLoggers.getLogger().info("verify userinfo in cache!");
            return userCache.get(username).getPassword().equals(md5String);
        }
        String hql = "select a from SwiftUserInfo a where a.username = :username";
        final List<?> select = dao.select(hql, query -> query.setParameter("username", username));
        if (select.isEmpty()) {
            return false;
        }
        String authCode = ((SwiftUserInfo) select.get(0)).getPassword();
        if (authCode.equals(md5String)) {
            JDBCUserInfo jdbcUserInfo = new JDBCUserInfo();
            jdbcUserInfo.setCreateTime(((SwiftUserInfo) select.get(0)).getCreateTime());
            jdbcUserInfo.setPassword(authCode);
            userCache.put(username, jdbcUserInfo);
            return true;
        }
        return false;
    }

    public static void insert(String username, String password, UserPermission scheme) {
        dao.insert(new SwiftUserInfo(username, password, scheme));
    }

}
