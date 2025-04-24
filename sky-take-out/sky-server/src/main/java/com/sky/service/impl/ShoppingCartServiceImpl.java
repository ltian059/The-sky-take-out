package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    /**
     * 添加购物车
     *
     * @param shoppingCartDTO
     * @return
     */
    @Override
    public void add(ShoppingCartDTO shoppingCartDTO) {
        // 1. 判断当前商品是否已经存在，如果已经存在，则直接修改number字段
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setUserId(BaseContext.getCurrentId()); //通过 threadLocal.get() 获取当前登录用户的id
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        if (list != null && !list.isEmpty()) {
            // 如果存在，则修改数量
            ShoppingCart cart = list.get(0);
            cart.setNumber(cart.getNumber() + 1);
            shoppingCartMapper.updateNumberById(cart);
        }
        // 2. 如果不存在，则添加到购物车
        else {
            //2.1 判断当前添加的商品是菜品还是套餐
            Long setmealId = shoppingCartDTO.getSetmealId();
            Long dishId = shoppingCartDTO.getDishId();
            if(setmealId != null){
                //套餐
                //2.2 根据套餐id查询套餐信息
                Setmeal setmeal = setmealMapper.selectById(setmealId);
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());
            }else if(dishId != null){
                //菜品
                List<Dish> dishes = dishMapper.getByIds(new Long[]{dishId});
                if(dishes == null || dishes.isEmpty()){
                    return;
                }
                Dish dish = dishes.get(0);
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());
            }
        }
        shoppingCart.setNumber(1);
        // 3. 插入数据到购物车表
        shoppingCartMapper.insert(shoppingCart);
    }

    /**
     * 查看购物车
     *
     * @return
     */
    @Override
    public List<ShoppingCart> showShoppingCart() {
        Long userId = BaseContext.getCurrentId(); //获取当前微信用户id
        ShoppingCart build = ShoppingCart.builder().userId(userId).build();
        return shoppingCartMapper.list(build);
    }

    @Override
    public void clean() {
        shoppingCartMapper.deleteByUserId(BaseContext.getCurrentId());
    }
}
