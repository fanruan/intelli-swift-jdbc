package com.fr.swift.cloud.jdbc.exception;

import java.sql.SQLException;

/**
 * @author yee
 * @date 2018/11/19
 */
class EnvironmentException extends SQLException {
    public EnvironmentException(Throwable cause) {
        super("Not a swift engine JVM! ", cause);
    }
}
