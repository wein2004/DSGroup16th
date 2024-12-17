document.getElementById("search-form").addEventListener("submit", async function (e) {
    e.preventDefault();
    const query = document.getElementById("search-query").value;

    const response = await fetch(`http://localhost:8080/api/search?query=${encodeURIComponent(query)}`);
    const results = await response.json();

    const resultsContainer = document.getElementById("results");
    resultsContainer.innerHTML = "";

    if (results.error) {
        resultsContainer.innerHTML = `<p>Error: ${results.error}</p>`;
        return;
    }

    results.forEach(result => {
        const item = document.createElement("div");
        item.className = "result-item";

        item.innerHTML = `
            <h3><a href="${result.url}" target="_blank">${result.title}</a></h3>
            <p>Score: ${result.score.toFixed(2)}</p>
        `;

        resultsContainer.appendChild(item);
    });
});
