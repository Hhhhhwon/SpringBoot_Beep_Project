package com.itwill.beep.service;

import java.io.IOException;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.itwill.beep.domain.ChannelEntity;
import com.itwill.beep.domain.ChannelRepository;
import com.itwill.beep.dto.ChannelImageRequestDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class ImageService {
    private final ChannelRepository channelRepository;
    private final AmazonS3 amazonS3;
    @Value("${AWS_S3_BUCKET}")
    private String bucketName;

    // 이미지를 S3에 업로드하고 이미지의 url을 반환
    @Transactional
    public void uploadImageToS3(ChannelImageRequestDto dto) throws IOException {
        log.info("uploadImageToS3(dto={})", dto);

        String originName = dto.getImgFile().getOriginalFilename(); // 원본 이미지 이름
        String ext = originName.substring(originName.lastIndexOf(".")); // 확장자
        String changedName = changedImageName(originName); // 새로 생성된 이미지 이름
        String folderName = "channel_profile_img/";
        String urlS3 = folderName + changedName;

        ObjectMetadata metadata = new ObjectMetadata(); // 메타데이터
        metadata.setContentType("image/" + ext);
        PutObjectResult putObjectResult = amazonS3.putObject(
                new PutObjectRequest(bucketName, urlS3, dto.getImgFile().getInputStream(), metadata)
                        .withCannedAcl(CannedAccessControlList.PublicRead));
        log.info("metadata={}", putObjectResult.toString());
        ChannelEntity channelEntity = channelRepository.findByChannelId(dto.getChannelId());
        // 데이터베이스에 저장할 이미지가 저장된 주소
        channelEntity.updateChannelProfileImg(amazonS3.getUrl(bucketName, urlS3).toString());
        channelRepository.save(channelEntity);
    }

    private String changedImageName(String originName) { // 이미지 이름 중복 방지를 위해 랜덤으로 생성
        String random = UUID.randomUUID().toString();
        return random + originName;
    }

}
