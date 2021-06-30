package com.fr.swift.cloud.jdbc.log;

import com.fr.general.log.MessageFormatter;
import com.fr.swift.cloud.util.Strings;
import org.slf4j.Marker;

/**
 * @author Heng.J
 * @date 2021/6/29
 * @description
 * @since swift-1.2.0
 */
public abstract class BaseJdbcLogger implements JdbcLogger {

    static MessageFormatter.FormattingTuple format(String s, Object... objects) {
        return MessageFormatter.arrayFormat(s, objects);
    }

    @Override
    public void debug(Throwable t) {
        debug(Strings.EMPTY, t);
    }

    @Override
    public void warn(Throwable t) {
        warn(Strings.EMPTY, t);
    }

    @Override
    public void error(Throwable t) {
        error(Strings.EMPTY, t);
    }

    @Override
    public void debug(String msg) {
        debug(msg, (Object[]) null);
    }

    @Override
    public void info(String msg) {
        info(msg, (Object[]) null);
    }

    @Override
    public void warn(String msg) {
        warn(msg, (Object[]) null);
    }

    @Override
    public void error(String s, Throwable throwable) {
        error(s, new Object[]{throwable});
    }

    @Override
    public void error(String msg) {
        error(msg, (Object[]) null);
    }

    @Override
    public boolean isTraceEnabled() {
        return false;
    }

    @Override
    public boolean isDebugEnabled() {
        return true;
    }

    @Override
    public boolean isInfoEnabled() {
        return true;
    }

    @Override
    public boolean isWarnEnabled() {
        return true;
    }

    @Override
    public boolean isErrorEnabled() {
        return true;
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return isErrorEnabled();
    }

    @Override
    public void error(Marker marker, String s) {
    }

    @Override
    public void error(Marker marker, String s, Object o) {
    }

    @Override
    public void error(Marker marker, String s, Object o, Object o1) {
    }

    @Override
    public void error(Marker marker, String s, Object... objects) {
    }

    @Override
    public void error(Marker marker, String s, Throwable throwable) {
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return isWarnEnabled();
    }

    @Override
    public void warn(Marker marker, String s) {
    }

    @Override
    public void warn(Marker marker, String s, Object o) {
    }

    @Override
    public void warn(Marker marker, String s, Object o, Object o1) {
    }

    @Override
    public void warn(Marker marker, String s, Object... objects) {
    }

    @Override
    public void warn(Marker marker, String s, Throwable throwable) {
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return isInfoEnabled();
    }

    @Override
    public void info(Marker marker, String s) {
    }

    @Override
    public void info(Marker marker, String s, Object o) {
    }

    @Override
    public void info(Marker marker, String s, Object o, Object o1) {
    }

    @Override
    public void info(Marker marker, String s, Object... objects) {
    }

    @Override
    public void info(Marker marker, String s, Throwable throwable) {
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return isDebugEnabled();
    }

    @Override
    public void debug(Marker marker, String s) {
    }

    @Override
    public void debug(Marker marker, String s, Object o) {
    }

    @Override
    public void debug(Marker marker, String s, Object o, Object o1) {
    }

    @Override
    public void debug(Marker marker, String s, Object... objects) {
    }

    @Override
    public void debug(Marker marker, String s, Throwable throwable) {
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return isTraceEnabled();
    }

    @Override
    public void trace(Marker marker, String s) {
    }

    @Override
    public void trace(Marker marker, String s, Object o) {
    }

    @Override
    public void trace(Marker marker, String s, Object o, Object o1) {
    }

    @Override
    public void trace(Marker marker, String s, Object... objects) {
    }

    @Override
    public void trace(Marker marker, String s, Throwable throwable) {
    }
}
