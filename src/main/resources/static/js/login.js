const form = document.getElementById("login-form");
const errorBox = document.getElementById("error-box");

form.addEventListener("submit", async (e) => {
  e.preventDefault();
  errorBox.classList.remove("visible");

  const body = {
    usernameOrEmail: document.getElementById("usernameOrEmail").value.trim(),
    password: document.getElementById("password").value,
  };

  try {
    const auth = await api("/api/auth/login", { method: "POST", body });
    setSession(auth.token, auth.user);
    window.location.href = "/index.html";
  } catch (err) {
    errorBox.textContent = err.message;
    errorBox.classList.add("visible");
  }
});
