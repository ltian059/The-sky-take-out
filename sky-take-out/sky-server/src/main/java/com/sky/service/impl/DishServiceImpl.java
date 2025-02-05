package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Override
    @Transactional
    public void addDishWithFlavor(DishDTO dishDTO) {
        //Add one record to dish table
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.insert(dish);

        //Get the primary key from the insertion.
        Long dishId = dish.getId();

        //Add n records to flavor table
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (!flavors.isEmpty()){
            flavors.forEach(dishFlavor -> dishFlavor.setDishId(dishId));
            //Insert n records into flavor table
            dishFlavorMapper.insertBatch(flavors);
        }

    }

    /**
     * Dish Page Query
     * @param dto
     * @return
     */
    @Override
    public PageResult<DishVO> pageQuery(DishPageQueryDTO dto) {
        PageHelper.startPage(dto.getPage(), dto.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dto);
        return new PageResult<>(page.getTotal(), page.getResult());
    }

    /**
     *
     * @param ids
     * @return
     */
    @Override
    @Transactional
    public Integer deleteInBatch(Long[] ids) {
        //1. Check if the dish is on sale or disabled. Only disabled dishes can be deleted.
        List<Dish> dishes = dishMapper.getByIds(ids);
        for(Dish d : dishes){
            if(Objects.equals(d.getStatus(), StatusConstant.ENABLE)){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        //2. Check if the dish is correlated with setmeals
        List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(ids);
        if(!setmealIds.isEmpty()){
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        //3. Delete dish records in the dish table
        Integer count = dishMapper.delete(ids);
        //4. Delete flavor records correlated with the deleted dishes.
        Integer dfCount = dishFlavorMapper.deleteByDishIds(ids);
        return count;
    }
}
