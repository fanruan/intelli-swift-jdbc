package com.fr.swift.cloud.jdbc.log;

import com.fr.swift.cloud.log.SwiftLogger;
import com.fr.swift.cloud.log.SwiftLoggerFactory;

/**
 * @author Heng.J
 * @date 2021/6/29
 * @description
 * @since swift-1.2.0
 */
public class SwiftJdbcLoggers implements SwiftLoggerFactory<Void> {

    private static final SwiftLogger LOGGER = new SwiftJdbcLogger();

    @Override
    public SwiftLogger apply(Void p) {
        return LOGGER;
    }
}