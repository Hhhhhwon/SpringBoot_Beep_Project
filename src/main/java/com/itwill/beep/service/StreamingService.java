package com.itwill.beep.service;

import com.itwill.beep.domain.StreamingInfo;
import com.itwill.beep.domain.StreamingInfoRepository;
import com.itwill.beep.domain.StreamingRecord;
import com.itwill.beep.domain.StreamingRecordRepository;
import com.itwill.beep.domain.UserAccountEntity;
import com.itwill.beep.domain.UserAccountRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class StreamingService {

    private final StreamingRecordRepository streamingRecordRepository;
    private final StreamingInfoRepository streamingInfoRepository;
    private final UserAccountRepository userAccountRepository;

    public void recordStartTime(String streamKey) {
        StreamingRecord streamingRecord = new StreamingRecord();
        streamingRecord.updateStreamingKey(streamKey);
        streamingRecord.updateStreamingStartTime(LocalDateTime.now());
        streamingRecordRepository.save(streamingRecord);
    }

    public List<StreamingRecord> getAllStreamRecords() {
        List<StreamingRecord> records = streamingRecordRepository.findAll();

        return records;
    }

    @Transactional
    public void saveStreamingInfo(Long userId, String streamKey, String streamingUrl) {
        StreamingInfo streamingInfo = new StreamingInfo();
        streamingInfo.updateStreamKey(streamKey);
        streamingInfo.updateStreamingUrl(streamingUrl);

        // User 엔티티를 조회하여 StreamingInfo와 연결
        // 예시 코드이므로 User 조회 로직은 생략
        UserAccountEntity user = userAccountRepository.findById(userId).orElseThrow();
        streamingInfo.updateUser(user);

        streamingInfoRepository.save(streamingInfo);
    }
}