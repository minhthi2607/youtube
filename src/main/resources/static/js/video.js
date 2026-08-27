const params = new URLSearchParams(window.location.search);
const videoId = params.get("id");
const currentUser = getCurrentUser();

if (!videoId) {
  window.location.href = "/index.html";
}

const player = document.getElementById("player");
const titleEl = document.getElementById("video-title");
const metaEl = document.getElementById("video-meta");
const descriptionEl = document.getElementById("video-description");
const channelLink = document.getElementById("channel-link");
const subscriberCountEl = document.getElementById("subscriber-count");
const subscribeBtn = document.getElementById("subscribe-btn");
const likeBtn = document.getElementById("like-btn");
const dislikeBtn = document.getElementById("dislike-btn");
const likeCountEl = document.getElementById("like-count");
const dislikeCountEl = document.getElementById("dislike-count");
const commentForm = document.getElementById("comment-form");
const commentInput = document.getElementById("comment-input");
const commentsList = document.getElementById("comments-list");
const commentHeading = document.getElementById("comment-count-heading");

async function loadVideo() {
  const video = await api(`/api/videos/${videoId}`);
  player.src = `/api/videos/${videoId}/stream`;
  titleEl.textContent = video.title;
  metaEl.textContent = `${formatCount(video.views)} views · ${formatDate(video.createdAt)}`;
  descriptionEl.textContent = video.description || "";
  channelLink.textContent = video.uploader.displayName;
  channelLink.href = `/channel.html?id=${video.uploader.id}`;
  likeCountEl.textContent = formatCount(video.likeCount);
  dislikeCountEl.textContent = formatCount(video.dislikeCount);

  await loadSubscription(video.uploader.id);
  await refreshLikeStatus();
}

async function loadSubscription(channelId) {
  if (!currentUser || currentUser.id === channelId) {
    subscribeBtn.style.display = "none";
    const profile = await api(`/api/users/${channelId}`);
    subscriberCountEl.textContent = `${formatCount(profile.subscriberCount)} subscribers`;
    return;
  }
  subscribeBtn.style.display = "inline-block";
  const status = await api(`/api/users/${channelId}/subscribe`);
  applySubscriptionStatus(status);

  subscribeBtn.onclick = async () => {
    const method = subscribeBtn.dataset.subscribed === "true" ? "DELETE" : "POST";
    const newStatus = await api(`/api/users/${channelId}/subscribe`, { method });
    applySubscriptionStatus(newStatus);
  };
}

function applySubscriptionStatus(status) {
  subscriberCountEl.textContent = `${formatCount(status.subscriberCount)} subscribers`;
  subscribeBtn.dataset.subscribed = status.subscribed;
  subscribeBtn.textContent = status.subscribed ? "Subscribed" : "Subscribe";
  subscribeBtn.classList.toggle("btn", true);
  subscribeBtn.classList.toggle("primary", !status.subscribed);
}

async function refreshLikeStatus() {
  const status = await api(`/api/videos/${videoId}/likes`);
  likeCountEl.textContent = formatCount(status.likeCount);
  dislikeCountEl.textContent = formatCount(status.dislikeCount);
  likeBtn.classList.toggle("active", status.currentUserReaction === "LIKE");
  dislikeBtn.classList.toggle("active", status.currentUserReaction === "DISLIKE");
}

async function react(type) {
  if (!isLoggedIn()) {
    window.location.href = "/login.html";
    return;
  }
  const endpoint = type === "LIKE" ? "like" : "dislike";
  const status = await api(`/api/videos/${videoId}/${endpoint}`, { method: "POST" });
  likeCountEl.textContent = formatCount(status.likeCount);
  dislikeCountEl.textContent = formatCount(status.dislikeCount);
  likeBtn.classList.toggle("active", status.currentUserReaction === "LIKE");
  dislikeBtn.classList.toggle("active", status.currentUserReaction === "DISLIKE");
}

likeBtn.addEventListener("click", () => react("LIKE"));
dislikeBtn.addEventListener("click", () => react("DISLIKE"));

async function loadComments() {
  const page = await api(`/api/videos/${videoId}/comments?size=50`);
  commentHeading.textContent = `${page.totalElements} Comments`;
  commentsList.innerHTML = page.content.map(renderComment).join("") ||
    '<div class="empty-state">No comments yet. Be the first to comment.</div>';
  attachCommentActions();
}

function renderComment(comment) {
  const isOwner = currentUser && currentUser.id === comment.author.id;
  return `
    <div class="comment" data-id="${comment.id}">
      <div class="comment-meta"><strong>${escapeHtml(comment.author.displayName)}</strong> &middot; ${formatDate(comment.createdAt)}</div>
      <div class="comment-content">${escapeHtml(comment.content)}</div>
      ${isOwner ? `
        <div class="comment-actions">
          <button class="edit-btn">Edit</button>
          <button class="delete-btn">Delete</button>
        </div>` : ""}
    </div>
  `;
}

function attachCommentActions() {
  commentsList.querySelectorAll(".comment").forEach((el) => {
    const id = el.dataset.id;
    const editBtn = el.querySelector(".edit-btn");
    const deleteBtn = el.querySelector(".delete-btn");

    if (editBtn) {
      editBtn.addEventListener("click", async () => {
        const contentEl = el.querySelector(".comment-content");
        const newContent = prompt("Edit comment", contentEl.textContent);
        if (newContent === null || !newContent.trim()) return;
        await api(`/api/comments/${id}`, { method: "PUT", body: { content: newContent.trim() } });
        loadComments();
      });
    }
    if (deleteBtn) {
      deleteBtn.addEventListener("click", async () => {
        if (!confirm("Delete this comment?")) return;
        await api(`/api/comments/${id}`, { method: "DELETE" });
        loadComments();
      });
    }
  });
}

if (isLoggedIn()) {
  commentForm.style.display = "flex";
  commentForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    const content = commentInput.value.trim();
    if (!content) return;
    await api(`/api/videos/${videoId}/comments`, { method: "POST", body: { content } });
    commentInput.value = "";
    loadComments();
  });
}

loadVideo().catch((err) => {
  titleEl.textContent = "Failed to load video";
  metaEl.textContent = err.message;
});
loadComments().catch((err) => {
  commentsList.innerHTML = `<div class="empty-state">Failed to load comments: ${escapeHtml(err.message)}</div>`;
});
