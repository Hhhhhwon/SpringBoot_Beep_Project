document.addEventListener('DOMContentLoaded', function() {
  var resetForm = document.querySelector('form');
  resetForm.onsubmit = function(event) {
    var password = document.getElementById('password').value;
    var confirmPassword = document.getElementById('confirmPassword').value;

    if (password !== confirmPassword) {
      event.preventDefault();
      alert('비밀번호가 일치하지 않습니다.');
    }
  };
});