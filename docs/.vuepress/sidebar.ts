import {sidebar} from "vuepress-theme-hope";

export default sidebar({
    "/database/": [
        "introduction",
        "quick-start",
        // "tutorial",

        // "database":[],

        {
            text: 'Setup',
            collapsible: true,
            expanded: false,
            prefix: "setup",
            children: [
                "sbt-setup",
                "domain-structure",
                "validation",
                "db-setup",
            ],
        },
        // {
        //     text: 'Example projects',
        //     collapsible: true,
        //     expanded: false,
        //     prefix: "examples",
        //     children: [
        //         "pet-clinic",
        //         "real-world",
        //         "todo",
        //     ],
        // },

        {
            text: 'Transact',
            collapsible: true,
            expanded: false,
            prefix: "transact",
            children: [
                "save",
                "insert",
                "update",
                "delete",
                "raw-transact",
                // "migration",
            ],
        },
        {
            text: 'Query',
            collapsible: true,
            expanded: false,
            prefix: "query",
            children: [
                "attributes",
                "filters",
                "filterAttr",
                "binding",
                "aggregation",
                "relationships",
                "sorting",
                "pagination",
                "subscription",
                "inspection",
                "raw-query",
            ],
        },

        {
            text: 'Compare',
            collapsible: true,
            expanded: false,
            prefix: "compare/",
            children: [
                "overview",
                "slick",
                "slick-sql",
                "protoquill",
                "scalasql",

                // "sql-dsl",
                // "sql-raw",
                // "anorm",
                // "doobie",
                // "prisma",
                // "quill",
                // "scalalikejdbc",
                // "scalasql",
                // "scalasql2",
                // "skunk",
                // "slick",
                // "squeryl",
                // {
                //     text: 'Tutorials',
                //     collapsible: true,
                //     expanded: false,
                //     prefix: "tutorials/",
                //     children: [
                //         "scalasql",
                //         "slick",
                //     ],
                // },
                // "zio-sql",
            ],
        },

        // {
        //     text: 'Cookbook',
        //     collapsible: true,
        //     expanded: false,
        //     prefix: "cookbook",
        //     children: [
        //         "testing",
        //         "optimization",
        //         "friends-of-friends",
        //     ],
        // },
        //
        // {
        //     text: 'FAQ',
        //     collapsible: true,
        //     expanded: false,
        //     prefix: "faq",
        //     children: [
        //         "testing",
        //         "optimization",
        //         "friends-of-friends",
        //     ],
        // },
        // {
        //     text: 'Integration JVM',
        //     collapsible: true,
        //     expanded: false,
        //     prefix: "integration-jvm",
        //     children: [
        //         "play",
        //         "react",
        //     ],
        // },
        // {
        //     text: 'Integration JS',
        //     collapsible: true,
        //     expanded: false,
        //     prefix: "integration-js",
        //     children: [
        //         "laminar",
        //         "tyrian",
        //         "react",
        //     ],
        // },
        // {
        //     text: 'Developer',
        //     collapsible: true,
        //     expanded: false,
        //     prefix: "developer",
        //     children: [
        //         "how-does-it-work",
        //         "philosophy",
        //         "compose-structure",
        //         "native",
        //     ],
        // },
    ],
});
