package com.restaurant.common.utils;

import io.minio.*;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class MinioUtil {

    @Value("${minio.endpoint:http://127.0.0.1:9000}")
    private String endpoint;

    @Value("${minio.access-key:}")
    private String accessKey;

    @Value("${minio.secret-key:}")
    private String secretKey;

    @Value("${minio.bucket:restaurant}")
    private String bucket;

    private MinioClient client;

    @PostConstruct
    public void init() {
        client = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
        ensureBucket();
    }

    private void ensureBucket() {
        try {
            boolean exists = client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                log.info("MinIO bucket created: {}", bucket);
            }
        } catch (Exception e) {
            log.error("Failed to ensure MinIO bucket: {}", bucket, e);
        }
    }

    /**
     * Upload file to MinIO, returns the public URL
     */
    public String upload(MultipartFile file, String folder) {
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String objectName = folder + "/" + UUID.randomUUID().toString() + extension;

        try (InputStream inputStream = file.getInputStream()) {
            client.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            String url = endpoint + "/" + bucket + "/" + objectName;
            log.info("File uploaded to MinIO: {}", url);
            return url;
        } catch (Exception e) {
            log.error("MinIO upload failed", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * Delete file from MinIO by URL
     */
    public void delete(String fileUrl) {
        try {
            // URL format: http://host:port/bucket/objectName
            String prefix = endpoint + "/" + bucket + "/";
            if (fileUrl != null && fileUrl.startsWith(prefix)) {
                String objectName = fileUrl.substring(prefix.length());
                client.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(bucket)
                                .object(objectName)
                                .build()
                );
                log.info("MinIO file deleted: {}", objectName);
            }
        } catch (Exception e) {
            log.error("MinIO delete failed: {}", fileUrl, e);
        }
    }
}
