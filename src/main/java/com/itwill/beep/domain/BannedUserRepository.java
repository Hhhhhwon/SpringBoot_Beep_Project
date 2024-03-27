package com.itwill.beep.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BannedUserRepository extends JpaRepository<BannedUserEntity, Long> {

    boolean existsByUserIdAndChannelId(UserAccountEntity user, ChannelEntity channel);

}
