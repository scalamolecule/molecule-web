// import { defineClientConfig } from "vuepress/client";
//
// export default defineClientConfig({
//     enhance() {
//         if (typeof window !== "undefined") {
//             const savedMode = localStorage.getItem("vuepress-theme-hope-scheme");
//             if (!savedMode) {
//                 document.documentElement.classList.add("dark");
//                 localStorage.setItem("vuepress-theme-hope-scheme", "dark");
//             }
//         }
//     },
// });


// export interface GetHeadersOptions {
//   /**
//    * The selector of the headers.
//    *
//    * @default "#markdown-content >  h1, #markdown-content > h2, #markdown-content > h3, #markdown-content > h4, #markdown-content > h5, #markdown-content > h6, [vp-content] > h2"
//    */
//   selector?: string;
//   /**
//    * Ignore specific elements within the header, should be an array of `CSS Selector`
//    *
//    * @default [".vp-badge", ".vp-icon"]
//    */
//   ignore?: string[];
//   /**
//    * The levels of the headers.
//    *
//    * `1` to `6` for `<h1>` to `<h6>`
//    *
//    * - `false`: No headers.
//    * - `number`: only headings of that level will be displayed.
//    * - `[number, number]: headings level tuple, where the first number should be less than the second number, for example, `[2, 4]` which means all headings from `<h2>` to `<h4>` will be displayed.
//    * - `deep`: same as `[2, 6]`, which means all headings from `<h2>` to `<h6>` will be displayed.
//    *
//    * @default "deep"
//    */
//   levels?: HeaderLevels;
// }