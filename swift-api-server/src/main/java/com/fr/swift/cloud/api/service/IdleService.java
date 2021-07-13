package com.fr.swift.cloud.api.service;

/**
 * @author Heng.J
 * @date 2021/7/12
 * @description
 * @since swift-1.2.0
 */
public interface IdleService {

    JdbcIdleResponse heartBeat();
}
