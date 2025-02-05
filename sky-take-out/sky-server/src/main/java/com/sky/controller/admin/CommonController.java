package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AmazonS3Util;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Api("Common interface")
@RequestMapping("/admin/common")
@RestController
public class CommonController {

    @Autowired
    private AmazonS3Util amazonS3Util;

    /**
     * Image file upload
     * @param file
     * @return
     */
    @PostMapping("/upload")
    @ApiOperation("Image File Upload")
    public Result<String> fileUpload(@RequestBody MultipartFile file){
        log.info("Uploading image files:{}", file);
        try {
            String originalFilename = file.getOriginalFilename();
            //Get original file suffix
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String objectName = UUID.randomUUID().toString() + extension;

            String url = amazonS3Util.uploadFile(file.getBytes(), objectName);
            log.info("Presigned url:{}", url);
            return Result.success(url);

        } catch (IOException e) {
            log.error("File upload failed:{}", e.getMessage());
            e.printStackTrace();
        }
        return Result.error(MessageConstant.UPLOAD_FAILED);
    }



}
