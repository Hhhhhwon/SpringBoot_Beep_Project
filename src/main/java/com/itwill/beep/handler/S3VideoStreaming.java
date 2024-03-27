package com.itwill.beep.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.event.S3EventNotification;
import com.amazonaws.services.s3.event.S3EventNotification.S3EventNotificationRecord;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class S3VideoStreaming implements RequestHandler<S3EventNotification, String>{

    private AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
    
    @Override
    public String handleRequest(S3EventNotification event, Context context) {
        for (S3EventNotificationRecord record : event.getRecords()) {
            String bucketName = record.getS3().getBucket().getName();
            String objectKey = record.getS3().getObject().getKey();
            
            // m3u8 파일 처리
            if (objectKey.endsWith(".m3u8")) {
                S3Object object = s3Client.getObject(bucketName, objectKey);
                InputStream inputStream = object.getObjectContent();
            }

            // ts 파일 처리
            if (objectKey.endsWith(".ts")) {
                S3Object object = s3Client.getObject(bucketName, objectKey);
                InputStream inputStream = object.getObjectContent();
            }
        }

        return "OK";
       
        
    }
    

}
