import {navbar} from "vuepress-theme-hope";

export default navbar([
    // "/",
    // "/portfolio",
    // "/demo/",
    // {
    //     // icon: "graphql",
    //     text: "Home",
    //     link: "home/",
    // },
    {
        icon: "database",
        text: "Database",
        link: "database/introduction",
        activeMatch: "/database/.*",
        // link: "docs/introduction",
        // activeMatch: "docs/.*",

        // prefix: "/guide/",
        // children: [
        //   {
        //     text: "Bar",
        //     // icon: "lightbulb",
        //     prefix: "bar/",
        //     children: ["baz", { text: "...", icon: "ellipsis", link: "#" }],
        //   },
        //   {
        //     text: "Foo",
        //     // icon: "lightbulb",
        //     prefix: "foo/",
        //     children: ["ray", { text: "...", icon: "ellipsis", link: "#" }],
        //   },
        // ],
    },
    // {
    //     // icon: "graphql",
    //     text: "GraphQL",
    //     link: "graphql/",
    // },
    // {
    //     text: "REST",
    //     link: "rest/",
    // },
    // {
    //     text: "Blog",
    //     link: "blog/",
    // },
    // {
    //     text: "Resources",
    //     link: "blog/",
    // },
]);
