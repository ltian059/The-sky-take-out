package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private HttpClientUtil httpClientUtil = new HttpClientUtil();

    @Autowired
    private WeChatProperties weChatProperties;

    private static final String CODE2SESSION_URL = "https://api.weixin.qq.com/sns/jscode2session";

    @Autowired
    private UserMapper userMapper;

    @Override
    public User userLogin(UserLoginDTO userLoginDTO) {
        //Call Wechat API to get user openid
        String openid = getOpenId(userLoginDTO.getCode());
        //Check openid is null or not. if null, denote login failed, throw exception
        if (openid == null) {
            log.error("User login failed, code:{}, openid is null", userLoginDTO);
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        //Check user is exist or not. if not, create new user
        User user = userMapper.getUserByOpenid(openid);
        if (user == null) {
            user = User.builder()
                    .openid(openid)
                    .build();
            userMapper.insert(user);
        }
        //Return user
        return user;
    }

    /**
     * Call Wechat API to get user openid
     * @param code Wechat code from the wechat mini program
     * @return
     */
    private String getOpenId(String code) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("appid", weChatProperties.getAppid());
        paramMap.put("secret", weChatProperties.getSecret());
        paramMap.put("js_code", code);
        paramMap.put("grant_type", "authorization_code");

        String jsonStr = HttpClientUtil.doGet(CODE2SESSION_URL, paramMap);

        JSONObject jsonObject = JSON.parseObject(jsonStr);
        String openid = jsonObject.getString("openid");
        return openid;
    }
}
