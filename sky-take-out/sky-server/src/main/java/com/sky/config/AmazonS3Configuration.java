package com.sky.config;

import com.sky.properties.AmazonS3Properties;
import com.sky.utils.AmazonS3Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for creating AmazonS3Util object
 */
@Slf4j
@Configuration
public class AmazonS3Configuration {

    @Bean
    @ConditionalOnMissingBean
    public AmazonS3Util amazonS3Util(AmazonS3Properties amazonS3Properties){
        log.info("Start creating AmazonS3Util object...{}", amazonS3Properties);
        return new AmazonS3Util(amazonS3Properties.getAwsAccessKeyId(),
                amazonS3Properties.getAwsSecretAccessKey(),
                amazonS3Properties.getBucketName(),
                amazonS3Properties.getAccessPoint());
    }
}
