package com.itwill.beep.service;

import org.springframework.stereotype.Service;

import com.itwill.beep.domain.BannedUserEntity;
import com.itwill.beep.domain.BannedUserRepository;
import com.itwill.beep.domain.ChannelEntity;
import com.itwill.beep.domain.UserAccountEntity;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BannedUserService {

    private final BannedUserRepository banDao;
    
    public void ban(BannedUserEntity bannedUser) {
        
        banDao.save(bannedUser);
    }
    
    public boolean isBan(UserAccountEntity user, ChannelEntity channel) {
        
        boolean check = banDao.existsByUserIdAndChannelId(user, channel);
        
        return check;
    }
}
