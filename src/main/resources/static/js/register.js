const form = document.getElementById("register-form");
const errorBox = document.getElementById("error-box");

form.addEventListener("submit", async (e) => {
  e.preventDefault();
  errorBox.classList.remove("visible");

  const body = {
    displayName: document.getElementById("displayName").value.trim(),
    username: document.getElementById("username").value.trim(),
    email: document.getElementById("email").value.trim(),
    password: document.getElementById("password").value,
  };

  try {
    const auth = await api("/api/auth/register", { method: "POST", body });
    setSession(auth.token, auth.user);
    window.location.href = "/index.html";
  } catch (err) {
    errorBox.textContent = err.message;
    errorBox.classList.add("visible");
  }
});
