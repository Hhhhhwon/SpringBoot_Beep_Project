function findUsername() {
  var email = document.getElementById('userEmail').value;
  if (email) {
    fetch(`/user/find-username?email=${encodeURIComponent(email)}`)
    .then(response => {
      if (response.ok) {
        return response.text();
      } else {
        throw new Error('아이디를 찾을 수 없습니다.');
      }
    })
    .then(username => {
      document.getElementById('usernameResult').innerText = `찾으신 아이디는 ${username}입니다.`;
    })
    .catch(error => {
      document.getElementById('usernameResult').innerText = error.message;
    });
  } else {
    document.getElementById('usernameResult').innerText = '이메일을 입력해주세요.';
  }
}