/**
 * follow.js
 * 
 */

document.addEventListener('DOMContentLoaded', async () => {
    // 팔로워 목록 불러오기
    fetchFollowerList();

    // 팔로우/언팔로우 버튼 불러오기
    await updateButtonVisibility();

});

async function updateButtonVisibility() {

    try {
        // 현재 사용자의 팔로우 상태를 확인하는 API 호출
        const loginUserId = document.getElementById('loginUserId').value;
        const channelUserId = document.getElementById('channelUserId').value;
        const response = await fetch(`/api/follow/status/${channelUserId}`);

        if (response.ok) {
            const data = await response.json();
            console.log(data);
            const isFollowing = data.isFollowing;

            // 팔로우 상태에 따라 버튼 초기 설정
            const followBtn = document.getElementById('followBtn');
            const unfollowBtn = document.getElementById('unfollowBtn');

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
    } catch (error) {
        console.error(error);
        console.log("팔로우 상태를 확인하는 중 오류가 발생했습니다!");
    }
}

async function followOrUnfollow(isFollow) {
    if (!isLoggedIn()) {
        alert("로그인이 필요합니다!");

        return;
    }

    try {
        const channelUserIdElement = document.getElementById('channelUserId');
        const channelUserId = channelUserIdElement.value;
        const channelUserNickname = document.getElementById('channelUserNickname').value;

        const method = isFollow ? 'POST' : 'DELETE';
        const response = await fetch(`/api/follow/${isFollow ? 'add' : 'delete'}/${channelUserId}`, {
            method: method,
        });

        if (response.ok) {
            console.log(response);
            const action = isFollow ? '팔로우' : '언팔로우';
            alert(`${channelUserNickname}님을 ${action} 하였습니다!`);

            // 성공 시 버튼 클래스 조작
            const followBtn = document.getElementById('followBtn');
            const unfollowBtn = document.getElementById('unfollowBtn');

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
        alert("오류가 발생했습니다!");
    }
}

async function follow() {
    await followOrUnfollow(true);
}

async function unfollow() {
    await followOrUnfollow(false);
}

function isLoggedIn() {

    return authentication !== 'anonymousUser';
}

async function fetchFollowerList() {
    const toUserNo = document.getElementById('channelUserId').value;
    try {
        const response = await fetch(`/api/follow/followerlist/${toUserNo}`);
        if (response.ok) {
            const data = await response.json();
            console.log(data);

            // 데이터를 Thymeleaf 템플릿에 적용
            document.getElementById('countByFollower').innerText = data.countByFollower + ' 명';
        } else {
            console.error('팔로워 목록을 불러오는 중 에러 발생:', response.status);
        }
    } catch (error) {
        console.error('팔로워 목록을 불러오는 중 에러 발생:', error);
    }
}
