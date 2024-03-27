package com.itwill.beep.web;

import com.itwill.beep.service.S3Service;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class S3Controller {
    private final S3Service s3Service = new S3Service();

    @GetMapping("/stream")
    public String getStreamUrl(Model model, @RequestParam String userStreamingKey) {
        // S3 버킷 이름과 사용자의 스트리밍 키를 기반으로 m3u8 URL 생성
        String bucketName = "your-s3-bucket-name";
        String m3u8Url = s3Service.getM3U8UrlForStreamKey(bucketName, userStreamingKey);

        if (m3u8Url != null) {
            model.addAttribute("streamingUrl", m3u8Url);
            return "streamPage"; // 스트리밍 페이지 반환
        } else {
            return "errorPage"; // 오류 페이지 반환
        }
    }
}