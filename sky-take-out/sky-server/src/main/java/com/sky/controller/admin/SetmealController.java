package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Api(tags = "Setmeal related interfaces.")
@RequestMapping("/admin/setmeal")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;


    @ApiOperation("Add a setmeal")
    @PostMapping
    public Result addSetmeal(@RequestBody SetmealDTO setmealDTO){
        setmealService.addSetmeal(setmealDTO);
        return Result.success();
    }

    @ApiOperation("Setmeal page query")
    @GetMapping("/page")
    public Result<PageResult<SetmealVO>> page(SetmealPageQueryDTO pageQueryDTO){
        PageResult<SetmealVO> result = setmealService.page(pageQueryDTO);
        return Result.success(result);
    }

    @ApiOperation("Delete one or multiple setmeals")
    @DeleteMapping
    public Result deleteSetmeals(@RequestParam List<Long> ids){
        setmealService.deleteSetmeals(ids);
        return Result.success();
    }

    @ApiOperation("Get a setmeal's info")
    @GetMapping("/{id}")
    public Result<SetmealVO> getSetmealVO(@PathVariable Long id){
        SetmealVO setmealVO = setmealService.getSetmealVO(id);
        return Result.success(setmealVO);
    }
    @ApiOperation("Update a setmeal")
    @PutMapping
    public Result updateSetmeal(@RequestBody SetmealDTO setmealDTO){
        setmealService.updateSetmeal(setmealDTO);
        return Result.success();
    }

    @ApiOperation("Toggle setmeal status")
    @PostMapping("/status/{status}")
    public Result toggleSetmealStatus(@RequestParam Long id, @PathVariable Integer status){
        setmealService.toggleStatus(id, status);
        return Result.success();
    }
}
