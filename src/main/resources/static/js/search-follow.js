document.addEventListener('DOMContentLoaded', () => {
    updateButtonVisibility();
});

async function updateButtonVisibility() {
    try {
        const loginUserIdElements = document.getElementsByClassName('data-loginUserId');
        const channelUserIdElements = document.getElementsByClassName('data-channelUserId');
        const followBtnElements = document.getElementsByClassName('data-followBySearchResult');
        const unfollowBtnElements = document.getElementsByClassName('data-unfollowBySearchResult');

        for (let i = 0; i < loginUserIdElements.length; i++) {
            const loginUserId = loginUserIdElements[i].value;
            const channelUserId = channelUserIdElements[i].value;
            const response = await fetch(`/api/follow/status/${channelUserId}`);

            if (response.ok) {
                const data = await response.json();
                console.log(data);
                const isFollowing = data.isFollowing;

                const followBtn = followBtnElements[i];
                const unfollowBtn = unfollowBtnElements[i];

                if (isFollowing) {
                    // 이미 팔로우 중인 경우
                    followBtn.classList.add('d-none');
                    unfollowBtn.classList.remove('d-none');
                } else {
                    // 아직 팔로우하지 않은 경우
                    followBtn.classList.remove('d-none');
                    unfollowBtn.classList.add('d-none');
                }

                // channel id와 loginUserId 비교하여 팔로우 버튼 비활성화
                if (loginUserId === channelUserId) {
                    followBtn.disabled = true;
                } else {
                    followBtn.disabled = false;
                }
            } else {
                console.error(response);
                console.log("팔로우 상태를 확인하는 중 오류가 발생했습니다!");
            }
        }
    } catch (error) {
        console.error(error);
        console.log("팔로우 상태를 확인하는 중 오류가 발생했습니다!");
    }
}

async function followOrUnfollow(isFollow, index) {
    console.log(`followOrUnfloww(isFollow=${isFollow}, index=${index})`);
    
    if (!isLoggedIn()) {
        alert("로그인이 필요합니다!");
        return;
    }

    try {
        const channelUserId = document.getElementsByClassName('data-channelUserId')[index].value;
        const channelUserNickname = document.getElementsByClassName('data-channelUserNickname')[index].value;

        const method = isFollow ? 'POST' : 'DELETE';
        const response = await fetch(`/api/follow/${isFollow ? 'add' : 'delete'}/${channelUserId}`, {
            method: method,
        });

        if (response.ok) {
            console.log(response);
            const action = isFollow ? '팔로우' : '언팔로우';
            alert(`${channelUserNickname}님을 ${action} 하였습니다!`);

            // 성공 시 버튼 클래스 조작
            const followBtn = document.getElementsByClassName('data-followBySearchResult')[index];
            const unfollowBtn = document.getElementsByClassName('data-unfollowBySearchResult')[index];

            if (isFollow) {
                followBtn.classList.add('d-none');
                unfollowBtn.classList.remove('d-none');
            } else {
                followBtn.classList.remove('d-none');
                unfollowBtn.classList.add('d-none');
            }
        } else {
            console.error(response);
            const action = isFollow ? '팔로우' : '언팔로우';
            alert(`사용자를 ${action} 하는 중 오류가 발생했습니다!`);
        }
    } catch (error) {
        console.error(error);
        console.log("오류가 발생했습니다!");
    }
}

async function follow(index) {
    await followOrUnfollow(true, index);
}

async function unfollow(index) {
    await followOrUnfollow(false, index);
}

function isLoggedIn() {
    return authentication !== 'anonymousUser';
}