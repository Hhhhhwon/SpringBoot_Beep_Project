package com.itwill.beep.domain;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@ToString
@EqualsAndHashCode
@Entity
@Table(name = "category")
public class CategoryEntity {
    @Id
    @Column(name = "category_id")
    private Long categoryId;

    @Basic(optional = false)
    @Column(name = "category_name")
    private String categoryName;

    @Column(name = "category_image_url")
    private String categoryImageUrl;

    @Column(name = "category_total_view")
    private Long categoryTotalView;

    public void updateCategoryTotalView(Long categoryTotalView) {
        this.categoryTotalView = categoryTotalView;
    }

}
