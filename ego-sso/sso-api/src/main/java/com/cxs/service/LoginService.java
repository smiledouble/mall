package com.cxs.service;

/**
 * @Author:chenxiaoshuang
 * @Date:2019/4/3 9:57
 */
public interface LoginService {
    /**
     * 登录的方法
     *
     * @param userName
     * @param password
     * @return
     */
    String doLogin(String userName, String password);

    /**
     * 注册用户
     *
     * @param username
     * @param pwd
     * @return
     */
    int addUser(String username, String pwd);
}
