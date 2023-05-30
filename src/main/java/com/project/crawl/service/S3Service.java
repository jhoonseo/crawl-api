package com.project.crawl.service;

import com.project.crawl.util.CommonUtil;
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
import java.nio.file.Paths;
import java.util.Set;


@Service
@Slf4j
@RequiredArgsConstructor
public class S3Service {
    @Value("${s3.bucket.name}")
    private String s3BucketName;
    @Value("${s3.images.object}")
    private String s3ImagesObject;
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

    public S3Client buildS3Client() {
        return S3Client.builder()
                .region(Region.AP_NORTHEAST_2)
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(awsAccessKeyId, awsSecretAccessKey)))
                .build();
    }

    public void uploadDirectoryFilesPublic(String path) {
        File directory = new File(String.join("/", localDirectory, path));
        if (!directory.exists())
            return;

        File[] files = directory.listFiles();

        if (files.length == 0)
            return;

        S3Client s3Client = buildS3Client();

        Set<File> fileSet = commonUtil.getFilteredFileSet(files);

        for (File file : fileSet) {
            String object = String.join("/", s3ImagesObject, file.getName());
            uploadS3Public(s3Client, file.getPath(), object);
        }
        s3Client.close();
    }
    public void uploadS3Public(S3Client s3Client, String filePath, String object) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(s3BucketName)
                .key(object)
                .acl(ObjectCannedACL.PUBLIC_READ)
                .build();

        PutObjectResponse response = s3Client.putObject(request, Paths.get(filePath));

        log.debug("File uploaded successfully. ETag: {}", response.eTag());
    }

    public void uploadS3Private(S3Client s3Client, String filePath, String object) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(s3BucketName)
                .key(object)
                .acl(ObjectCannedACL.PRIVATE)
                .build();

        PutObjectResponse response = s3Client.putObject(request, Paths.get(filePath));

        log.debug("File uploaded successfully. ETag: {}", response.eTag());
    }


}
