/**
 * Sidebar.js
 * 
 * 이 스크립트는 토글 및 다크 모드 스위치가 있는 사이드바의 기능을 관리
 */

// 필요한 DOM 요소 선택
const body = document.body,
      sidebar = document.querySelector(".sidebar"),
      toggle = document.querySelector(".toggle"),
      modeSwitch = document.querySelector(".toggle-switch"),
      modeText = document.querySelector(".mode-text");

// 로컬 스토리지에서 저장된 테마 모드를 가져옴
const savedTheme = localStorage.getItem("theme");

// 페이지 로드 시 초기 테마 모드 설정
body.classList.add(savedTheme || "light");

// 함수를 사용하여 테마 모드 업데이트
function updateTheme() {
    body.classList.toggle("dark");

    // 현재 모드에 기반하여 모드 텍스트 업데이트
    modeText.innerText = body.classList.contains("dark") ? "Light Mode" : "Dark Mode";

    // 테마 모드를 로컬 스토리지에 저장
    const currentTheme = body.classList.contains("dark") ? "dark" : "light";
    localStorage.setItem("theme", currentTheme);
}

// 사이드바를 열고 닫기 위한 토글 버튼에 대한 이벤트 리스너
toggle.addEventListener("click", () => {
    sidebar.classList.toggle("close");
});

// 다크 모드 스위치에 대한 이벤트 리스너
modeSwitch.addEventListener("click", updateTheme);