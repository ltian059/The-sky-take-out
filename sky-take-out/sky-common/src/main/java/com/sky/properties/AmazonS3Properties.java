package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "sky.amazons3")
public class AmazonS3Properties {

    private String awsAccessKeyId;
    private String awsSecretAccessKey;
    private String bucketName;
    private String accessPoint;
}
