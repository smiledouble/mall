package com.cxs.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.cxs.domain.Admin;
import com.cxs.domain.AdminExample;
import com.cxs.domain.AdminWithBLOBs;
import com.cxs.exec.BaseException;
import com.cxs.mapper.AdminMapper;
import com.cxs.redis.RedisClient;
import com.cxs.service.LoginService;
import com.cxs.utils.Md5Util;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

/**
 * @Author:chenxiaoshuang
 * @Date:2019/4/3 9:58
 */
@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private RedisClient redis;


    /**
     * 登录的方法
     *
     * @param userName
     * @param password
     * @return
     */
    @Override
    public String doLogin(String userName, String password) {
        AdminExample example = new AdminExample();
        example.createCriteria().andUserNameEqualTo(userName);
        List<Admin> admins = this.adminMapper.selectByExample(example);
        if (admins.size() == 0) {
            throw new BaseException("用户不存在，请先注册哦");
        }
        if (admins.size() > 1) {
            throw new BaseException("系统正在升级中");
        }
        Admin admin = admins.get(0);
        //得到数据库的密码
        String dbPwd = admin.getPassword();
        //得到数据库的盐
        String ecSalt = admin.getEcSalt();
        if (!dbPwd.equals(Md5Util.GetMD5WithSalt(password, ecSalt))) {
            throw new BaseException("密码不正确哦");
        }
        //如果都正确就 存到redis里面 记得加密
        admin.setEcSalt("****");
        admin.setPassword("****");
        String token = UUID.randomUUID().toString();
        redis.set(token, JSON.toJSONString(admin));
        return token;
    }

    /**
     * 注册用户
     *
     * @param username
     * @param pwd
     * @return
     */
    @Override
    public int addUser(String username, String pwd) {
        AdminWithBLOBs user = new AdminWithBLOBs();
        user.setUserName(username);
        String dbPwd = Md5Util.GetMD5WithSalt(pwd, "cxs");
        user.setPassword(dbPwd);
        user.setEcSalt("cxs");
        int i = adminMapper.insertSelective(user);
        return i;
    }
}
