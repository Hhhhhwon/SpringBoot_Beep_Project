/**
 * SSE(Server-Sent-Event)를 처리하기 위한 스크립트
 */

 document.addEventListener('DOMContentLoaded', () => {
     
    const sse = new EventSource("http://13.209.122.0:8081/connect");

    sse.addEventListener('connect', (e) => {
        console.log(e);
        const { data: receivedConnectData } = e;
        console.log('connect event data: ',receivedConnectData);  // "connected!"
    });
    
     
    sse.addEventListener('broadcast_on', (e) => {
        // 알림 처리
        console.log(e);
        // 방송 시작 알림 처리
        // ... 알림 표시 로직
        // 알림 생성 및 표시
        createNotification(e.data);
        
        // 알림창 생성 함수
        
        });
    
        function createNotification(data) {
            console.log(data);
            
            // JSON 객체 처리
            let message = JSON.parse(data);
            const title = message.title;
            // notificationElement.textContent = JSON.stringify(data);

            const strongElement = document.querySelector(".toast-header strong");
            strongElement.textContent = message.streamer + '님이 방송을 시작했습니다.';
            
            const body = document.querySelector('.toast-body');
            body.textContent = title;
            
            const img = document.querySelector('.notiImg');
            img.src = message.profileImageUrl;
        
        
            //let notification = document.getElementById('toast-body');
            //notification.innerHTML = sival;
            
            const toastLiveExample = document.getElementById('liveToast')
            const toast = new bootstrap.Toast(toastLiveExample);
            
            toast.show();
            
            // 알림창 보여주기
            
            // 3초 후 알림창 숨기기
            setTimeout(() => {
                toast.hide();
            }, 300000);
            }
    
 });