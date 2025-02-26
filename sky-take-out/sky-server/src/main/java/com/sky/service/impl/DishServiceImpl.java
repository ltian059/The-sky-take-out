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
import com.sky.utils.AmazonS3Util;
import com.sky.vo.DishVO;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private AmazonS3Util amazonS3Util;

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
        if (!flavors.isEmpty()) {
            flavors.forEach(dishFlavor -> dishFlavor.setDishId(dishId));
            //Insert n records into flavor table
            dishFlavorMapper.insertBatch(flavors);
        }

    }

    /**
     * Dish Page Query
     *
     * @param dto
     * @return
     */
    @Override
    public PageResult<DishVO> pageQuery(DishPageQueryDTO dto) {
        PageHelper.startPage(dto.getPage(), dto.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dto);
        List<DishVO> result = page.getResult();
        return new PageResult<>(page.getTotal(), page.getResult());
    }

    /**
     * @param ids
     * @return
     */
    @Override
    @Transactional
    public Integer deleteInBatch(Long[] ids) {
        //1. Check if the dish is on sale or disabled. Only disabled dishes can be deleted.
        Integer onSale = dishMapper.countOnStatus(ids, StatusConstant.ENABLE);
        if (onSale > 0) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
        }

        //2. Check if the dish is correlated with setmeals
        List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(ids);
        if (!setmealIds.isEmpty()) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        //3. Delete dish records in the dish table
        Integer count = dishMapper.delete(ids);
        //4. Delete flavor records correlated with the deleted dishes.
        Integer dfCount = dishFlavorMapper.deleteByDishIds(ids);
        return count;
    }


    @Override
    public Integer toggleDishStatus(Long id, Integer status) {
        List<Dish> dishes = new ArrayList<>();
        Dish dish = Dish.builder().id(id).status(status).build();
        dishes.add(dish);
        Integer update = dishMapper.updateDishes(dishes);
        return update;
    }

    @Override
    public DishVO getDishWithFlavorById(Long id) {
        //1. Get the dish
        List<Dish> byIds = dishMapper.getByIds(new Long[]{id});
        if (byIds.isEmpty()) throw new RuntimeException("The dish cannot be found.");
        Dish dish = byIds.get(0);
        //2. Get the flavors belonging to the dish
        List<DishFlavor> flavors = dishFlavorMapper.getByDishId(id);
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(flavors);

        return dishVO;
    }

    @Override
    @Transactional
    public Integer updateDish(DishDTO dishDTO) {
        ArrayList<DishDTO> list = new ArrayList<>();
        list.add(dishDTO);
        ArrayList<Dish> dishes = new ArrayList<>();
        list.forEach(dto -> {
            Dish dish = new Dish();
            BeanUtils.copyProperties(dto, dish);
            dishes.add(dish);
        });
        //1. update the dish table
        Integer update = dishMapper.updateDishes(dishes);
        //2. update the dish_flavor table: Delete all flavors of the dish, then add updated flavors of the dish.
        Integer deletes = dishFlavorMapper.deleteByDishIds(dishes.stream()
                .map(Dish::getId)
                .toArray(Long[]::new));
        list.forEach(dto -> {
            if(!dto.getFlavors().isEmpty()){
                dto.getFlavors().forEach(flavor -> flavor.setDishId(dto.getId()));
                dishFlavorMapper.insertBatch(dto.getFlavors());
            }
        });
        return update;
    }

    @Override
    public List<Dish> listDishByCategoryId(Long categoryId) {

        return dishMapper.selectByCategoryId(categoryId);
    }
    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {

        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }

    @Override
    public Dish getById(Long id) {
        List<Dish> byIds = dishMapper.getByIds(new Long[]{id});
        return byIds.get(0);
    }
}
