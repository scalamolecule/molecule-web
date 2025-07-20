import { defineUserConfig } from "vuepress";
import { googleAnalyticsPlugin } from '@vuepress/plugin-google-analytics'

import theme from "./theme.js";


export default defineUserConfig({
    base: "/",
    lang: "en-US",
    title: "Molecule",
    description: "A docs demo for vuepress-theme-hope",
    theme,

    head: [
        ['link', { rel: 'icon', href: '/favicon.ico' }],
    ],

    plugins: [
        googleAnalyticsPlugin({
            id: 'G-HWT03GY1MS',
        }),
    ],
});