/**
 * category.js
 * categories.html 적용
 * 
 */

async function searchKeyword() {
    // 검색 키워드 가져오기
    const searchKeyword = document.getElementById("searchKeyword").value;

    try {
        // Fetch API를 사용한 비동기 요청
        const response = await fetch(`/categories/api/search?keyword=${encodeURIComponent(searchKeyword)}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            // 성공적으로 응답받은 경우 결과를 화면에 표시
            const searchData = await response.json();
            displaySearchResults(searchData);
        } else {
            // 에러 발생 시 메시지 출력
            console.error("에러: " + response.statusText);
        }
    } catch (error) {
        console.error("에러: " + error.message);
    }
}

function displaySearchResults(results) {
    const resultContainer = document.getElementById("searchResults");
    resultContainer.innerHTML = ""; // 결과를 초기화

    // 결과 반복문으로 처리
    for (let i = 0; i < results.length; i++) {
        const category = results[i];

        // 카드 생성 및 속성 설정
        const card = document.createElement("div");
        card.className = "col-lg-2 col-md-4 mb-4";
        card.innerHTML = `
            <div class="divCategoryStyle card categoryCard">
                <a href="/categories/${category.categoryId}" class="card-link text-decoration-none text-white">
                    <img src="${category.categoryImageUrl}" class="img-fluid card-img-top" alt="Category Image" style="width: 200px; height: 250px;">
                    <div class="card-body bg-dark">
                        <h5 class="text-left text-white text-truncate fs-5">${category.categoryName}</h5>
                        <h5 class="text-left text-white text-truncate fs-6">
                            <span class="totalViewSpan">${category.categoryTotalView != null ? '시청자 ' + category.categoryTotalView + ' 명' : '시청자 0명'}</span>
                        </h5>
                    </div>
                </a>
            </div>
        `;

        // 결과를 화면에 추가
        resultContainer.appendChild(card);
    }

    // 검색 결과를 삭제하는 버튼 추가
    const clearButton = document.createElement("button");
    clearButton.type = "button";
    clearButton.className = "btn btn-danger my-3";
    clearButton.textContent = "검색 결과 삭제";
    clearButton.addEventListener("click", () => {
        resultContainer.innerHTML = ""; // 검색 결과를 모두 지움
    });
    resultContainer.appendChild(clearButton);
}

// 엔터 키 이벤트 핸들러 추가
document.getElementById("searchKeyword").addEventListener("keyup", function(event) {
    if (event.key === "Enter") {
        event.preventDefault(); // 기본 엔터 동작 방지
        searchKeyword(); // 검색 함수 호출
    }
});