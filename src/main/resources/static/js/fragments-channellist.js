document.addEventListener('DOMContentLoaded', async () => {
    await fetchPopularChannels();

    toggle.addEventListener('click', () => {
        detectSidebarState();
    });
});

async function detectSidebarState() {
    try {
        const sidebar = document.querySelector('.sidebar');
        const channelInfoElements = document.querySelectorAll('.list-group-item > div');

        if (sidebar.classList.contains('close')) {
            channelInfoElements.forEach(element => {
                element.parentElement.parentElement.style.display = 'none'; // 채널 정보 숨김 
            });
        } else {
            channelInfoElements.forEach(element => {
                element.parentElement.parentElement.style.display = 'block'; // 채널 정보 표시
            });
        }
    } catch (error) {
        console.error('사이드바 상태를 감지하는 동안 에러 발생:', error);
    }
}

async function fetchFollowList() {
    try {
        const response = await fetch('/api/follow/followlist');
        if (response.ok) {

            const data = await response.json();
            console.log(data);

            // followListElement와 itemsPerPage, currentPage를 초기화
            if (authentication !== 'anonymousUser') {
                const followListElement = document.getElementById('followList');
                followListElement.innerHTML = '<h5 class="my-2 text-secondary fw-bold" style="white-space: nowrap; overflow: hidden;">팔로우 채널</h5>';
                const itemsPerPage = 3; // 페이지 당 항목 수
                let currentPage = 1; // 현재 페이지
                let closeButton; // 닫기 버튼

                // 첫 번째 페이지 렌더링
                renderFollowList(currentPage);

                // 페이지를 렌더링하는 함수
                function renderFollowList(page) {
                    // 페이지에 표시할 항목의 시작 인덱스와 끝 인덱스 계산
                    const startIndex = (page - 1) * itemsPerPage;
                    const endIndex = Math.min(page * itemsPerPage, data.channelList.length);

                    // streamingState가 'ON'인 것들을 먼저 정렬
                    data.channelList.sort((a, b) => {
                        // 'ON'인 것이 먼저 오도록 정렬
                        if (a.streamingState.includes('ON') && !b.streamingState.includes('ON')) {
                            return -1;
                        }
                        if (!a.streamingState.includes('ON') && b.streamingState.includes('ON')) {
                            return 1;
                        }
                        return 0;
                    });

                    // 이전에 추가한 더보기 버튼이 있다면 제거
                    const loadMoreButton = document.getElementById('loadMoreButton');
                    if (loadMoreButton) {
                        loadMoreButton.remove();
                    }

                    data.channelList.slice(startIndex, endIndex).forEach(channel => {
                        const li = createChannelListItem(channel);
                        followListElement.appendChild(li);
                    });

                    // 만약 현재 페이지가 마지막 페이지가 아니라면 더보기 버튼 추가
                    if (endIndex < data.channelList.length) {
                        addLoadMoreButton();
                    }

                    // 현재 페이지가 1보다 크면 닫기 버튼 추가
                    if (endIndex >= data.channelList.length && data.channelList.length > 0) {
                        addCloseButton();
                    }
                }

                // 더보기 버튼을 추가하는 함수
                function addLoadMoreButton() {
                    const loadMoreButton = document.createElement('button');
                    loadMoreButton.id = 'loadMoreButton'; // 버튼에 ID 추가
                    loadMoreButton.innerText = '더보기';
                    loadMoreButton.classList.add('btn', 'btn-outline-secondary', 'mt-2', 'btn-sm');
                    loadMoreButton.style.border = 'none';
                    loadMoreButton.style.whiteSpace = 'nowrap';
                    loadMoreButton.style.overflow = 'hidden';

                    loadMoreButton.addEventListener('click', () => {
                        currentPage++; // 다음 페이지로 이동
                        renderFollowList(currentPage); // 페이지 렌더링
                    });

                    followListElement.appendChild(loadMoreButton); // 더보기 버튼을 followListElement에 추가
                }

                // 닫기 버튼을 추가하는 함수
                function addCloseButton() {
                    closeButton = document.createElement('button');
                    closeButton.id = 'closeButton'; // 버튼에 ID 추가
                    closeButton.innerText = '닫기';
                    closeButton.classList.add('btn', 'btn-outline-danger', 'mt-2', 'btn-sm');
                    closeButton.style.border = 'none';
                    closeButton.style.whiteSpace = 'nowrap';
                    closeButton.style.overflow = 'hidden';

                    closeButton.addEventListener('click', () => {
                        // 추가된 요소들과 닫기 버튼 제거
                        removeAllElements();
                        currentPage = 1; // 첫 번째 페이지로 돌아감
                        renderFollowList(currentPage); // 페이지 렌더링
                    });

                    if (currentPage > 1) {
                        followListElement.appendChild(closeButton); // 닫기 버튼을 followListElement에 추가
                    }
                }

                function createChannelListItem(channel) {
                    const divTag = document.createElement('div');
                    divTag.classList.add('list-group-item', 'd-flex', 'align-items-center', 'mb-1');
                    divTag.style.border = "none";

                    divTag.addEventListener('mouseenter', () => {
                        divTag.style.backgroundColor = '#1F1F23'; // 회색 빛 추가
                    });

                    divTag.addEventListener('mouseleave', () => {
                        divTag.style.backgroundColor = ''; // 마우스를 떼면 원래의 색으로 복원
                    });

                    const a = document.createElement('a'); // <a> 태그 생성
                    a.classList.add('text-decoration-none', 'text-secondary');
                    a.href = `/channel/${channel.channelUserAccountEntityChannelName}`;
                    divTag.appendChild(a);

                    const div = document.createElement('div');
                    div.classList.add('d-flex', 'align-items-center', 'flex-grow-1'); // 텍스트를 오른쪽으로 정렬하기 위해 flex 속성 추가
                    a.appendChild(div); // <div>를 <a> 태그 내에 추가

                    const img = document.createElement('img');
                    img.src = `${channel.channelProfileImg}`;
                    img.alt = '프로필 이미지';
                    img.style.width = '50px';
                    img.style.height = '50px';
                    img.classList.add('me-3', 'rounded-circle');
                    div.appendChild(img); // 이미지를 <div> 태그 내에 추가

                    const channelInfo = document.createElement('div'); // 채널 정보를 감싸는 <div> 생성
                    channelInfo.classList.add('flex-grow-1', 'my-1'); // 채널 정보가 남은 공간을 차지하도록 설정
                    div.appendChild(channelInfo); // 채널 정보를 <div> 태그 내에 추가

                    const channelName = document.createElement('span');
                    channelName.classList.add('fw-bold');
                    channelName.innerText = channel.channelUserAccountEntityChannelName;
                    channelInfo.appendChild(channelName); // 채널 이름을 <div> 태그 내에 추가

                    let text = '';

                    if (channel.streamingState.includes('ON')) {
                        text += `<br><span class="badge bg-danger">LIVE</span> 🚀 <span style="color: #FF5454; font-weight: bold;">${channel.channelViewerCount.toLocaleString()}</span><br>${channel.categoryEntityOfChannelCategoryName}`;
                    } else {
                        text += '<br>오프라인';
                    }

                    const small = document.createElement('small');
                    small.classList.add('text-truncate', 'text-secondary');
                    small.innerHTML = text;
                    channelInfo.appendChild(small); // 텍스트 정보를 <div> 태그 내에 추가

                    return divTag;
                }

                // 추가된 요소들과 닫기 버튼 제거하는 함수
                function removeAllElements() {
                    // 추가된 요소들 제거
                    const additionalItems = followListElement.querySelectorAll('.list-group-item');
                    additionalItems.forEach(item => {
                        item.remove();
                    });
                    // 닫기 버튼 제거
                    closeButton.remove();
                }
            }
        } else {
            console.error('팔로우 채널을 불러오는 중 에러 발생:', response.status);
        }
    } catch (error) {
        console.error('팔로우 채널을 불러오는 중 에러 발생:', error);
    }

}

