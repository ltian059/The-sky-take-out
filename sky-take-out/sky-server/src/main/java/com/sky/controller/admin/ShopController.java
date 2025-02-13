package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("adminShopController")
@Api(tags = "Shop related APIs")
@Slf4j
@RequestMapping("/admin/shop")
public class ShopController {

    @Autowired
    private RedisTemplate redisTemplate;

    private static final String SHOP_STATUS = "SHOP_STATUS";

    @PutMapping("/{status}")
    @ApiOperation("Update shop status")
    public Result setStatus(@PathVariable Integer status) {
        log.info("Update shop status: {}", status == 1 ? "open" : "close");
        redisTemplate.opsForValue().set("SHOP_STATUS", status);
        return Result.success();
    }

    @GetMapping("/status")
    @ApiOperation("Get shop status")
    public Result<Integer> getStatus() {
        Integer status = (Integer) redisTemplate.opsForValue().get(SHOP_STATUS);
        return Result.success(status);
    }
}
