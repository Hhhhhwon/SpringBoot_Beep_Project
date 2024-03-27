package com.itwill.beep.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.itwill.beep.config.MyS3;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {
    private final S3Client s3Client;
    private final MyS3 my3s;

    public S3Service() {
        this.s3Client = S3Client.builder()
            .credentialsProvider(DefaultCredentialsProvider.create())
            .region(Region.AP_NORTHEAST_2) // AWS 서울 리전
            .build();
        this.my3s = new MyS3();
    }

    public String getM3U8UrlForStreamKey(String bucketName, String streamKey) {
        String keyPattern = streamKey + ".m3u8"; // 파일 이름 패턴
        
        // 자격 증명 설정
        AWSCredentialsProvider credentialsProvider = new DefaultAWSCredentialsProviderChain();

        // S3 클라이언트 생성
        AmazonS3 s3Client = AmazonS3Client.builder()
                .withCredentials(credentialsProvider)
                .setRegion(Region.AP_NORTHEAST_2) // AWS 서울 리전
                .build();
        
        
        // S3 버킷에서 스트림 키에 해당하는 객체만 검색
        ListObjectsV2Request listReq = ListObjectsV2Request.builder()
            .bucket(bucketName)
            .prefix(streamKey) // 스트림 키로 시작하는 객체만 검색
            .delimiter("hls/")
            .build();
        log.info("req = {}", listReq);
        
        
        // 스트림 키에 해당하고 .m3u8로 끝나는 객체들 중 가장 최근의 객체를 찾음
        Optional<S3Object> mostRecentM3u8File = s3Client.listObjectsV2(listReq).contents().stream()
            .filter(s3Object -> s3Object.key().endsWith(".m3u8"))
            .max(Comparator.comparing(S3Object::lastModified));
        log.info("mostRecentM3u8File = {}", mostRecentM3u8File);
        
        // 가장 최근의 m3u8 파일의 URL을 생성
        if (mostRecentM3u8File.isPresent()) {
            String url = "https://" + bucketName + ".s3.amazonaws.com/" + mostRecentM3u8File.get().key();
            return url;
        } else {
            return null; // 해당하는 파일이 없다면 null 반환
        }
    }
}
