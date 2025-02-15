package com.sky.service;


import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {

    /**
     * Add new dish and its flavor
     * @param dishDTO
     */
    public void addDishWithFlavor(DishDTO dishDTO);

    PageResult<DishVO> pageQuery(DishPageQueryDTO dto);

    Integer deleteInBatch(Long[] ids);

    Integer toggleDishStatus(Long id, Integer status);

    DishVO getDishWithFlavorById(Long id);

    Integer updateDish(DishDTO dishDTO);

    List<Dish> listDishByCategoryId(Long categoryId);

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    List<DishVO> listWithFlavor(Dish dish);
}
