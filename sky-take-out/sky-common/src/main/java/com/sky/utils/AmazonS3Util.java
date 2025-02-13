package com.sky.utils;


import com.amazonaws.HttpMethod;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.sky.constant.CommonConstant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Data
@Slf4j
@AllArgsConstructor
public class AmazonS3Util {

//    private String awsAccessKeyId;
//    private String awsSecretAccessKey;
    private String bucketName;

    @Autowired
    private RedisTemplate redisTemplate;

    public AmazonS3Util(String bucketName){
        this.bucketName = bucketName;
    }
    public AmazonS3 createS3Client(){
        //BasicAWSCredentials awsCredentials = new BasicAWSCredentials(awsAccessKeyId, awsSecretAccessKey);

        return AmazonS3ClientBuilder.standard()
                .withCredentials(new ProfileCredentialsProvider("LiTian"))
                .withRegion(Regions.CA_CENTRAL_1)
                .build();

    }

    public String generatePresignedUrl(String objectKey, int expirationMinutes){
        Date exp = new Date();
        exp.setTime(System.currentTimeMillis() + expirationMinutes * 60 * 1000);

        GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucketName, objectKey)
                .withMethod(HttpMethod.GET)
                .withExpiration(exp);
        URL url = createS3Client().generatePresignedUrl(generatePresignedUrlRequest);
        redisTemplate.opsForValue().setIfAbsent(objectKey, url, expirationMinutes, TimeUnit.MINUTES);
        return url.toString();
    }

    public void uploadFile(byte[] bytes, String objectName){
        AmazonS3 s3Client = createS3Client();

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(bytes.length);

        s3Client.putObject(bucketName, objectName, new ByteArrayInputStream(bytes), metadata);
        //return generatePresignedUrl(objectName, CommonConstant.IMAGE_EXPIRATION_MINUTES);
    }



}
