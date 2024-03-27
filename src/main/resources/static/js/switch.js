/**
 *  채팅창 설정 스위치를 작동하는 스크립트
 *  switch.js
 */

document.addEventListener('DOMContentLoaded', () => {
    
    const followChatSwitch = document.getElementById('switch1');
    const anonymousChatSwitch = document.getElementById('switch2');
    const streamerChatSwitch = document.getElementById('switch3');
    
    const channelId = document.querySelector('.dummy').value;
    const chatState = document.querySelector('.chatState').value;
    
    // 채팅창의 현재 설정에 따라 스위치 상태 변경

    if(chatState == '[FOLLOW]') {
        followChatSwitch.checked = true;
    } else if(chatState == '[ANONYMOUS]') {
        anonymousChatSwitch.checked = true;  
    } else if(chatState == '[STREAMER_ONLY]') {
        streamerChatSwitch.checked = true;  
    }
    
    // 팔로우 챗을 바꾸는 함수
    followChatSwitch.addEventListener('change', function() {
        
        
        if (this.checked) {

            const uri = '/api/chat/follow/' + channelId;
            axios.post(uri)
            .then((response) => {
                console.log(response);
            })
            .catch((error) => {
                console.log(error);
            });
            
            console.log("체크 상태입니다.");
            
            anonymousChatSwitch.checked = false;
            streamerChatSwitch.checked = false;
            
        } else {
            
            const uri = '/api/chat/default/' + channelId;
            axios.post(uri)
            .then((response) => {
                console.log(response);
            })
            .catch((error) => {
                console.log(error);
            });

            console.log("체크 해제된 상태입니다.");
            
        }
    });
    
    // 무제한 챗으로 바꾸는 함수
    anonymousChatSwitch.addEventListener('change', function() {
        
        
        if (this.checked) {
            const uri = '/api/chat/anonymous/' + channelId;
            axios.post(uri)
            .then((response) => {
                console.log(response);
            })
            .catch((error) => {
                console.log(error);
            });
            
            console.log("체크 상태입니다.");
            
            streamerChatSwitch.checked = false;
            followChatSwitch.checked = false;
            
        } else {
            
            const uri = '/api/chat/default/' + channelId;
            axios.post(uri)
            .then((response) => {
                console.log(response);
            })
            .catch((error) => {
                console.log(error);
            });

            console.log("체크 해제된 상태입니다.");
            
        }
    });
    
    // 스티리머 챗으로 바꾸는 함수
    streamerChatSwitch.addEventListener('change', function() {
        
        
        if (this.checked) {

            const uri = '/api/chat/ice/' + channelId;
            console.log(uri);
            axios.post(uri)
            .then((response) => {
                console.log(response);
            })
            .catch((error) => {
                console.log(error);
            });
            
            console.log("체크 상태입니다.");
            
            anonymousChatSwitch.checked = false;
            followChatSwitch.checked = false;
            
        } else {
            
            const uri = '/api/chat/default/' + channelId;
            console.log(uri);
            axios.post(uri)
            .then((response) => {
                console.log(response);
            })
            .catch((error) => {
                console.log(error);
            });

            console.log("체크 해제된 상태입니다.");
            
        }
    });
    
});