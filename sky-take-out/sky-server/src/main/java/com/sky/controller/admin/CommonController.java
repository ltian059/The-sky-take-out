package com.sky.controller.admin;

import com.sky.constant.CommonConstant;
import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AmazonS3Util;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Api("Common interface")
@RequestMapping("/admin/common")
@RestController
public class CommonController {

    @Autowired
    private AmazonS3Util amazonS3Util;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * Image file upload
     * @param file
     * @return
     */
    @PostMapping("/upload")
    @ApiOperation("Image File Upload")
    public Result<String> fileUpload(@RequestBody MultipartFile file){
        log.info("Uploading image files:{}", file);
        String originalFilename = file.getOriginalFilename();
        //Get original file suffix
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String objectName = UUID.randomUUID().toString() + extension;

        String url = null;
        try {
            amazonS3Util.uploadFile(file.getBytes(), objectName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        url = CommonConstant.IMAGE_GET_URL + objectName;
        log.info("Presigned url:{}", url);
        return Result.success(url);

    }

    @GetMapping("/image/{objectName}")
    @ApiOperation("Get the image url by objectName")
    public void getImageUrl(@PathVariable String objectName, HttpServletResponse response) throws IOException {
        log.info("Get the image url by objectName:{}", objectName);
        Object redisValue = redisTemplate.opsForValue().get(objectName);
        String presignedUrl;
        if (redisValue == null){
            presignedUrl = amazonS3Util.generatePresignedUrl(objectName, CommonConstant.IMAGE_EXPIRATION_MINUTES);
        }else{
            presignedUrl = redisValue.toString();
        }
        //Use httpclient to send a get request to the presigned url
        try(CloseableHttpClient httpClient = HttpClients.createDefault()){
            HttpGet httpGet = new HttpGet(presignedUrl);
            CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
            //Get the entity of the response and write it to the output stream of the servlet response
            httpResponse.getEntity().writeTo(response.getOutputStream());
            httpResponse.close();
        }

//        URL url = new URL(presignedUrl);
//        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//        conn.setRequestMethod("GET");
        // 设置响应的 Content-Type 为 S3 返回的图片类型（如 image/png）
        //response.setContentType(conn.getContentType());

//        try(InputStream in = conn.getInputStream();
//            OutputStream out = response.getOutputStream()){
//            byte[] buffer = new byte[4096];
//            int len;
//            while((len = in.read(buffer)) != -1){
//                out.write(buffer, 0, len);
//            }
//        }

    }


}
