package com.izj.knowledge.service.base.aws;

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AwsConfig {

    @Bean
    public AWSCredentialsProvider awsCredentialsProvider() {
        // When running on EC2, credentials is assigned to the instance.
        // On Lambda, credentials is in environment variables.
        // Otherwise, detect user directory. (eg. ~/.aws)
        // Check http://docs.aws.amazon.com/ja_jp/sdk-for-java/v1/developer-guide/credentials.html
        return new DefaultAWSCredentialsProviderChain();
    }

    @Bean
    @Autowired
    public AwsProperties awsProperties() {
        final String env = "";
        String fileName = null;
        switch (env) {
        case "EVAL":
        case "TEST":
        case "PROD":
            fileName = StringUtils.join("aws-", StringUtils.lowerCase(env), ".properties");
            break;
        case "LOCAL":
        case "DEV":
        default:
            fileName = "aws.properties";
            break;
        }

        Properties props = new Properties();
        try {
            props.load(new ClassPathResource("aws/" + fileName).getInputStream());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load aws.properties!", e);
        }

        AwsProperties properties = AwsProperties
            .builder()
            .build();
        log.info("Loaded AWS properties => {}", properties);
        return properties;
    }

}
