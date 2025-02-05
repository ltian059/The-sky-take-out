package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Api(tags = "Dish related interfaces")
@RequestMapping("/admin/dish")
@Slf4j
public class DishController {

    @Autowired
    DishService dishService;

    @PostMapping
    @ApiOperation("Add new dish")
    public Result addDish(@RequestBody DishDTO dishDTO){
        log.info("Add new Dish:{}", dishDTO);
        dishService.addDishWithFlavor(dishDTO);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("page query of dish page")
    public Result<PageResult<DishVO>> page(DishPageQueryDTO dto){
        log.info("dish page query:{}", dto);
        PageResult<DishVO> pageResult = dishService.pageQuery(dto);
        return Result.success(pageResult);
    }

    @DeleteMapping
    @ApiOperation("Delete one or multiple dishes")
    public Result deleteInBatch(@RequestParam List<Long> ids){
        log.info("Delete one or multiple dishes:{}", ids);
        Integer count = dishService.deleteInBatch(ids.toArray(new Long[ids.size()]));
        return Result.success();
    }

    @PostMapping("/status/{status}")
    @ApiOperation("Enable or Disable dish sale status")
    public Result toggleDishStatus(@RequestParam Long id, @PathVariable Integer status){
        Integer update = dishService.toggleDishStatus(id, status);
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("Query a dish by id")
    public Result<DishVO> getDishById(@PathVariable Long id){
        DishVO dishVO = dishService.getDishWithFlavorById(id);
        return Result.success(dishVO);
    }

    @PutMapping
    @ApiOperation("Modify a dish")
    public Result updateDish(@RequestBody DishDTO dishDTO){
        Integer update = dishService.updateDish(dishDTO);
        if (update > 0)
            return Result.success();
        else throw new RuntimeException("Update dish failed...");
    }

}
