package com.athena.data.recevier.athena.data.receiver;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.athena.AthenaClient;

@Configuration
public class AthenaConfig {

    @Bean
    public AthenaClient athenaClient() {
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(
                "AWS Access Key",  // Replace with your AWS Access Key
                "AWS Secret Key"   // Replace with your AWS Secret Key
        );

        return AthenaClient.builder()
                .region(Region.US_EAST_1) // Replace with your region
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .build();
    }
}
