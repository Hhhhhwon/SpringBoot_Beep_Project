/**
 *  방송을 시작할 때 방송 정보를 입력할 모달창을 띄우는 스크립트
 *  broadcast.js
 */
document.addEventListener('DOMContentLoaded', function() {
   
    const button = document.querySelector('#broadcastOn');
    var modal = document.getElementById('myModal');

    button.addEventListener('click', function() {
        modal.style.display = 'block'; 
    });
    
    const cancelBtn = document.querySelector('#cancelBtn')
    cancelBtn.addEventListener('click', function() {
        modal.style.display = 'none';              
    });
    
    document.addEventListener('keydown', function(event) {
        if (event.keyCode === 27) {
            modal.style.display = 'none';
        }
    });
    
});