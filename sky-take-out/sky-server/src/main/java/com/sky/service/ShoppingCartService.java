package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface ShoppingCartService {


    /**
     * 添加购物车
     *
     * @param shoppingCartDTO
     * @return
     */
    void add(@RequestBody ShoppingCartDTO shoppingCartDTO);

    /**
     * 查看购物车
     *
     * @return
     */
    List<ShoppingCart> showShoppingCart();

    /**
     * 清空购物车
     */
    void clean();
}

