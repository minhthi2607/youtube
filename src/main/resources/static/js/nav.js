function renderNav() {
  const container = document.getElementById("nav-actions");
  if (!container) return;

  if (isLoggedIn()) {
    const user = getCurrentUser();
    container.innerHTML = `
      <a class="btn" href="/upload.html">Upload</a>
      <a class="btn" href="/channel.html?id=${user.id}">${escapeHtml(user.displayName)}</a>
      <button class="btn danger" id="logout-btn">Log out</button>
    `;
    document.getElementById("logout-btn").addEventListener("click", () => {
      clearSession();
      window.location.href = "/index.html";
    });
  } else {
    container.innerHTML = `
      <a class="btn" href="/login.html">Log in</a>
      <a class="btn primary" href="/register.html">Sign up</a>
    `;
  }
}

renderNav();
