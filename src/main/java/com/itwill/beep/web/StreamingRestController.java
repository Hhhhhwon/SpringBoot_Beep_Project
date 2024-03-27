package com.itwill.beep.web;

import com.itwill.beep.domain.StreamingRecord;
import com.itwill.beep.service.StreamingService;
import com.itwill.beep.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/stream")
public class StreamingRestController {

    private final StreamingService streamingService;
    private final UserService userService;

    @PostMapping("/start")
    public ResponseEntity<?> startStream(HttpServletRequest request) {
        log.info("request: {}", request);
        String streamingKey = request.getParameter("streamingKey");
        if (streamingKey == null) {
            // streamingKey가 없는 경우, 적절한 응답 반환
            return ResponseEntity.badRequest().body("streamingKey is required");
        }
        log.info("startStream = {}", streamingKey);
        streamingService.recordStartTime(streamingKey);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/records")
    public ResponseEntity<List<StreamingRecord>> getAllStreamRecords() {
        List<StreamingRecord> records = streamingService.getAllStreamRecords();
        return ResponseEntity.ok(records);
    }

    @PostMapping("/validate-stream-key")
    public ResponseEntity<Void> validateStreamKey(@RequestParam("key") String key) {
        log.info("validateStreamKey()");
        log.info("key = {}", key);
        boolean isValid = userService.validateStreamKey(key);

        if (isValid) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

}
