
const urlForm = document.getElementById("urlForm");
const originalUrlInput = document.getElementById("originalUrl");
const customAliasInput = document.getElementById("customAlias");
const shortenButton = document.getElementById("shortenButton");
const message = document.getElementById("message");
const result = document.getElementById("result");
const shortUrlLink = document.getElementById("shortUrl");
const copyButton = document.getElementById("copyButton");

urlForm.addEventListener("submit", async function (event) {
    event.preventDefault();

    message.textContent = "";
    result.hidden = true;
    shortenButton.disabled = true;
    shortenButton.textContent = "Creating...";

    const requestBody = {
        originalUrl: originalUrlInput.value.trim()
    };

    const alias = customAliasInput.value.trim();

    if (alias) {
        requestBody.customAlias = alias;
    }

    try {
        const response = await fetch("/api/urls", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(requestBody)
        });

        const data = await response.json().catch(() => ({}));

        if (!response.ok) {
            throw new Error(
                data.detail ||
                data.message ||
                data.error ||
                `Request failed with status ${response.status}`
            );
        }

        shortUrlLink.href = data.shortUrl;
        shortUrlLink.textContent = data.shortUrl;

        result.hidden = false;
        message.textContent = "Your short URL was created successfully.";
    } catch (error) {
        message.textContent = "Could not create URL: " + error.message;
    } finally {
        shortenButton.disabled = false;
        shortenButton.textContent = "Shorten URL";
    }
});

copyButton.addEventListener("click", async function () {
    try {
        await navigator.clipboard.writeText(shortUrlLink.href);
        message.textContent = "Short URL copied to clipboard!";
    } catch (error) {
        message.textContent =
            "Copy was blocked by the browser. Select and copy the link above.";
    }
});
