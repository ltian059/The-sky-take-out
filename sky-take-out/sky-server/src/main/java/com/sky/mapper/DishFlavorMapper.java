package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishFlavorMapper {


    /**
     * Insert flavor records in batch.
     * @param flavors
     */
    void insertBatch(List<DishFlavor> flavors);

    Integer deleteByDishIds(Long[] ids);

    @Select("SELECT * FROM dish_flavor WHERE dish_id = #{id}")
    List<DishFlavor> getByDishId(Long id);
}
