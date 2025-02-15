package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Override
    @Transactional
    public void addSetmeal(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        //1. Add setmeal to the setmeal table
        Integer smAdd = setmealMapper.insert(setmeal);
        if (smAdd == 0) throw new RuntimeException("Add setmeal dishes exception...");
        Long setmealId = setmeal.getId();
        //2. add the dishes in the setmeal to the table setmeal_dish
        setmealDTO.getSetmealDishes().forEach(setmealDish -> setmealDish.setSetmealId(setmealId));
        Integer smdAdd = setmealDishMapper.insertBatch(setmealDTO.getSetmealDishes());
        if (smdAdd == 0) throw new RuntimeException("Add setmeal dishes exception...");

    }

    @Override
    public PageResult<SetmealVO> page(SetmealPageQueryDTO pageQueryDTO) {
        PageHelper.startPage(pageQueryDTO.getPage(), pageQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.pageQuery(pageQueryDTO);

        //List<SetmealVO> setmealVOList = correlateSetmealDishes(page.getResult());
        return new PageResult<>(page.getTotal(), page.getResult());
    }

    @Override
    @Transactional
    public void deleteSetmeals(List<Long> ids) {
        //1.Delete the records in the table setmeal
        setmealMapper.deleteBatch(ids);
        //2.Delete the dishes of each setmeal in the table setmeal_dish
        setmealDishMapper.deleteBatchBySetmealIds(ids);
    }

    @Override
    public SetmealVO getSetmealVO(Long id) {
        Setmeal setmeal = setmealMapper.selectById(id);
        SetmealVO vo = new SetmealVO();
        BeanUtils.copyProperties(setmeal, vo);

        ArrayList<SetmealVO> setmealVOS = new ArrayList<>();
        setmealVOS.add(vo);
        correlateSetmealDishes(setmealVOS);

        return setmealVOS.get(0);
    }

    @Override
    public void updateSetmeal(SetmealDTO setmealDTO) {
        //1. Update setmeal table
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.update(setmeal);

        //2. Update setmeal_dish table
        ArrayList<Long> setmealIdList = new ArrayList<>();
        setmealIdList.add(setmealDTO.getId());
        setmealDishMapper.deleteBatchBySetmealIds(setmealIdList);
        if (!setmealDTO.getSetmealDishes().isEmpty()){
            setmealDTO.getSetmealDishes().forEach(smd->{
                smd.setSetmealId(setmealDTO.getId());
            });
            setmealDishMapper.insertBatch(setmealDTO.getSetmealDishes());
        }
    }

    @Override
    public void toggleStatus(Long id, Integer status) {
        Setmeal setmeal = setmealMapper.selectById(id);
        setmeal.setStatus(status);
        setmealMapper.update(setmeal);
    }

    @Override
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> setmealList = setmealMapper.list(setmeal);

        return setmealList;
    }

    @Override
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }

    private List<SetmealVO> correlateSetmealDishes(List<SetmealVO> voList) {
        //For every setmeal in page, correlate its dishes.
        if (!voList.isEmpty()) {
            Long setmealIds[] = voList.stream()
                    .map(SetmealVO::getId)
                    .toArray(Long[]::new);
            //Get dishes list of all the setmeals in the page.
            List<SetmealDish> setmealDishesList = setmealDishMapper.getSetmealDishesBySetmealIds(setmealIds);
            //Genereate the list of dishes belonging to the same setmeal.
            Map<Long, List<SetmealDish>> setmealIdToSetmealDishes = new HashMap<>();
            for (SetmealDish setmealDish : setmealDishesList) {
                List<SetmealDish> dishesList =
                        setmealIdToSetmealDishes.getOrDefault(setmealDish.getSetmealId(), new ArrayList<>());
                dishesList.add(setmealDish);
                setmealIdToSetmealDishes.put(setmealDish.getSetmealId(), dishesList);
            }
            //correlate each setmeal with its dish list
            for (SetmealVO vo : voList) {
                vo.setSetmealDishes(setmealIdToSetmealDishes.get(vo.getId()));
            }
        }
        return voList;
    }
}
