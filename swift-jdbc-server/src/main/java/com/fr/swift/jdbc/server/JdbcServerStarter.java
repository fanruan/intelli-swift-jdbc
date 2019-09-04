package com.fr.swift.jdbc.server;

import com.fr.swift.service.ServiceStarter;

/**
 * @author yee
 * @version 1.1
 * Created by yee on 2019-09-03
 */
public class JdbcServerStarter implements ServiceStarter {


    @Override
    public void start() throws Exception {
        new JdbcServer().start();
    }

    @Override
    public void stop() throws Exception {

    }
}
