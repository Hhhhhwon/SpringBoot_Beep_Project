package com.itwill.beep.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.itwill.beep.domain.ChannelEntity;
import com.itwill.beep.domain.ChannelRepository;
import com.itwill.beep.domain.FollowEntity;
import com.itwill.beep.domain.FollowRepository;
import com.itwill.beep.domain.UserAccountEntity;
import com.itwill.beep.dto.ChannelRequestDto;
import com.itwill.beep.dto.StreamingOnDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChannelService {

    private final ChannelRepository channelDao;
    private final FollowRepository followRepository;

    public ChannelEntity findChannelByUserAccount(UserAccountEntity userAcconut) {

        ChannelEntity channel = channelDao.findByChannelUserAccountEntity(userAcconut);

        return channel;
    }

    public ChannelEntity findChannelById(Long channelId) {
        ChannelEntity channel = channelDao.findByChannelId(channelId);
        return channel;
    }

    public void save(ChannelEntity channel) {
        channelDao.save(channel);
    }

    public List<ChannelEntity> findAllChannel() {

        List<ChannelEntity> channelList = channelDao.findAll();

        return channelList;
    }


    @Transactional
    public void update(StreamingOnDto dto) {
        // 뭐가 오류가 생겼는데 하드코딩하면 해결 될 것 같아서 하드 코딩함
        Long channelId = dto.getChannelId();
        String title = dto.getTitle();
        String content = dto.getContent();
        /* 카테고리아이디 추가 */
        Long categoryId = dto.getCategoryId();
        /* 카테고리 없이 방송하는 동작을 막기위해 null일 시에 JustChatting 번호를 부여 */
        if (categoryId == null) {
            categoryId = 100_000_000L;
        }

        channelDao.update(channelId, title, content, categoryId);

    }

    @Transactional
    public UserAccountEntity findUserByChannelId(Long channelId) {

        UserAccountEntity streamer = channelDao.findChannelUserAccountEntityByChannelId(channelId);

        return streamer;
    }

    /* 카테고리 아이디로 채널 검색하기 */
    public List<ChannelEntity> findByCategoryCategoryId(Long categoryId) {
        log.info("findByCategoryCategoryId(categoryId={})", categoryId);

        List<ChannelEntity> list = channelDao.findByCategoryEntityOfChannelCategoryId(categoryId);

        return list;
    }

    /* 팔로우 채널 불러오기(시청자 수 높은순) */
    public List<ChannelRequestDto> getChannelListForFollowings(UserAccountEntity followerEntity) {
        log.info("getChannelListForFollowings(followerEntity={})", followerEntity);

        List<FollowEntity> followList = followRepository.findByFollowerUserAccount(followerEntity);
        List<ChannelRequestDto> data = new ArrayList<>();
        for (FollowEntity entity : followList) {
            ChannelEntity channelEntity =
                    channelDao.findByChannelUserAccountEntity(entity.getFollowingUserAccount());

            ChannelRequestDto channelRequestDto = ChannelRequestDto.builder()
                    .channelUserAccountEntityChannelName(
                            channelEntity.getChannelUserAccountEntity().getUserNickname())
                    .channelTitle(channelEntity.getChannelTitle())
                    .channelProfileImg(channelEntity.getChannelProfileImg())
                    .categoryEntityOfChannelCategoryName(
                            channelEntity.getCategoryEntityOfChannel() != null
                                    ? channelEntity.getCategoryEntityOfChannel().getCategoryName()
                                    : "Just chatting")
                    .channelViewerCount(channelEntity.getChannelViewerCount())
                    .streamingState(channelEntity.getStreamingStateSet().toString()).build();
            data.add(channelRequestDto);

        }
        data.sort(Comparator.comparingLong(ChannelRequestDto::getChannelViewerCount));
        Collections.reverse(data);

        return data;
    }

    /* 추천 채널 불러오기(시청자 수 높은 순) */
    public List<ChannelRequestDto> getPopularChannelList() {
        log.info("getPopularChannelList()");

        // 채널을 조회하여 가져옴
        List<ChannelEntity> channels = channelDao.findByCategoryEntityOfChannelIsNotNull();

        // 시청자 수를 기준으로 채널을 정렬
        channels.sort(Comparator.comparingLong(ChannelEntity::getChannelViewerCount));

        // 역순으로 정렬
        Collections.reverse(channels);

        // DTO로 변환
        List<ChannelRequestDto> data = new ArrayList<>();
        for (ChannelEntity channel : channels) {
            if (channel.getStreamingStateSet().toString().contains("ON")) {
                ChannelRequestDto channelRequestDto = ChannelRequestDto.builder()
                        .channelUserAccountEntityChannelName(
                                channel.getChannelUserAccountEntity().getUserNickname())
                        .channelTitle(channel.getChannelTitle())
                        .channelProfileImg(channel.getChannelProfileImg())
                        .categoryEntityOfChannelCategoryName(
                                channel.getCategoryEntityOfChannel().getCategoryName())
                        .channelViewerCount(channel.getChannelViewerCount())
                        .streamingState(channel.getStreamingStateSet().toString()).build();
                data.add(channelRequestDto);
            }
        }

        return data;
    }

    @Transactional
    public void setViewers(Long channelId, Long viewers) {

        channelDao.updateViewers(channelId, viewers);

    }

    /* 채널 제목으로 찾기 */
    public List<ChannelEntity> findChannelByChannelTitle(String keyword) {

        return channelDao.findByChannelTitleContainingIgnoreCase(keyword);
    }

    /* 채널을 통해서 유저 검색하기(채널의 데이터를 얻기 위해) */
    public List<ChannelEntity> findChannelBychannelUserAccountEntity(String keyword) {

        return channelDao.findByChannelUserAccountEntityUserNicknameContainingIgnoreCase(keyword);
    }

}