async function fetchPopularChannels() {
    try {
        const response = await fetch('/api/channel/popular');
        if (response.ok) {
            const data = await response.json();
            console.log(data);

            const popularListElement = document.getElementById('popularList');
            popularListElement.innerHTML = '<h5 class="my-2 text-secondary fw-bold" style="white-space: nowrap; overflow: hidden;">인기 채널</h5>';

            const itemsPerPage = 3;
            let currentPage = 1;
            let closeButtonByPopular;

            renderPopularList(currentPage);

            function renderPopularList(page) {
                const startIndex = (page - 1) * itemsPerPage;
                const endIndex = Math.min(page * itemsPerPage, data.length);


                data.sort((a, b) => {
                    if (a.streamingState.includes('ON') && !b.streamingState.includes('ON')) {
                        return -1;
                    }
                    if (!a.streamingState.includes('ON') && b.streamingState.includes('ON')) {
                        return 1;
                    }
                    return 0;
                });

                const loadMoreButtonByPopular = document.getElementById('loadMoreButtonByPopular');
                if (loadMoreButtonByPopular) {
                    loadMoreButtonByPopular.remove();
                }

                data.slice(startIndex, endIndex).forEach(popular => {
                    const x = createpopularListItem(popular);
                    popularListElement.appendChild(x);
                });

                // 만약 현재 페이지가 마지막 페이지가 아니라면 더보기 버튼 추가
                if (endIndex < data.length) {
                    addLoadMoreButtonByPopular();
                }

                // 현재 페이지가 1보다 크면 닫기 버튼 추가
                if (endIndex >= data.length && data.length > 0) {
                    addCloseButtonByPopular();
                }

                // 더보기 버튼을 추가하는 함수
                function addLoadMoreButtonByPopular() {
                    const loadMoreButtonByPopular = document.createElement('button');
                    loadMoreButtonByPopular.id = 'loadMoreButtonByPopular'; // 버튼에 ID 추가
                    loadMoreButtonByPopular.innerText = '더보기';
                    loadMoreButtonByPopular.classList.add('btn', 'btn-outline-secondary', 'mt-2', 'btn-sm');
                    loadMoreButtonByPopular.style.border = 'none';
                    loadMoreButtonByPopular.style.whiteSpace = 'nowrap';
                    loadMoreButtonByPopular.style.overflow = 'hidden';

                    loadMoreButtonByPopular.addEventListener('click', () => {
                        currentPage++; // 다음 페이지로 이동
                        renderPopularList(currentPage); // 페이지 렌더링
                    });

                    popularListElement.appendChild(loadMoreButtonByPopular);
                }

                // 닫기 버튼을 추가하는 함수
                function addCloseButtonByPopular() {
                    closeButtonByPopular = document.createElement('button');
                    closeButtonByPopular.id = 'closeButtonByPopular'; // 버튼에 ID 추가
                    closeButtonByPopular.innerText = '닫기';
                    closeButtonByPopular.classList.add('btn', 'btn-outline-danger', 'mt-2', 'btn-sm');
                    closeButtonByPopular.style.border = 'none';
                    closeButtonByPopular.style.whiteSpace = 'nowrap';
                    closeButtonByPopular.style.overflow = 'hidden';

                    closeButtonByPopular.addEventListener('click', () => {
                        // 추가된 요소들과 닫기 버튼 제거
                        removeAllElementsByPopular();
                        currentPage = 1; // 첫 번째 페이지로 돌아감
                        renderPopularList(currentPage); // 페이지 렌더링
                    });

                    if (currentPage > 1) {
                        popularListElement.appendChild(closeButtonByPopular);
                    }
                }

                function createpopularListItem(channel) {
                    const divTag = document.createElement('div');
                    divTag.classList.add('list-group-item', 'd-flex', 'align-items-center', 'mb-1');
                    divTag.style.border = "none";

                    divTag.addEventListener('mouseenter', () => {
                        divTag.style.backgroundColor = '#1F1F23'; // 회색 빛 추가
                    });

                    divTag.addEventListener('mouseleave', () => {
                        divTag.style.backgroundColor = ''; // 마우스를 떼면 원래의 색으로 복원
                    });

                    const a = document.createElement('a'); // <a> 태그 생성
                    a.classList.add('text-decoration-none', 'text-secondary');
                    a.href = `/channel/${channel.channelUserAccountEntityChannelName}`;
                    divTag.appendChild(a);

                    const div = document.createElement('div');
                    div.classList.add('d-flex', 'align-items-center', 'flex-grow-1'); // 텍스트를 오른쪽으로 정렬하기 위해 flex 속성 추가
                    a.appendChild(div); // <div>를 <a> 태그 내에 추가

                    const img = document.createElement('img');
                    img.src = `${channel.channelProfileImg}`;
                    img.alt = '프로필 이미지';
                    img.style.width = '50px';
                    img.style.height = '50px';
                    img.classList.add('me-3', 'rounded-circle');
                    div.appendChild(img); // 이미지를 <div> 태그 내에 추가

                    const channelInfo = document.createElement('div'); // 채널 정보를 감싸는 <div> 생성
                    channelInfo.classList.add('flex-grow-1'); // 채널 정보가 남은 공간을 차지하도록 설정
                    div.appendChild(channelInfo); // 채널 정보를 <div> 태그 내에 추가

                    const channelName = document.createElement('span');
                    channelName.classList.add('fw-bold');
                    channelName.innerText = channel.channelUserAccountEntityChannelName;
                    channelInfo.appendChild(channelName); // 채널 이름을 <div> 태그 내에 추가

                    let text = '';

                    if (channel.streamingState.includes('ON')) {
                        text += `<br><span class="badge bg-danger">LIVE</span> 🚀 <span style="color: #FF5454; font-weight: bold;">${channel.channelViewerCount.toLocaleString()}</span><br>${channel.categoryEntityOfChannelCategoryName}`;
                    } else {
                        text += '<br>오프라인';
                    }

                    const small = document.createElement('small');
                    small.classList.add('text-truncate', 'text-secondary');
                    small.innerHTML = text;
                    channelInfo.appendChild(small); // 텍스트 정보를 <div> 태그 내에 추가

                    return divTag;
                }

                // 추가된 요소들과 닫기 버튼 제거하는 함수
                function removeAllElementsByPopular() {
                    // 추가된 요소들 제거
                    const additionalItems = popularListElement.querySelectorAll('.list-group-item');
                    additionalItems.forEach(item => {
                        item.remove();
                    });
                    // 닫기 버튼 제거
                    closeButtonByPopular.remove();
                }

            }
            await fetchFollowList();
        } else {
            console.error('인기 있는 채널을 불러오는 중 에러 발생:', response.status);
        }
    } catch (error) {
        console.error('인기 있는 채널을 불러오는 중 에러 발생:', error);
    }
}