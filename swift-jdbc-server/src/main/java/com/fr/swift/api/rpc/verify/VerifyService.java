package com.fr.swift.api.rpc.verify;

/**
 * @author Hoky
 * @date 2020/10/21
 */
public class VerifyService {
    public static boolean verify(String authcode) {
        return authcode.equals("df9829a3");
    }
}
