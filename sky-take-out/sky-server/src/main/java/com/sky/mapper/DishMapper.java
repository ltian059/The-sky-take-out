package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);


    /**
     * Add a new dish record
     * @param dish
     */
    @AutoFill(OperationType.INSERT)
    void insert(Dish dish);

    /**
     * Page query for dish
     * @param dto
     * @return
     */
    Page<DishVO> pageQuery(DishPageQueryDTO dto);


    Integer delete(Long[] ids);

    List<Dish> getByIds(Long[] ids);

    /**
     * Count how many dishes are on sale among the dishes.
     * @param ids
     * @return the number of dishes that are on sale.
     */
    Integer countOnStatus(Long[] ids, Integer status);

    @AutoFill(OperationType.UPDATE)
    Integer updateDishes(List<Dish> dishes);


    @Select("SELECT * FROM dish WHERE category_id = #{categoryId}")
    List<Dish> selectByCategoryId(Long categoryId);
}
