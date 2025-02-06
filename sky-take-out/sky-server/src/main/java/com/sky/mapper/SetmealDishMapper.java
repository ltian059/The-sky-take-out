package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    List<Long> getSetmealIdsByDishIds(Long[] dishIds);

    Integer insertBatch(List<SetmealDish> setmealDishes);

    List<SetmealDish> getSetmealDishesBySetmealIds(Long[] setmealIds);

    /**
     * Delete all the setmeal dishes of the selected setmeals
     * @param ids The setmeals ids.
     */
    void deleteBatchBySetmealIds(List<Long> ids);
}
