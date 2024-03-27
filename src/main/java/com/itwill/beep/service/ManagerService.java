package com.itwill.beep.service;

import org.springframework.stereotype.Service;

import com.itwill.beep.domain.ChannelEntity;
import com.itwill.beep.domain.ManagerEntity;
import com.itwill.beep.domain.ManagerRepository;
import com.itwill.beep.domain.UserAccountEntity;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ManagerService {

    private final ManagerRepository managerDao;
    
    public boolean isManager(UserAccountEntity user, ChannelEntity channel) {
        
        boolean managerCheck = managerDao.existsByUserIdAndChannelId(user, channel);
        
        return managerCheck;
    }

    public void manager(ManagerEntity manager) {
        managerDao.save(manager);
    }

    @Transactional
    public void managerRevoke(ManagerEntity manager) {
        UserAccountEntity userId = manager.getUserId();
        ChannelEntity channelId = manager.getChannelId();
        
        managerDao.deleteByUserIdAndChannelId(userId, channelId);
        
    }

    
}
