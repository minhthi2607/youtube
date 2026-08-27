const grid = document.getElementById("video-grid");
const emptyState = document.getElementById("empty");
const searchForm = document.getElementById("search-form");
const searchInput = document.getElementById("search-input");

const params = new URLSearchParams(window.location.search);
const initialQuery = params.get("q") || "";
searchInput.value = initialQuery;

async function loadVideos(query) {
  grid.innerHTML = "";
  emptyState.style.display = "none";
  try {
    const qs = query ? `?q=${encodeURIComponent(query)}` : "";
    const page = await api(`/api/videos${qs}`);
    if (!page.content.length) {
      emptyState.style.display = "block";
      return;
    }
    grid.innerHTML = page.content.map(renderCard).join("");
  } catch (err) {
    emptyState.textContent = "Failed to load videos: " + err.message;
    emptyState.style.display = "block";
  }
}

function renderCard(video) {
  return `
    <a class="video-card" href="/video.html?id=${video.id}">
      <div class="video-thumb">&#9654;</div>
      <div class="video-card-body">
        <h3>${escapeHtml(video.title)}</h3>
        <div class="meta">${escapeHtml(video.uploader.displayName)}</div>
        <div class="meta">${formatCount(video.views)} views &middot; ${formatDate(video.createdAt)}</div>
      </div>
    </a>
  `;
}

searchForm.addEventListener("submit", (e) => {
  e.preventDefault();
  const q = searchInput.value.trim();
  const url = q ? `/index.html?q=${encodeURIComponent(q)}` : "/index.html";
  window.history.pushState({}, "", url);
  loadVideos(q);
});

loadVideos(initialQuery);
