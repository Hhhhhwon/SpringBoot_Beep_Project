package com.itwill.beep.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.api.igdb.apicalypse.APICalypse;
import com.api.igdb.apicalypse.Sort;
import com.api.igdb.exceptions.RequestException;
import com.api.igdb.request.IGDBWrapper;
import com.api.igdb.request.ProtoRequestKt;
import com.api.igdb.request.TwitchAuthenticator;
import com.api.igdb.utils.TwitchToken;
import com.itwill.beep.domain.CategoryEntity;
import com.itwill.beep.domain.CategoryRepository;
import com.itwill.beep.domain.ChannelEntity;
import com.itwill.beep.domain.ChannelRepository;
import com.itwill.beep.domain.GenreType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import proto.Cover;

@Slf4j
@RequiredArgsConstructor
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ChannelRepository channelRepository;
    private final String defaultStaticUrl = "/images/no_cover_image.jpg";

    // Twitch API 클라이언트 정보
    @Value("${spring.security.oauth2.client.registration.twitch.client-id}")
    private String clientId;
    @Value("${spring.security.oauth2.client.registration.twitch.client-secret}")
    private String clientSecret;

    /**
     * 시청자 수가 높은 순서대로 카테고리를 조회하여 반환하는 메서드입니다.
     * 
     * @return 시청자 수가 높은 순서대로 정렬된 카테고리 목록
     */
    public List<CategoryEntity> findByTotalViewers() {
        log.info("findByTotalViewers()");

        // 중복된 카테고리를 제거하기 위해 Set을 사용
        Set<CategoryEntity> categoriesWithTotalViewers = new HashSet<>();

        // 채널을 조회하여 가져옴
        List<ChannelEntity> channels = channelRepository.findByCategoryEntityOfChannelIsNotNull();

        // 각 채널의 카테고리를 가져와서 시청자 수를 업데이트하고,
        // 시청자 수가 있는 카테고리만 Set에 추가
        for (ChannelEntity channel : channels) {
            CategoryEntity category = channel.getCategoryEntityOfChannel();
            Long totalViewerCount =
                    channelRepository.getTotalViewerCountByCategoryId(category.getCategoryId());
            if (totalViewerCount != null
                    && channel.getStreamingStateSet().toString().contains("ON")) {
                // totalViewerCount가 null 아닐 때, 방송 중일때만 불러오기
                category.updateCategoryTotalView(totalViewerCount);
                categoriesWithTotalViewers.add(category);
            }
        }

        // Set을 List로 변환하여 List에 저장
        List<CategoryEntity> sortedCategories = new ArrayList<>(categoriesWithTotalViewers);

        // 시청자 수가 높은 순서대로 정렬
        sortedCategories
                .sort(Comparator.comparingLong(CategoryEntity::getCategoryTotalView).reversed());

        // 정렬된 리스트를 반환
        return sortedCategories;
    }

    /**
     * 인기도가 높은 순서대로 카테고리를 조회하여 반환하는 메서드입니다.
     * 
     * @return 인기도가 높은 순서대로 정렬된 카테고리 목록
     */
    public List<CategoryEntity> findByPopulars() {
        log.info("findByChannelCountSortedByTotalViewers()");

        // 중복된 카테고리를 제거하기 위해 Set을 사용
        Set<CategoryEntity> categoriesWithTotalViewers = new HashSet<>();

        // 채널을 조회하여 가져옴
        List<ChannelEntity> channels = channelRepository.findByCategoryEntityOfChannelIsNotNull();

        // 각 채널의 카테고리를 가져와서 시청자 수를 업데이트하고,
        // 시청자 수가 있는 카테고리만 Set에 추가
        for (ChannelEntity channel : channels) {
            CategoryEntity category = channel.getCategoryEntityOfChannel();
            Long totalViewerCount =
                    channelRepository.getTotalViewerCountByCategoryId(category.getCategoryId());
            if (totalViewerCount != null
                    && channel.getStreamingStateSet().toString().contains("ON")) {
                // totalViewerCount가 null 아닐 때, 방송 중일때만 불러오기
                category.updateCategoryTotalView(totalViewerCount);
                categoriesWithTotalViewers.add(category);
            }
        }

        List<CategoryEntity> sortedCategories = new ArrayList<>(categoriesWithTotalViewers);
        sortedCategories.sort(Comparator.comparingInt(
                category -> channelRepository.countByCategoryEntityOfChannel(category)));

        // 역순으로 정렬
        Collections.reverse(sortedCategories);

        return sortedCategories;
    }

    public CategoryEntity findByCategoryIdIs(Long categoryId) {
        log.info("findByCategoryId(categoryId={})", categoryId);

        CategoryEntity category = categoryRepository.findByCategoryId(categoryId);
        category.updateCategoryTotalView(
                channelRepository.getTotalViewerCountByCategoryId(categoryId));

        return category;
    }

    /* 카테고리를 필요시 저장할 때만 사용하겠습니다. */
    @Transactional
    public List<CategoryEntity> findAllAndSaveCategories() {
        log.info("findAllAndSaveCategories()");

        List<CategoryEntity> savedCategories = new ArrayList<>();

        // Twitch API 토큰 요청
        setTwitchAuthCredentials();

        // IGDB API를 통해 신규 게임 목록
        APICalypse gamesQuery =
                new APICalypse().fields("*").limit(20).sort("release_dates", Sort.DESCENDING);

        try {
            var games = ProtoRequestKt.games(IGDBWrapper.INSTANCE, gamesQuery);
            for (var game : games) {
                // 각 게임에 대한 커버 정보 가져오기
                Cover cover = getCoverForGame(game.getId());
                String imageUrl =
                        (cover != null) ? cover.getUrl().replace("/t_thumb/", "/t_cover_big/")
                                : defaultStaticUrl;

                // 해당 카테고리의 채널들의 뷰어 수를 합산
                Long totalViewerCount =
                        channelRepository.getTotalViewerCountByCategoryId(game.getId());

                // CategoryEntity 엔터티 생성 및 저장
                CategoryEntity category = CategoryEntity.builder().categoryId(game.getId())
                        .categoryName(game.getName()).categoryImageUrl(imageUrl)
                        .categoryTotalView(totalViewerCount).build();

                savedCategories.add(categoryRepository.save(category));
            }
        } catch (RequestException e) {
            log.error("RequestException");
            e.printStackTrace();
        }

        return savedCategories;
    }

    public List<GenreType> findGenre(Long categoryId) {
        log.info("findGenre(categoryId={})", categoryId);

        setTwitchAuthCredentials();

        List<GenreType> list = new ArrayList<>();

        String c = categoryRepository.findByCategoryId(categoryId).getCategoryName();

        APICalypse gameFindQuery = new APICalypse().search(c).fields("*").limit(1);
        try {
            var games = ProtoRequestKt.games(IGDBWrapper.INSTANCE, gameFindQuery);

            // 첫 번째 게임만 가져오기
            if (!games.isEmpty()) {
                var game = games.get(0);

                // 첫 번째 게임의 모든 장르 가져오기
                for (int i = 0; i < game.getGenresCount(); i++) {
                    String genreStr = game.getGenres(i).toString();
                    // "id =" 문자열을 제거하고, 숫자 값으로 변환
                    int genreId = Integer.parseInt(genreStr.replaceAll("[^\\d]", ""));
                    GenreType genreType = GenreType.getByValue(genreId);
                    list.add(genreType);
                }
            }
        } catch (RequestException e) {
            log.error("RequestException");
            e.printStackTrace();
        }

        return list;
    }


    /**
     * IGDB API를 통해 키워드로 게임을 검색하고 결과를 카테고리로 저장합니다.
     *
     * @param keyword 검색 키워드
     * @return 검색된 카테고리 목록
     * @throws RequestException
     */
    @Transactional
    public List<CategoryEntity> searchGames(String keyword) {
        log.info("searchGames(keyword={})", keyword);

        List<CategoryEntity> foundGames = new ArrayList<>();

        // Twitch API 토큰 요청
        setTwitchAuthCredentials();

        // IGDB API를 통해 키워드로 게임 검색
        APICalypse gameFindQuery = new APICalypse().search(keyword).fields("*").limit(6);

        try {
            var games = ProtoRequestKt.games(IGDBWrapper.INSTANCE, gameFindQuery);
            // 검색 결과가 없을 경우
            if (games.isEmpty()) {
                return foundGames; // 빈 리스트 반환
            }

            for (var game : games) {
                CategoryEntity existingCategory = categoryRepository.findByCategoryId(game.getId());
                if (existingCategory == null) {
                    // 각 게임에 대한 커버 정보 가져오기
                    Cover cover = getCoverForGame(game.getId());
                    String imageUrl =
                            (cover != null) ? cover.getUrl().replace("/t_thumb/", "/t_cover_big/")
                                    : defaultStaticUrl;

                    log.info("갯수={}", game.getGenresCount());
                    if (game.getGenresCount() > 0) {
                        for (int i = 0; i < game.getGenresCount(); i++) {
                            log.info("인덱스={}, id={}", i + 1, game.getGenres(i));
                        }
                    }

                    // CategoryEntity 엔터티 생성 및 저장
                    CategoryEntity category = CategoryEntity.builder().categoryId(game.getId())
                            .categoryName(game.getName()).categoryImageUrl(imageUrl)
                            .categoryTotalView(
                                    channelRepository.getTotalViewerCountByCategoryId(game.getId()))
                            .build();

                    foundGames.add(categoryRepository.save(category));
                } else {
                    // 이미 존재하면 넘어감
                    log.info("갯수={}", game.getGenresCount());
                    if (game.getGenresCount() > 0) {
                        for (int i = 0; i < game.getGenresCount(); i++) {
                            log.info("인덱스={}, id={}", i + 1, game.getGenres(i));
                        }
                    }
                    existingCategory.updateCategoryTotalView(
                            channelRepository.getTotalViewerCountByCategoryId(game.getId()));
                    foundGames.add(existingCategory);
                }
            }

        } catch (RequestException e) {
            log.error("RequestException");
            e.printStackTrace();
        }
        return foundGames;
    }

    /**
     * 게임 ID에 해당하는 커버 정보를 IGDB API에서 가져옵니다.
     *
     * @param gameId 게임 ID
     * @return 게임의 커버 정보 (존재하지 않으면 null 반환)
     * @throws RequestException IGDB API 요청 중 발생한 예외
     */
    private Cover getCoverForGame(long gameId) throws RequestException {
        APICalypse coverQuery = new APICalypse().fields("*").where("game = " + gameId).limit(1);
        var covers = ProtoRequestKt.covers(IGDBWrapper.INSTANCE, coverQuery);

        return covers.isEmpty() ? null : covers.get(0);
    }

    /**
     * Twitch API로부터 인증 토큰을 가져와 IGDB API에 인증 정보를 설정합니다.
     */
    private void setTwitchAuthCredentials() {
        TwitchAuthenticator twitchAuthenticator = TwitchAuthenticator.INSTANCE;
        // requestTwitchToken 메서드를 호출 -> Twitch API 접근 가능한 토큰 요청.
        TwitchToken twitchToken = twitchAuthenticator.requestTwitchToken(clientId, clientSecret);
        // clientId와 twitchToken.getAccess_token()을 인자로 전달하여 IGDB API에 접근할 수 있는 인증 정보 설정.
        IGDBWrapper.INSTANCE.setCredentials(clientId, twitchToken.getAccess_token());
    }

}
