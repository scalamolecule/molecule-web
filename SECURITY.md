### Security Alert Acknowledgement

Several moderate security alerts (e.g. Vite `server.fs.deny` bypasses, esbuild dev server exposure, DOMPurify XSS risks) have been reported by automated scanners. These alerts are **not relevant in this project** because:

- The site is built **statically** with VuePress.
- No Vite/esbuild dev server is exposed publicly.
- DOMPurify (if used) is only applied to **trusted, developer-authored content**.
- Only static files are deployed to GitHub Pages.

We monitor relevant dependency updates and will upgrade when compatibility improves.