/**
 * checkUsername.js
 * 회원가입시 사용자 아이디 중복 여부를 확인하여 유효성 검사.
 */

// DOMContentLoaded 이벤트가 발생하면 실행되는 함수
document.addEventListener("DOMContentLoaded", function() {

	// 각 입력 필드에 대한 요소 가져오기
	const usernameInput = document.querySelector("#username"); // 아이디 입력 필드
	const passwordInput = document.querySelector("#password"); // 비밀번호 입력 필드
	const nicknameInput = document.querySelector("#userNickname"); // 닉네임 입력 필드
	const emailInput = document.querySelector("#userEmail"); // 이메일 입력 필드
	const signupButton = document.querySelector("#signupButton"); // 회원가입 버튼
	const checkUsernameButton = document.querySelector("#checkUsernameButton"); // 아이디 중복 확인 버튼

	// 아이디 중복 확인 여부를 저장할 변수
	let isUsernameChecked = false;


	// 회원가입 버튼 클릭 시 이벤트 처리
	signupButton.addEventListener("click", async function(event) {
		event.preventDefault(); // 기본 submit 동작 방지

		const username = usernameInput.value; // 입력된 아이디 값 가져오기

		// 사용자가 아이디를 입력하지 않은 경우 경고창 표시
		if (!username) {
			Swal.fire({
				icon: "warning",
				title: "아이디를 입력해주세요!",
				text: "아이디를 비워둘 수 없습니다.",
			});
			return; // 함수 종료
		}


		// 중복 체크 여부 확인
		if (!isUsernameChecked) {
			Swal.fire({
				icon: "warning",
				title: "중복 체크를 먼저 진행해주세요!",
				text: "사용 가능한 아이디 여부를 확인하려면 중복 체크 버튼을 클릭하세요.",
			});
			return; // 함수 종료
		}

		// 중복되지 않은 아이디인 경우 비밀번호, 닉네임, 이메일 값 가져오기
		const password = passwordInput.value; // 입력된 비밀번호 값 가져오기
		const nickname = nicknameInput.value; // 입력된 닉네임 값 가져오기
		const email = emailInput.value; // 입력된 이메일 값 가져오기

		// 비밀번호, 닉네임, 이메일이 비어있는지 확인
		if (!password) {
			Swal.fire({
				icon: "warning",
				title: "비밀번호를 입력해주세요.",
				text: "비밀번호를 비워둘 수 없습니다.",
			});
			return; // 함수 종료
		}

		if (!email) {
			Swal.fire({
				icon: "warning",
				title: "이메일을 입력해주세요.",
				text: "이메일을 비워둘 수 없습니다.",
			});
			return; // 함수 종료
		}
		
    // 사용자 이메일 중복 여부를 확인하는 URI 생성
    const emailUri = `/user/signup/checkUseremail?useremail=${email}`;
    console.log(emailUri);

    try {
        // Axios를 사용하여 서버에 요청을 보냄
        const response = await axios.get(emailUri);
        console.log(response);
        
        // 서버 응답에서 결과를 가져옴
        const result = response.data;
        console.log(result);

        // 결과에 따라 처리
        if (result === "Y") {
            // 중복된 이메일인 경우 에러 메시지 표시
            Swal.fire({
                icon: "error",
                title: "이미 존재하는 사용자 이메일이거나 사용할 수 없는 이메일입니다!",
                text: "다른 이메일을 입력해주세요.",
            });
            console.log(result);
            return; // 함수 종료
        } else if (result === "N") {
            // 중복되지 않은 닉네임인 경우 성공 메시지 표시
            Swal.fire({
                icon: "success",
                title: "사용 가능한 이메일입니다!",
                showConfirmButton: false,
                timer: 1500
            });
        } else {
            // 다른 예외 처리는 보류
        }
    } catch (error) {
        // 에러가 발생한 경우 콘솔에 에러 출력
        console.error("Error:", error);
    }

		if (!nickname) {
			Swal.fire({
				icon: "warning",
				title: "닉네임을 입력해주세요.",
				text: "닉네임을 비워둘 수 없습니다.",
			});
			return; // 함수 종료
		}
		
    // 사용자 닉네임 중복 여부를 확인하는 URI 생성
    const nickNameUri = `/user/signup/checkUsernickname?usernickname=${nickname}`;
    console.log(nickNameUri);

    try {
        // Axios를 사용하여 서버에 요청을 보냄
        const response = await axios.get(nickNameUri);
        console.log(response);
        
        // 서버 응답에서 결과를 가져옴
        const result = response.data;
        console.log(result);

        // 결과에 따라 처리
        if (result === "Y") {
            // 중복된 닉네임인 경우 에러 메시지 표시
            Swal.fire({
                icon: "error",
                title: "이미 존재하는 사용자 닉네임이거나 사용할 수 없는 닉네임입니다!",
                text: "다른 닉네임 입력해주세요.",
            });
            console.log(result);
            return; // 함수 종료
        } else if (result === "N") {
            // 중복되지 않은 닉네임인 경우 성공 메시지 표시
            Swal.fire({
                icon: "success",
                title: "사용 가능한 닉네임입니다!",
                showConfirmButton: false,
                timer: 1500
            });
        } else {
            // 다른 예외 처리는 보류
        }
    } catch (error) {
        // 에러가 발생한 경우 콘솔에 에러 출력
        console.error("Error:", error);
    }

		// 비밀번호, 닉네임, 이메일 양식 확인
		if (!isValidPassword(password)) {
			Swal.fire({
				icon: "error",
				title: "비밀번호 양식 오류",
				text: "비밀번호는 8자 이상, 영문 대소문자와 숫자, 특수문자를 포함해야 합니다.",
			});
			return; // 함수 종료
		}

		if (!isValidEmail(email)) {
			Swal.fire({
				icon: "error",
				title: "이메일 양식 오류",
				text: "올바른 이메일 양식을 입력해주세요.",
			});
			return; // 함수 종료
		}
		
		


		if (!isValidNickname(nickname)) {
			Swal.fire({
				icon: "error",
				title: "닉네임 양식 오류",
				text: "닉네임은 2자 이상이어야 합니다.",
			});
			return; // 함수 종료
		}
		
		

		// 정상적인 경우에만 회원가입 완료 처리

		// 타이머 간격을 저장할 변수
		let timerInterval;

		// SweetAlert2 라이브러리를 사용하여 회원가입 완료 메시지 표시
		Swal.fire({
			// 제목
			title: "회원가입 완료!",
			// HTML 내용, 여기에는 타이머를 나타내는 <b> 태그가 들어감(Swal 에서 퍼온 그대로).
			html: "<b></b>초 후에 자동으로 이동합니다.",
			// 타이머 설정, 4000밀리초(4초)
			timer: 4000,
			// 타이머에 따라 진행 바 표시
			timerProgressBar: true,
			// 팝업이 열릴 때 실행되는 함수
			didOpen: () => {
				// 타이머 엘리먼트를 찾아서 저장
				const timer = Swal.getPopup().querySelector("b");
				// 타이머 간격마다 실행되는 함수
				timerInterval = setInterval(() => {
					// 남은 시간을 초로 변환하여 타이머 엘리먼트에 표시(Swal은 밀리초 단위 였음)
					const secondsLeft = Swal.getTimerLeft() / 1000;
					timer.textContent = `${Math.ceil(secondsLeft)}`;
				}, 1000);
			},
			// 팝업이 닫힐 때 실행되는 함수
			willClose: () => {
				// 타이머 간격을 지워줌
				clearInterval(timerInterval);
			},
			// 아이콘 설정 (회원가입 완료를 나타내는 성공 아이콘)
			icon: "success",
			// 확인 버튼 비활성화
			showConfirmButton: false,
			// 배경색 설정
			background: "#f0f0f0",
			// 외부 클릭, ESC 키, Enter 키 비활성화
			allowOutsideClick: false,
			allowEscapeKey: false,
			allowEnterKey: false
		}).then(() => {
			// 팝업이 닫힌 후에는 폼을 자동으로 제출
			document.querySelector(".form").submit();
			// 콘솔에 메시지 출력
			console.log("타이머 닫힘");
		});
	});

	checkUsernameButton.addEventListener("click", function() {
		// 사용자가 입력한 아이디를 가져옴
		const username = usernameInput.value;

		// 만약 아이디가 비어있다면 경고 메시지를 표시하고 함수 종료
		if (!username) {
			Swal.fire({
				icon: "warning",
				title: "아이디를 입력해주세요!",
				text: "아이디를 비워둘 수 없습니다.",
			});
			return; // 함수 종료
		}

		// 사용자 아이디 중복 여부를 확인하는 URI 생성
		const nameUri = `/user/signup/checkusername?username=${username}`;
		console.log(nameUri);
		

		
		

		// Axios를 사용하여 서버에 요청을 보냄
		axios.get(nameUri)
			.then(response => {
				// 서버 응답에서 결과를 가져옴
				const result = response.data;
				console.log(result);

				// 결과에 따라 처리
				if (result === "Y") {
					// 중복된 아이디인 경우 에러 메시지 표시
					Swal.fire({
						icon: "error",
						title: "이미 존재하는 사용자 아이디거나 사용할 수 없는 아이디입니다!",
						text: "다른 아이디를 입력해주세요.",
					});
				} else if (result === "N") {
					// 중복되지 않은 아이디인 경우 성공 메시지 표시
					isUsernameChecked = true; // 중복 체크 완료
					Swal.fire({
						icon: "success",
						title: "사용 가능한 아이디입니다!",
						showConfirmButton: false,
						timer: 1500
					});
				} else {
					// 다른 예외 처리는 보류
				}
			})
			.catch(error => {
				// 에러가 발생한 경우 콘솔에 에러 출력
				console.error("Error:", error);
			});
	});

	// 비밀번호 양식 확인 함수
	function isValidPassword(password) {
		// 엄격한 비밀번호 양식 확인 정규식
		// 8자 이상, 영문 대소문자와 숫자, 특수문자 중 최소 1개 이상 포함
		const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/;
		return passwordRegex.test(password);
	}

	// 닉네임 양식 확인 함수
	function isValidNickname(nickname) {
		// 간단한 닉네임 양식 확인 
		return nickname.length >= 2;
	}

	// 이메일 양식 확인 함수
	function isValidEmail(email) {
		// 이메일 양식 확인 정규식
		 const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}$/;
		return emailRegex.test(email);
	}

	/* 테스트 예시
	const passwordTest = isValidPassword("Abcd1234!"); // true (올바른 비밀번호 형식)
	const nicknameTest = isValidNickname("JohnDoe");    // true (닉네임은 2자 이상)
	const emailTest = isValidEmail("john.doe@example.com"); // true (올바른 이메일 형식)

	console.log(passwordTest, nicknameTest, emailTest); */
});
