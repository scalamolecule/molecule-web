import {hopeTheme} from "vuepress-theme-hope";

import navbar from "./navbar.js";
import sidebar from "./sidebar.js";

export default hopeTheme({
  docsDir: "docs",

  pure: true,

  hostname: "https://www.scalamolecule.org/intro/",

  logo: "/static/img/logo/molecule-logo-m-75.png",
  repo: "scalamolecule/molecule",

  navbar,
  sidebar,

  pageInfo: false,
  editLink: false,
  contributors: false,
  breadcrumb: false,
  darkmode: "toggle",
  print: false,
  focus: false,

  favicon: "/static/favicon.ico",

  markdown: {
    imgLazyload: true,
    imgSize: true,
    codeTabs: true,
    tabs: true,
    component: true,
    include: true,
    mark: true,
    demo: true,
    mermaid: true,
//     mermaid: {
// //     'theme': 'base',
// //         'themeVariables': {
// //             'lineColor': '#c98500',
// //             'secondaryColor': '#c78a13',
// //             'tertiaryColor': '#fff'
// //         },
//     'lineColor': '#c98500',
//
//     },

    highlighter: {
      type: "shiki",
//       langs: [
//         "scala", "clojure", "sql", "bash",
//         "js", "md", "ts", "html", "css", "json",
//       ],
//       theme: {
//         light: "one-dark-pro",
//         dark: "one-dark-pro",
//       },
      lineNumbers: false,
      highlightLines: true,
    },
  },

  plugins: {
    icon: {
      assets: "fontawesome-with-brands",
    },
    readingTime: false,
  },

//   layout: {
//     // This will be passed as options to the header extractor used for TOC
//     getHeaders: {
//       levels: [2, 3],  // show only h2 and h3
//     },
//   },
});
