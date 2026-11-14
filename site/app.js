'use strict';

const repo = 'Lanzkila/KirinYT';
const api = 'https://api.github.com/repos/' + repo;

const formatCount = (value) => {
  const number = Number(value) || 0;
  if (number >= 1e6) return (number / 1e6).toFixed(1) + 'M';
  if (number >= 1e3) return (number / 1e3).toFixed(1) + 'K';
  return String(number);
};

const setText = (id, value) => {
  const element = document.getElementById(id);
  if (element) element.textContent = value;
};

async function fetchJson(url) {
  const controller = new AbortController();
  const timeoutId = window.setTimeout(() => controller.abort(), 12000);

  try {
    const response = await fetch(url, {
      headers: { Accept: 'application/vnd.github+json' },
      cache: 'no-store',
      signal: controller.signal
    });

    if (!response.ok) {
      throw new Error('GitHub API returned HTTP ' + response.status);
    }

    return await response.json();
  } finally {
    window.clearTimeout(timeoutId);
  }
}

function updateRelease(release) {
  if (!release || release.draft) return;

  const name = release.name || release.tag_name || 'Latest KirinYT release';
  setText('releaseName', name);

  if (release.published_at) {
    const published = new Date(release.published_at);
    if (!Number.isNaN(published.getTime())) {
      setText('releaseDate', 'Published ' + published.toLocaleDateString(undefined, {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      }));
    }
  }

  if (release.html_url) {
    const releaseLink = document.getElementById('releaseLink');
    const downloadButton = document.getElementById('downloadBtn');
    if (releaseLink) releaseLink.href = release.html_url;
    if (downloadButton) downloadButton.href = release.html_url;
  }
}

async function loadRepositoryStats() {
  try {
    const info = await fetchJson(api);
    setText('stars', formatCount(info.stargazers_count));
    setText('forks', formatCount(info.forks_count));
  } catch (error) {
    console.warn('KirinYT repository statistics could not be refreshed:', error);
  }
}

async function loadReleaseData() {
  try {
    const releases = await fetchJson(api + '/releases?per_page=30');

    if (!Array.isArray(releases)) {
      throw new Error('GitHub releases response was not a list');
    }

    const totalDownloads = releases.reduce((total, release) => {
      const assets = Array.isArray(release.assets) ? release.assets : [];
      return total + assets.reduce((sum, asset) => sum + (Number(asset.download_count) || 0), 0);
    }, 0);

    setText('downloads', formatCount(totalDownloads));

    const latest = releases.find((release) => !release.draft);
    if (latest) {
      updateRelease(latest);
    } else {
      setText('releaseName', 'No published releases yet');
      setText('releaseDate', 'KirinYT release information is not available yet.');
    }
  } catch (error) {
    console.warn('KirinYT release list could not be refreshed:', error);

    // Keep the HTML fallback visible and try the stable latest-release endpoint.
    try {
      const latest = await fetchJson(api + '/releases/latest');
      updateRelease(latest);
    } catch (fallbackError) {
      console.warn('KirinYT latest-release fallback also failed:', fallbackError);
    }
  }
}

function updateCopyrightYear() {
  setText('copyrightYear', new Date().getFullYear());
}

updateCopyrightYear();
Promise.allSettled([loadRepositoryStats(), loadReleaseData()]);
