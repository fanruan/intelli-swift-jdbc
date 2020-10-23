package com.fr.swift.api.rpc.user;

import com.fr.swift.config.dao.SwiftDao;
import com.fr.swift.config.dao.SwiftDaoImpl;
import com.fr.swift.config.entity.SwiftUserInfo;
import com.fr.swift.config.entity.user.UserPermission;
import com.fr.swift.source.core.MD5Utils;

import java.util.List;

/**
 * @author Hoky
 * @date 2020/10/21
 */
public class UserInfoService {
    private static final SwiftDao<SwiftUserInfo> dao = new SwiftDaoImpl<>(SwiftUserInfo.class);

    public static boolean verify(String username, String password) {
        String hql = "select a from SwiftUserInfo a where a.username = :username";
        final List<?> select = dao.select(hql, query -> query.setParameter("username", username));
        String authCode = ((SwiftUserInfo)select.get(0)).getPassword();
        return authCode.equals(MD5Utils.getMD5String(new String[]{password}));
    }

    public static void insert(String username, String password, UserPermission scheme) {
        dao.insert(new SwiftUserInfo(username,password,scheme));
    }
}
