package com.itwill.beep.domain;


import org.springframework.data.jpa.repository.JpaRepository;

public interface StreamingInfoRepository extends JpaRepository<StreamingInfo, Long> {
}