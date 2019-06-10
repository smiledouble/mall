package com.cxs.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.cxs.exec.BaseException;
import com.cxs.model.EgoResult;
import com.cxs.model.WeChatMessage;
import com.cxs.redis.RedisClient;
import com.cxs.service.LoginService;
import com.cxs.utils.AuthCodeUtil;
import com.cxs.utils.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author:chenxiaoshuang
 * @Date:2019/4/2 20:31
 */
@Controller
public class LoginController {

    @Reference
    private LoginService loginService;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private RedisClient redis;

    /**
     * 登录 将token写到
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("/user/sign")
    @ResponseBody
    public EgoResult writeToken(HttpServletRequest request, HttpServletResponse response, @RequestParam(required = true) String userName, @RequestParam(required = true) String password) {
        String token = null;
        String label = null;
        try {
            token = this.loginService.doLogin(userName, password);
        } catch (BaseException e) {
            return EgoResult.fail(701, e.getMsg());
        }
        if (token != null) {
            //说明登录成功，将用户的唯一标识token存放到一级域名里面，共享
            CookieUtil.setCookie(request, response, "X-TOKEN", token, 7 * 24 * 3600, true);

            //得到label ，就可以利用队列了
            label = CookieUtil.getCookieValue(request, "EGO-CART-LABEL");
            if (label != null) {
                jmsTemplate.convertAndSend("user.login.queue", label + "," + token);
            }

        }
        return EgoResult.ok();
    }

    /**
     * 跳回到离开的页面
     *
     * @param lastUrl
     * @return
     */
    @RequestMapping("/toLogin")
    public String toLogin(String lastUrl, Model model) {
        model.addAttribute("redirectUrl", lastUrl);
        return "login";
    }

    /**
     * 跳转到注册页面
     *
     * @return
     */
    @GetMapping("/toResigter")
    public ModelAndView toResigter() {
        ModelAndView view = new ModelAndView("resigter");
        return view;
    }

    /**
     * 发送验证码
     *
     * @param username
     * @return
     */
    @RequestMapping("/code/getCode")
    @ResponseBody
    public EgoResult getCode(String username) {
        //生成验证码
        String code = AuthCodeUtil.createCode(4);
        if (username == null) {
            return EgoResult.fail(400, "用户名不能为空");
        }
        WeChatMessage message = new WeChatMessage();
        message.setToUser(username);
        message.setTemplateId("io29TbPIg19Jk20Xg4oYAXmZDKkoPgMxtoWaB1_cxiQ");
        Map<String, Map<String, String>> data = new HashMap<>(16);
        data.put("code", WeChatMessage.buildInterMap(code, "#0033ff"));
        data.put("time", WeChatMessage.buildInterMap("3min", ""));
        message.setData(data);
        //通过微信发出去
        jmsTemplate.convertAndSend("weChat.message.queue", message);

        //放到redis里面去
        redis.set("Auth:" + username, code);
        redis.setExpire("Auth:" + username, 180);
        return EgoResult.ok("请查看您的手机");
    }

    /**
     * 验证验证码是否正确
     *
     * @param username
     * @param code
     * @return
     */
    @RequestMapping("/validate/code")
    @ResponseBody
    public EgoResult validateCode(String username, String code) {
        if (redis.isExsit("Auth:" + username)) {
            String redisCode = redis.get("Auth:" + username);
            if (redisCode.equals(code)) {
                return EgoResult.ok("验证成功");
            } else {
                return EgoResult.fail(400, "验证码错误，请重新输入");
            }
        }
        return EgoResult.fail(400, "验证码无效，请重新获取");
    }

    @RequestMapping("/doResigter")
    @ResponseBody
    public EgoResult doResigter(@RequestParam(required = true) String username, @RequestParam(required = true) String pwd, String code) {
        EgoResult egoResult = validateCode(username, code);
        if (egoResult.getStatus() == 200) {
            //验证成功
            int result = this.loginService.addUser(username, pwd);
            if (result > 0) {
                return EgoResult.ok();
            }
        } else {
            return EgoResult.fail(701, "验证码错误");
        }
        return null;
    }


}
