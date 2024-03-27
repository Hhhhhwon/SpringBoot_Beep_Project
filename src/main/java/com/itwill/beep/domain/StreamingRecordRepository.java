package com.itwill.beep.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StreamingRecordRepository extends JpaRepository<StreamingRecord, Long> {
    // 여기에 추가적인 쿼리 메서드를 정의할 수 있습니다.
    // 예를 들어, 특정 스트림 키에 대한 레코드를 찾는 메서드 등을 추가할 수 있습니다.
}