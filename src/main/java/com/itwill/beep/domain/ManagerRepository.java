package com.itwill.beep.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ManagerRepository extends JpaRepository<ManagerEntity, Long>{

    boolean existsByUserIdAndChannelId(UserAccountEntity user, ChannelEntity channel);

    void deleteByUserIdAndChannelId(UserAccountEntity userId, ChannelEntity channelId);

}
