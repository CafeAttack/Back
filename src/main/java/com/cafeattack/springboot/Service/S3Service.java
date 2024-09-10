package com.cafeattack.springboot.Service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class S3Service {
    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public S3Service(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    public String[] uploadPics(MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return new String[0];
        }

        String[] imageUrls = new String[files.length];
        int index = 0;

        for (MultipartFile file : files) {
            try {
                String fileName = UUID.randomUUID().toString() + "-" + file.getOriginalFilename();
                // 파일 메타데이터 설정
                ObjectMetadata objectMetadata = new ObjectMetadata();
                objectMetadata.setContentType(file.getContentType());
                objectMetadata.setContentLength(file.getSize());

                // S3에 파일 업로드
                amazonS3.putObject(new PutObjectRequest(bucket, fileName, file.getInputStream(), objectMetadata)
                        .withCannedAcl(CannedAccessControlList.PublicRead));

                // S3 URL 생성
                imageUrls[index++] = amazonS3.getUrl(bucket, fileName).toString();
            } catch (IOException e) {
                throw new RuntimeException("파일 업로드 실패", e);
            }
        }
        return imageUrls;
    }
}

