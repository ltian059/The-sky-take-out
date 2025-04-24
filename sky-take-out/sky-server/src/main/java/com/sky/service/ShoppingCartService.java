package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import org.springframework.web.bind.annotation.RequestBody;

public interface ShoppingCartService {


    /**
     * 添加购物车
     *
     * @param shoppingCartDTO
     * @return
     */
    void add(@RequestBody ShoppingCartDTO shoppingCartDTO);
}

