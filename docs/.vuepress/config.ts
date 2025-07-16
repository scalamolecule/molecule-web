import { defineUserConfig } from "vuepress";

import theme from "./theme.js";


export default defineUserConfig({
  base: "/",
  lang: "en-US",
  title: "Molecule",
  description: "A docs demo for vuepress-theme-hope",
  theme,
  // head: [
  //   [
  //     "link",
  //     {
  //       rel: "stylesheet",
  //       href: "/css/custom.css",
  //     },
  //   ],
  // ],
});
