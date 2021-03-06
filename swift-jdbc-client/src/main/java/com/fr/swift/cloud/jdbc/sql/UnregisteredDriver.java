package com.fr.swift.cloud.jdbc.sql;

import com.fr.swift.cloud.jdbc.BuildInConnectionProperty;
import com.fr.swift.cloud.jdbc.JdbcProperty;
import com.fr.swift.cloud.jdbc.Mode;
import com.fr.swift.cloud.jdbc.SwiftJdbcConstants;
import com.fr.swift.cloud.jdbc.exception.Exceptions;
import com.fr.swift.cloud.jdbc.request.JdbcRequestService;
import com.fr.swift.cloud.jdbc.request.impl.RequestServiceImpl;
import com.fr.swift.cloud.util.Strings;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * @author yee
 * @date 2018/11/16
 */
public abstract class UnregisteredDriver implements Driver {
    protected Holder holder;
    private static final String URL_PATTERN = "jdbc:swift:(remote)://(((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})(\\.((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})){3}:\\d+/.*|default)";

    public UnregisteredDriver() {
        this.holder = new Holder();
        this.holder.realtimeAddresses = new LinkedBlockingQueue<String>();
        this.holder.analyseAddresses = new LinkedBlockingQueue<String>();
        this.holder.requestService = new RequestServiceImpl();
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        if (null == url || SwiftJdbcConstants.EMPTY.equals(url.trim())) {
            throw Exceptions.urlEmpty();
        }
        String testUrl = url.trim().toLowerCase();
        if (!acceptsURL(testUrl)) {
            return null;
        }
        holder.connectUri = URI.create(testUrl.substring(getConnectionSchema().length() + 1));
        info.put(BuildInConnectionProperty.URL.getPropertyName(), url);
        String schema = holder.connectUri.getScheme();
        if (null == schema) {
            throw Exceptions.urlFormat(url);
        }
        Mode mode;
        try {
            mode = Mode.fromKey(schema);
            holder.mode = mode;
        } catch (Exception e) {
            throw Exceptions.urlFormat(url);
        }
        final String query = holder.connectUri.getQuery();
        parseQueryProperties(query, info);
        if (Mode.EMB.equals(mode)) {
            try {
                Class clazz = Class.forName("com.fr.swift.cloud.jdbc.sql.EmbSwiftConnection");
                Constructor constructor = clazz.getDeclaredConstructor(UnregisteredDriver.class, Properties.class);
                constructor.setAccessible(true);
                return (Connection) constructor.newInstance(this, info);
            } catch (Exception e) {
                throw Exceptions.environment(e);
            }
        } else {
            return new RemoteConnection(this, info);
        }

    }

    @Override
    public boolean acceptsURL(String url) {
        return Pattern.matches(URL_PATTERN, url);
    }

    /**
     * getConnectionSchema
     *
     * @return schema like jdbc:swift
     */
    protected abstract String getConnectionSchema();

    /**
     * 解析下query
     *
     * @param query
     * @param properties
     * @throws SQLException
     */
    private void parseQueryProperties(String query, Properties properties) throws SQLException {
        if (Strings.isNotEmpty(query)) {
            final String[] queries = query.split("&");
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (DataOutputStream dos = new DataOutputStream(out)) {
                for (String q : queries) {
                    dos.writeBytes(q);
                    dos.writeBytes("\n");
                }
                properties.load(new ByteArrayInputStream(out.toByteArray()));
            } catch (IOException e) {
                throw new SQLException(e);
            }
        }
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) {
        List<DriverPropertyInfo> list = new ArrayList<DriverPropertyInfo>();
        for (Map.Entry<Object, Object> entry : info.entrySet()) {
            list.add(new DriverPropertyInfo((String) entry.getKey(), (String) entry.getValue()));
        }
        for (BuildInConnectionProperty value : BuildInConnectionProperty.values()) {
            list.add(new DriverPropertyInfo(value.getPropertyName(), value.getDefaultValue().toString()));
        }
        return list.toArray(new DriverPropertyInfo[0]);
    }

    @Override
    public int getMajorVersion() {
        return JdbcProperty.get().getMajorVersion();
    }

    @Override
    public int getMinorVersion() {
        return JdbcProperty.get().getMinorVersion();
    }

    @Override
    public boolean jdbcCompliant() {
        return JdbcProperty.get().isCompliant();
    }

    public Logger getParentLogger() {
        return Logger.getLogger(SwiftJdbcConstants.EMPTY);
    }


    protected void register() {
        try {
            DriverManager.registerDriver(this);
        } catch (SQLException ignore) {
        }
    }

    public static class Holder {
        private URI connectUri;
        private JdbcRequestService requestService;
        private Mode mode;
        /**
         * TODO: 2018/12/03 校验码 暂时不做先保留
         */
        private String authCode = UUID.randomUUID().toString();
        private Queue<String> realtimeAddresses;
        private Queue<String> analyseAddresses;

        public URI getConnectUri() {
            return connectUri;
        }

        public String getAuthCode() {
            return authCode;
        }

        public JdbcRequestService getRequestService() {
            return requestService;
        }

        public void setAuthCode(String authCode) {
            this.authCode = authCode;
        }

        public void setRealtimeAddresses(Queue<String> realtimeAddresses) {
            this.realtimeAddresses = realtimeAddresses;
        }

        public void setAnalyseAddresses(Queue<String> analyseAddresses) {
            this.analyseAddresses = analyseAddresses;
        }

        synchronized
        public String nextRealTime() {
            if (mode.equals(Mode.EMB)) {
                return "default";
            }
            String address = realtimeAddresses.poll();
            if (null == address) {
                throw Exceptions.addressNotFound("Insert service");
            }
            realtimeAddresses.offer(address);
            return address;
        }

        synchronized
        public String nextAnalyse() {
            if (mode.equals(Mode.EMB)) {
                return "default";
            }
            String address = analyseAddresses.poll();
            if (null == address) {
                throw Exceptions.addressNotFound("Analyse service");
            }
            analyseAddresses.offer(address);
            return address;
        }
    }
}