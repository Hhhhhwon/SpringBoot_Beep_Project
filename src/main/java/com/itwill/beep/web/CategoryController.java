package com.itwill.beep.web;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.itwill.beep.domain.CategoryEntity;
import com.itwill.beep.domain.ChannelEntity;
import com.itwill.beep.domain.GenreType;
import com.itwill.beep.domain.UserAccountEntity;
import com.itwill.beep.service.CategoryService;
import com.itwill.beep.service.ChannelService;
import com.itwill.beep.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/categories")
public class CategoryController {
    private final CategoryService categoryService;
    private final ChannelService channelService;
    private final UserService userService;

    @GetMapping
    public String showCategories(Model model) {
        log.info("showCategories()");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        UserAccountEntity loginUser = null;
        if (authentication.getPrincipal() instanceof OAuth2User) {
            loginUser = userService.findUserByUserName(
                    (String) ((OAuth2User) principal).getAttributes().get("name"));
        } else if (authentication.isAuthenticated()) {
            loginUser = userService.findUserByUserName(authentication.getName());
        } else {
            log.info("로그인되지 않은 사용자입니다.");
        }

        List<CategoryEntity> mostViewers = categoryService.findByTotalViewers();
        List<CategoryEntity> populars = categoryService.findByPopulars();

        model.addAttribute("mostViewers", mostViewers);
        model.addAttribute("populars", populars);

        // fragments 에 필요한 모델
        model.addAttribute("userAccount", loginUser);

        // myModal에 필요한 모델
        ChannelEntity forModal = channelService.findChannelByUserAccount(loginUser);
        model.addAttribute("channel", forModal);
        if (forModal != null) {
            String status = forModal.getStreamingStateSet().toString();
            model.addAttribute("status", status);
        }

        return "categories";
    }

    @GetMapping("/{categoryId}")
    public String categoriesChannelList(@PathVariable("categoryId") Long categoryId, Model model) {
        log.info("categoriesChannelList");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        UserAccountEntity loginUser = null;
        if (authentication.getPrincipal() instanceof OAuth2User) {
            loginUser = userService.findUserByUserName(
                    (String) ((OAuth2User) principal).getAttributes().get("name"));
        } else if (authentication.isAuthenticated()) {
            loginUser = userService.findUserByUserName(authentication.getName());
        } else {
            log.info("로그인되지 않은 사용자입니다.");
        }

        CategoryEntity categoryEntityDetails = categoryService.findByCategoryIdIs(categoryId);
        List<ChannelEntity> categoryChannelList =
                channelService.findByCategoryCategoryId(categoryId);
        List<GenreType> genreList = categoryService.findGenre(categoryId);
        model.addAttribute("genreList", genreList);
        model.addAttribute("categoryDetails", categoryEntityDetails);
        model.addAttribute("categoryChannelList", categoryChannelList);

        // fragments 에 필요한 모델
        model.addAttribute("userAccount", loginUser);

        // myModal에 필요한 모델
        ChannelEntity forModal = channelService.findChannelByUserAccount(loginUser);
        model.addAttribute("channel", forModal);
        if (forModal != null) {
            String status = forModal.getStreamingStateSet().toString();
            model.addAttribute("status", status);
        }
        return "categories-channellist";
    }

    @ResponseBody
    @GetMapping("/api/search")
    public ResponseEntity<List<CategoryEntity>> searchGamesApi(
            @RequestParam(value = "keyword") String keyword) {
        log.info("searchGamesApi(keyword={})", keyword);

        List<CategoryEntity> foundGames = categoryService.searchGames(keyword);

        // ResponseEntity를 사용하여 HTTP 응답을 반환
        return ResponseEntity.ok(foundGames);

    }

}
