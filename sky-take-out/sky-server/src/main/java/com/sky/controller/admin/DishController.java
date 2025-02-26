package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@Api(tags = "Dish related interfaces")
@RequestMapping("/admin/dish")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping
    @ApiOperation("Add new dish")
    public Result addDish(@RequestBody DishDTO dishDTO){
        log.info("Add new Dish:{}", dishDTO);
        dishService.addDishWithFlavor(dishDTO);
        //Delete the cache
        cleanRedisCache("dish_"+ dishDTO.getCategoryId());
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
        //Delete the cache: all dishes
        cleanRedisCache("dish_*");
        return Result.success();
    }


    @PostMapping("/status/{status}")
    @ApiOperation("Enable or Disable dish sale status")
    public Result toggleDishStatus(@RequestParam Long id, @PathVariable Integer status){
        Integer update = dishService.toggleDishStatus(id, status);
        //Delete the cache
       cleanRedisCache("dish_*");
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
        if (update > 0) {
            //Delete the cache
            //If the dish is updated with a new category, then we need to delete the cache of the old category and the new category.
            //But here we just delete all the cache.
            cleanRedisCache("dish_*");
            return Result.success();
        }
        else throw new RuntimeException("Update dish failed...");
    }

    @GetMapping("/list")
    @ApiOperation("Get dishes list by categoryId")
    public Result<List<Dish>> listDishByCategoryId(@RequestParam Long categoryId){
        List<Dish> dishes = dishService.listDishByCategoryId(categoryId);
        return Result.success(dishes);
    }


    private void cleanRedisCache(String pattern) {
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }
}
