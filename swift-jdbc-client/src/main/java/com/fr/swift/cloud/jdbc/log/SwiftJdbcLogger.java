package com.fr.swift.cloud.jdbc.log;

import com.fr.general.log.MessageFormatter.FormattingTuple;
import com.fr.log.FineLoggerFactory;
import com.fr.swift.cloud.log.SwiftLogger;

/**
 * @author Heng.J
 * @date 2021/6/29
 * @description
 * @since swift-1.2.0
 */
public class SwiftJdbcLogger extends BaseJdbcLogger implements SwiftLogger {

    SwiftJdbcLogger() {
    }

    @Override
    public String getName() {
        return FineLoggerFactory.getLogger().toString();
    }

    @Override
    public void trace(String s) {
    }

    @Override
    public void trace(String s, Object o) {
    }

    @Override
    public void trace(String s, Object o, Object o1) {
    }

    @Override
    public void trace(String s, Object... objects) {
    }

    @Override
    public void trace(String s, Throwable throwable) {
    }

    @Override
    public void debug(String s) {
        FineLoggerFactory.getLogger().debug(s);
    }

    @Override
    public void debug(String s, Object o) {
        FormattingTuple tuple = format(s, o);
        FineLoggerFactory.getLogger().debug(tuple.getMessage());
    }

    @Override
    public void debug(String s, Object o, Object o1) {
        FormattingTuple tuple = format(s, o, o1);
        FineLoggerFactory.getLogger().debug(tuple.getMessage());
    }

    @Override
    public void debug(String s, Object... objects) {
        FormattingTuple tuple = format(s, objects);
        FineLoggerFactory.getLogger().debug(tuple.getMessage());
    }

    @Override
    public void debug(String s, Throwable throwable) {
        FineLoggerFactory.getLogger().debug(s, throwable);
    }

    @Override
    public void info(String s) {
        FineLoggerFactory.getLogger().info(s);
    }

    @Override
    public void info(String s, Object o) {
        FormattingTuple tuple = format(s, o);
        FineLoggerFactory.getLogger().info(tuple.getMessage(), tuple.getThrowable());
    }

    @Override
    public void info(String s, Object o, Object o1) {
        FormattingTuple tuple = format(s, o, o1);
        FineLoggerFactory.getLogger().info(tuple.getMessage(), tuple.getThrowable());
    }

    @Override
    public void info(String s, Object... objects) {
        FormattingTuple tuple = format(s, objects);
        FineLoggerFactory.getLogger().info(tuple.getMessage(), tuple.getThrowable());
    }

    @Override
    public void info(String s, Throwable throwable) {
        FineLoggerFactory.getLogger().info(s, throwable);
    }

    @Override
    public void warn(String s) {
        FineLoggerFactory.getLogger().warn(s);
    }

    @Override
    public void warn(String s, Object o) {
        FormattingTuple tuple = format(s, o);
        FineLoggerFactory.getLogger().warn(tuple.getMessage(), tuple.getThrowable());
    }

    @Override
    public void warn(String s, Object o, Object o1) {
        FormattingTuple tuple = format(s, o, o1);
        FineLoggerFactory.getLogger().warn(tuple.getMessage(), tuple.getThrowable());
    }

    @Override
    public void warn(String s, Object... objects) {
        FormattingTuple tuple = format(s, objects);
        FineLoggerFactory.getLogger().warn(tuple.getMessage(), tuple.getThrowable());
    }

    @Override
    public void warn(String s, Throwable throwable) {
        FineLoggerFactory.getLogger().warn(s, throwable);
    }

    @Override
    public void error(String s) {
        FineLoggerFactory.getLogger().error(s);
    }

    @Override
    public void error(String s, Object o) {
        FormattingTuple tuple = format(s, o);
        FineLoggerFactory.getLogger().error(tuple.getMessage(), tuple.getThrowable());
    }

    @Override
    public void error(String s, Object o, Object o1) {
        FormattingTuple tuple = format(s, o, o1);
        FineLoggerFactory.getLogger().error(tuple.getMessage(), tuple.getThrowable());
    }

    @Override
    public void error(String s, Object... objects) {
        FormattingTuple tuple = format(s, objects);
        FineLoggerFactory.getLogger().error(tuple.getMessage(), tuple.getThrowable());
    }

    @Override
    public void error(String s, Throwable throwable) {
        FineLoggerFactory.getLogger().error(s, throwable);
    }
}