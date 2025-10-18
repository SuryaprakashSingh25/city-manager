package com.project.media_service.infrastructure.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    public String generatePresignedUrl(String key, int expiresInMinutes) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(expiresInMinutes))
                .getObjectRequest(getObjectRequest)
                .build();

        URL url = s3Presigner.presignGetObject(presignRequest).url();
        return url.toString();
    }

    public String uploadFile(MultipartFile file, String keyPrefix) {
        String key = keyPrefix + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));
            return key;
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to S3", e);
        }
    }


    public void deleteFile(String key){
        try{
            DeleteObjectRequest deleteObjectRequest= DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            s3Client.deleteObject(deleteObjectRequest);
        } catch (S3Exception e){
            throw new RuntimeException("Failed to delete file from S3",e);
        }
    }

    public byte[] downloadFile(String key){
        try{
            GetObjectRequest getObjectRequest= GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            ResponseBytes<GetObjectResponse> response=s3Client.getObjectAsBytes(getObjectRequest);

            return response.asByteArray();
        } catch (S3Exception e){
            throw new RuntimeException("Failed to download file from S3", e);
        }
    }

}
