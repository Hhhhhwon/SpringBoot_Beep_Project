function sendPasswordResetEmail() {
  var email = document.getElementById('userEmail').value;
  if (email) {
    fetch(`/user/send-password-reset-email`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ email: email })
    })
    .then(response => {
      if (response.ok) {
        document.getElementById('passwordResetResult').innerText = '비밀번호 재설정 이메일을 보냈습니다. 메일을 확인해 주세요.';
      } else {
        throw new Error('이메일 전송에 실패했습니다.');
      }
    })
    .catch(error => {
      document.getElementById('passwordResetResult').innerText = error.message;
    });
  } else {
    document.getElementById('passwordResetResult').innerText = '이메일을 입력해주세요.';
  }
}