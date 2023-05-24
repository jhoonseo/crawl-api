package com.costco.crawl.service;

import com.costco.crawl.util.CommonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class S3Service {
    @Value("${s3.bucket.name}")
    private String s3BucketName;
    @Value("${s3.object.directory}")
    private String s3ObjectDirectory;
    @Value("${local.directory}")
    private String localDirectory;
    @Value("${local.daily.directory}")
    private String localDailyDirectory;
    @Value("${local.images.directory}")
    private String localImagesDirectory;
    @Value("${aws.access.key.id}")
    private String awsAccessKeyId;
    @Value("${aws.secret.access.key}")
    private String awsSecretAccessKey;

    private final CommonUtil commonUtil;
    private S3Client s3Client;

    public void buildS3Client() {
        s3Client = S3Client.builder()
                .region(Region.AP_NORTHEAST_2)
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(awsAccessKeyId, awsSecretAccessKey)))
                .build();
    }

    public void uploadDirectoryFiles(String formatToday) {
        buildS3Client();
        File directory = new File(String.join("/", localDirectory, "daily", formatToday, "images"));
        if (!directory.exists())
            return;

        File[] files = directory.listFiles();

        if (files == null)
            return;

        Set<File> fileSet = commonUtil.getFilteredFileSet(files);

        for (File file : fileSet) {
            if (file.isFile()) {
                uploadS3(file.getPath(), file.getName());
            }
        }
        s3Client.close();
    }
    public void uploadS3(String filePath, String filename) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(s3BucketName)
                .key(String.join("/", s3ObjectDirectory, filename))
                .acl(ObjectCannedACL.PUBLIC_READ)
                .build();

        PutObjectResponse response = s3Client.putObject(request, Paths.get(filePath));

        log.debug("File uploaded successfully. ETag: {}", response.eTag());
    }

}
