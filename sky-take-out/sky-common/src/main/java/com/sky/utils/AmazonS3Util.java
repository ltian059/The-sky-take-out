package com.sky.utils;


import com.amazonaws.HttpMethod;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.sky.context.BaseContext;
import com.sky.result.Result;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Slf4j
@AllArgsConstructor
public class AmazonS3Util {

    private String awsAccessKeyId;
    private String awsSecretAccessKey;
    private String bucketName;
    private String accessPoint;
    public AmazonS3 createS3Client(){
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(awsAccessKeyId, awsSecretAccessKey);

        return AmazonS3ClientBuilder.standard()
                .withRegion(Regions.CA_CENTRAL_1)
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .build();

    }

    public String generatePresignedUrl(String objectKey, int expirationMinutes){
        Date exp = new Date();
        exp.setTime(System.currentTimeMillis() + expirationMinutes * 60 * 1000);

        GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucketName, objectKey)
                .withMethod(HttpMethod.GET)
                .withExpiration(exp);
        URL url = createS3Client().generatePresignedUrl(generatePresignedUrlRequest);
        return url.toString();
    }

    public String  uploadFile(byte[] bytes, String objectName){
        AmazonS3 s3Client = createS3Client();

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(bytes.length);

        s3Client.putObject(accessPoint, objectName, new ByteArrayInputStream(bytes), metadata);

        return generatePresignedUrl(objectName, 3600);
    }



}
