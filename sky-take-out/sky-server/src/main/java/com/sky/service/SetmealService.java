package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.SetmealVO;
import org.springframework.stereotype.Service;

import java.util.List;


public interface SetmealService {
    void addSetmeal(SetmealDTO setmealDTO);

    PageResult<SetmealVO> page(SetmealPageQueryDTO pageQueryDTO);

    void deleteSetmeals(List<Long> ids);

    SetmealVO getSetmealVO(Long id);

    void updateSetmeal(SetmealDTO setmealDTO);

    void toggleStatus(Long id, Integer status);
}
