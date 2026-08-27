const params = new URLSearchParams(window.location.search);
const channelId = params.get("id");
const currentUser = getCurrentUser();

if (!channelId) {
  window.location.href = "/index.html";
}

const nameEl = document.getElementById("channel-name");
const metaEl = document.getElementById("channel-meta");
const subscribeBtn = document.getElementById("subscribe-btn");
const grid = document.getElementById("video-grid");
const emptyState = document.getElementById("empty");

function applySubscriptionStatus(status) {
  subscribeBtn.dataset.subscribed = status.subscribed;
  subscribeBtn.textContent = status.subscribed ? "Subscribed" : "Subscribe";
  metaEl.textContent = `${formatCount(status.subscriberCount)} subscribers`;
}

async function loadChannel() {
  const profile = await api(`/api/users/${channelId}`);
  nameEl.textContent = profile.displayName;
  metaEl.textContent = `${formatCount(profile.subscriberCount)} subscribers`;

  if (currentUser && currentUser.id !== profile.id) {
    subscribeBtn.style.display = "inline-block";
    const status = await api(`/api/users/${channelId}/subscribe`);
    applySubscriptionStatus(status);
    subscribeBtn.onclick = async () => {
      const method = subscribeBtn.dataset.subscribed === "true" ? "DELETE" : "POST";
      applySubscriptionStatus(await api(`/api/users/${channelId}/subscribe`, { method }));
    };
  }
}

async function loadVideos() {
  const page = await api(`/api/users/${channelId}/videos`);
  if (!page.content.length) {
    emptyState.style.display = "block";
    return;
  }
  grid.innerHTML = page.content.map((video) => `
    <a class="video-card" href="/video.html?id=${video.id}">
      <div class="video-thumb">&#9654;</div>
      <div class="video-card-body">
        <h3>${escapeHtml(video.title)}</h3>
        <div class="meta">${formatCount(video.views)} views &middot; ${formatDate(video.createdAt)}</div>
      </div>
    </a>
  `).join("");
}

loadChannel().catch((err) => {
  nameEl.textContent = "Failed to load channel";
  metaEl.textContent = err.message;
});
loadVideos();
