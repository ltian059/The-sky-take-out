package com.sky.service;

import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;

public interface UserService {

    /**
     * User Wechat login
     *
     * @param userLoginDTO
     * @return
     */
    User userLogin(UserLoginDTO userLoginDTO);
}
