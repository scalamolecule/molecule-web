// import {defineUserConfig} from "vuepress";
import {hopeTheme} from "vuepress-theme-hope";

import navbar from "./navbar.js";
import sidebar from "./sidebar.js";


export default hopeTheme({
    docsDir: "docs",

    // no page transitions
    pure: true,

    hostname: "https://www.scalamolecule.org/intro/",

    iconAssets: "fontawesome-with-brands",

    // logo: "https://theme-hope-assets.vuejs.press/logo.svg",
    logo: "/static/img/logo/molecule-logo-m-75.png",

    repo: "scalamolecule/molecule",


    // navbarLayout: {
    //     start: ["Brand"],
    //     // center: [],
    //     center: ["Links"],
    //     end: [
    //         // "Links",
    //         "Search",
    //         "Repo",
    //         "Outlook"
    //     ]
    // },
    navbar,

    pageInfo: false,
    // fullscreen: true,
    editLink: false,
    contributors: false,

    // sidebar
    sidebar,
    // sidebar: false,

    // headerDepth: 1,
    // toc: false,

    // navbarTitle: "",

    darkmode: "toggle",
    print: false,

    // custom: true,

    // favicon: "/favicon.ico",
    favicon: "/static/favicon.ico",
    // favicon: "/static/img/logo/favicon.png",


    // footer: "Default footer",
    //
    // displayFooter: true,

    // encrypt: {
    //   config: {
    //     "/demo/encrypt.html": ["1234"],
    //   },
    // },
    //
    // metaLocales: {
    //   editLink: "Edit this page on GitHub",
    // },

    breadcrumb: false,


    plugins: {


        // activeHeaderLinks: {
        //     // options
        //     delay: 2000,
        // },


        shiki: {
            // shikiPlugin: {
            // theme: "ayu-dark",
            langs: ["scala", "clojure", "sql", "bash", "js", "md", "ts", "html", "css", "json"],
            defaultLang: "scala",

            lineNumbers: false,
            highlightLines: true,

            themes: {
                // light: 'ayu-dark',
                // light: 'material-theme-lighter',
                //     light: 'solarized-light',
                // light: 'everforest-light',
                // light: 'github-light',
                // light: 'github-light-default',
                // light: 'github-light-high-contrast',
                // light: 'one-light',
                // light: 'vitesse-light',
                light: 'one-dark-pro',

                // dark: "ayu-dark",
                dark: 'one-dark-pro',
                // dark: 'material-theme-lighter',
                // dark: 'material-theme-palenight',
                // dark: 'slack-dark',
                // dark: 'aurora-x',
                // dark: 'material-theme-ocean',
                // dark: 'solarized-dark',
                // dark: 'solarized-light',
                //
                // dark: 'andromeeda',
                // dark: 'catppuccin-frappe',
                // dark: 'catppuccin-latte',
                // dark: 'catppuccin-macchiato',
                // dark: 'catppuccin-mocha',
                // dark: 'dark-plus',
                // dark: 'dracula',
                // dark: 'dracula-soft',
                // dark: 'everforest-dark',
                // dark: 'everforest-light',
                // dark: 'github-dark',
                // dark: 'github-dark-default',
                // dark: 'github-dark-dimmed',
                // dark: 'github-dark-high-contrast',
                // dark: 'github-light',
                // dark: 'github-light-default',
                // dark: 'github-light-high-contrast',
                // dark: 'houston',
                // dark: 'laserwave',
                // dark: 'light-plus',
                // dark: 'material-theme',
                // dark: 'material-theme-darker',
                // dark: 'min-dark',
                // dark: 'min-light',
                // dark: 'monokai',
                // dark: 'night-owl',
                // dark: 'nord',
                // dark: 'one-light',
                // dark: 'plastic',
                // dark: 'poimandres',
                // dark: 'red',
                // dark: 'rose-pine',
                // dark: 'rose-pine-dawn',
                // dark: 'rose-pine-moon',
                // dark: 'slack-ochin',
                // dark: 'snazzy-light',
                // dark: 'synthwave-84',
                // dark: 'tokyo-night',
                // dark: 'vesper',
                // dark: 'vitesse-black',
                // dark: 'vitesse-dark',
                // dark: 'vitesse-light',
            },
        },

        readingTime: false,
        // activeHeaderLinks: false,


        // // Note: This is for testing ONLY!
        // // You MUST generate and use your own comment service in production.
        // comment: {
        //   provider: "Giscus",
        //   repo: "vuepress-theme-hope/giscus-discussions",
        //   repoId: "R_kgDOG_Pt2A",
        //   category: "Announcements",
        //   categoryId: "DIC_kwDOG_Pt2M4COD69",
        // },

        // components: {
        //   components: ["Badge", "VPCard"],
        // },

        // activeHeaderLinks: {
        //     delay: 2000,
        //
        // },

        // All features are enabled for demo, only preserve features you need here
        mdEnhance: {


            // align: true,
            // attrs: true,
            codetabs: true,
            component: true,
            demo: true,
            // figure: true,
            imgLazyload: true,
            imgSize: true,
            include: true,
            mark: true,
            // plantuml: true,
            // spoiler: true,
            // stylize: [
            //     {
            //         matcher: "Recommended",
            //         replacer: ({tag}) => {
            //             if (tag === "em")
            //                 return {
            //                     tag: "Badge",
            //                     attrs: {type: "tip"},
            //                     content: "Recommended",
            //                 };
            //         },
            //     },
            // ],
            // sub: true,
            // sup: true,
            tabs: true,
            // tasklist: true,
            // vPre: true,

            // install chart.js before enabling it
            // chart: true,

            // insert component easily

            // install echarts before enabling it
            // echarts: true,

            // install flowchart.ts before enabling it
            // flowchart: true,

            // gfm requires mathjax-full to provide tex support
            // gfm: true,

            // install katex before enabling it
            // katex: true,

            // install mathjax-full before enabling it
            // mathjax: true,

            // install mermaid before enabling it
            mermaid: true,

            // playground: {
            //   presets: ["ts", "vue"],
            // },

            // install reveal.js before enabling it
            // revealJs: {
            //   plugins: ["highlight", "math", "search", "notes", "zoom"],
            // },

            // install @vue/repl before enabling it
            // vuePlayground: true,

            // install sandpack-vue3 before enabling it
            // sandpack: true,
        },
    },
});
