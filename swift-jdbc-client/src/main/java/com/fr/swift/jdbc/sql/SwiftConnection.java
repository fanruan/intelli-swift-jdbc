package com.fr.swift.jdbc.sql;

import java.sql.Connection;

/**
 * @author yee
 * @version 1.1
 * Created by yee on 2019-08-31
 */
public interface SwiftConnection extends Connection {
    ConnectionConfig getConfig();
}
