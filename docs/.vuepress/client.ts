import { defineClientConfig } from "vuepress/client";
import { defineMermaidConfig } from "vuepress-plugin-md-enhance/client";

defineMermaidConfig({
    // mermaid options here
    'theme': 'base',
    'themeVariables': {
        'lineColor': '#c98500',
        'secondaryColor': '#c78a13',
        'tertiaryColor': '#fff'
    },
    // flowchart: {
    //     htmlLabels: true, // ensure SVG rendering (not HTML)
    //     // edgeLabelPadding: 8 // ⬅️ increase for more padding around labels
    // }
});

export default defineClientConfig({
    // ...
});