package com.itwill.beep.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {

    CategoryEntity findByCategoryId(long categoryId);

    List<CategoryEntity> findByCategoryTotalViewNotNull();

    /* 카테고리 제목으로 찾기 */
    List<CategoryEntity> findByCategoryNameContainingIgnoreCase(String keyword);

}
