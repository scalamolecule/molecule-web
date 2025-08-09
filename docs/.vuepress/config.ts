import { defineUserConfig } from "vuepress";
import theme from "./theme.js";

export default defineUserConfig({
    base: "/",
    lang: "en-US",
    title: "Scala Molecule",
    description: "A docs demo for vuepress-theme-hope",

    head: [
        ['link', { rel: 'icon', href: '/favicon.ico' }],

        // Google Analytics manual script
        ['script', {}, `
          window.dataLayer = window.dataLayer || [];
          function gtag(){dataLayer.push(arguments);}
          gtag('js', new Date());
          gtag('config', 'G-HWT03GY1MS');
        `],
        ['script', { async: true, src: 'https://www.googletagmanager.com/gtag/js?id=G-HWT03GY1MS' }],
    ],
    theme,

  extendsPage(page) {
    if (!page.frontmatter.toc) {
      page.frontmatter.toc = { levels: [2, 3] };
    }
  },
});
