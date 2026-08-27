requireLogin();

const form = document.getElementById("upload-form");
const errorBox = document.getElementById("error-box");
const submitBtn = document.getElementById("submit-btn");

form.addEventListener("submit", async (e) => {
  e.preventDefault();
  errorBox.classList.remove("visible");

  const fileInput = document.getElementById("file");
  if (!fileInput.files.length) return;

  const formData = new FormData();
  formData.append("title", document.getElementById("title").value.trim());
  formData.append("description", document.getElementById("description").value.trim());
  formData.append("file", fileInput.files[0]);

  submitBtn.disabled = true;
  submitBtn.textContent = "Uploading...";

  try {
    const video = await api("/api/videos", { method: "POST", body: formData, isForm: true });
    window.location.href = `/video.html?id=${video.id}`;
  } catch (err) {
    errorBox.textContent = err.message;
    errorBox.classList.add("visible");
    submitBtn.disabled = false;
    submitBtn.textContent = "Upload";
  }
});
