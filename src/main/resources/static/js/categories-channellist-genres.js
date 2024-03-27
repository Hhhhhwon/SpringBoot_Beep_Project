window.onload = function() {
    var genreBadges = document.querySelectorAll('.genreElement');
    genreBadges.forEach(function(badge) {
        badge.style.fontSize = '1.5em'; // 크기를 크게 만듭니다.
        badge.style.padding = '10px'; // 원의 크기를 조절할 수 있습니다.
        badge.style.borderRadius = '10px'; // 네모 모양의 모서리를 둥글게 만듭니다.

        var genreColors = {
            "POINT_AND_CLICK": "#f44336",
            "FIGHTING": "#BA68C8",
            "SHOOTER": "#03a9f4",
            "MUSIC": "#4caf50",
            "PLATFORM": "#ff9800",
            "PUZZLE": "#9c27b0",
            "RACING": "#ff5722",
            "REAL_TIME_STRATEGY": "#e91e63",
            "ROLE_PLAYING": "#00bcd4",
            "SIMULATOR": "#607d8b",
            "SPORT": "#795548",
            "MOBA": "#ffeb3b",
            "TURN_BASED_STRATEGY": "#8bc34a",
            "TACTICAL": "#2196f3",
            "HACK_AND_SLASH": "#cddc39",
            "QUIZ_TRIVIA": "#009688",
            "PINBALL": "#9e9e9e",
            "ADVENTURE": "#8e24aa",
            "INDIE": "#7b1fa2",
            "ARCADE": "#00e676",
            "VISUAL_NOVEL": "#76ff03",
            "CARD_AND_BOARD_GAME": "#ffd600",
            "STRATEGY": "#64ffda"
        };

        // 배지의 텍스트를 가져옴
        var genreText = badge.innerText.trim();

        // 해당 장르의 색상이 있는지 확인하고 있으면 배경색을 설정
        if (genreColors.hasOwnProperty(genreText)) {
            badge.style.backgroundColor = genreColors[genreText];

            // 배경색이 밝은 경우 흰색 텍스트, 어두운 경우 검은색 텍스트 적용
            var backgroundColor = genreColors[genreText];
            var textColor = getTextColor(backgroundColor);
            badge.style.color = textColor;
        } else {
            // 장르에 해당하는 색상이 없으면 기본 색상 사용
            badge.style.backgroundColor = '#777';
            badge.style.color = '#fff';
        }
    });

    // 배경색에 따라 텍스트 색상을 반환하는 함수
    function getTextColor(bgColor) {
        // 배경색의 밝기 계산
        var color = bgColor.charAt(0) === "#" ? bgColor.substring(1, 7) : bgColor;
        var r = parseInt(color.substring(0, 2), 16); // Red 채널
        var g = parseInt(color.substring(2, 4), 16); // Green 채널
        var b = parseInt(color.substring(4, 6), 16); // Blue 채널

        // YIQ 색상 공식을 사용하여 밝기 계산
        var yiq = ((r * 299) + (g * 587) + (b * 114)) / 1000;

        // 밝기에 따라 흰색 또는 검은색 반환
        return yiq >= 128 ? '#000' : '#fff';
    }

    // 카테고리 이름 및 상세 정보 텍스트 색상 변경
    var categoryNameElement = document.getElementById('Cname');
    var viewerAndChannelElement = document.getElementById('VC');
    if (genreBadges.length === 1) {
        categoryNameElement.style.color = getComputedStyle(genreBadges[0]).backgroundColor;
        viewerAndChannelElement.style.color = getComputedStyle(genreBadges[0]).backgroundColor;
    } else if (genreBadges.length > 1) {
        categoryNameElement.style.color = getComputedStyle(genreBadges[0]).backgroundColor;
        viewerAndChannelElement.style.color = getComputedStyle(genreBadges[1]).backgroundColor;
    } else if (genreBadges.length === 0) {
        categoryNameElement.style.color = 'rgba(200, 200, 200, 0.8)';
        viewerAndChannelElement.style.color = 'rgba(200, 200, 200, 0.8)';
    }
}