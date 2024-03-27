document.addEventListener('DOMContentLoaded', function() {
    const categoryInput = document.getElementById('categoryInput');
    const categoryList = document.getElementById('categoryList');
    
    categoryInput.addEventListener('input', function() {
        const keyword = categoryInput.value;
        console.log(keyword);
    
        const requestURL = 'http://13.209.122.0:8081/' + 'categories/api/search' + '?keyword=' + keyword;
        
        const xhr = new XMLHttpRequest();
        xhr.open('GET', requestURL, true);

        xhr.onreadystatechange = function() {
            if (xhr.readyState === XMLHttpRequest.DONE) {
                if (xhr.status === 200) {
                    const categories = JSON.parse(xhr.responseText);
                    displayCategories(categories);
                } else {
                    console.error('Request failed: ' + xhr.status);
                }
            }
        };

        xhr.send();
    });

    // 선택된 카테고리 스타일 변경
    function selectCategory(categoryId) {
        const previousSelectedCategory = categoryList.querySelector('.selected');
        if (previousSelectedCategory) {
            previousSelectedCategory.classList.remove('selected');
        }

        const selectedCategory = categoryList.querySelector(`[data-id="${categoryId}"]`);
        if (selectedCategory) {
            selectedCategory.classList.add('selected');
        }

        console.log('Selected category ID:', categoryId);

        document.getElementById('categoryId').value = categoryId;
    }

    // 검색된 카테고리를 표시하는 함수
    function displayCategories(categories) {
        categoryList.innerHTML = '';

        if (categoryInput.value === '') {
            const justChatting = 100_000_000;
            // 검색어가 비어 있을 때 기본 카테고리 선택
            selectCategory(justChatting); // Just chatting의 카테고리 ID를 사용합니다.
        } else {
            categories.forEach(function(category) {
                const categoryItem = document.createElement('div');
                categoryItem.classList.add('category-item');
                categoryItem.dataset.id = category.categoryId;

                const categoryImage = document.createElement('img');
                categoryImage.src = category.categoryImageUrl;
                categoryImage.alt = category.categoryName;
                categoryImage.width = 40;
                categoryImage.height = 40;
                categoryItem.appendChild(categoryImage);

                const categoryName = document.createElement('span');
                categoryName.textContent = category.categoryName;
                categoryItem.appendChild(categoryName);

                categoryItem.addEventListener('click', function() {
                    selectCategory(category.categoryId);
                });
                categoryList.appendChild(categoryItem);
            });
        }
    }
});