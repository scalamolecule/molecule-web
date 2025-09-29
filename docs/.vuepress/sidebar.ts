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
            text: 'Transaction',
            collapsible: true,
            expanded: false,
            prefix: "transaction",
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
                "parameter-binding",
                "aggregation",
                "sorting",
                "pagination",
                "subscription",
                "inspection",
                "raw-query",
            ],
        },
        {
            text: 'Relationships',
            collapsible: true,
            expanded: false,
            prefix: "relationships",
            children: [
                "many-to-one",
                "one-to-many",
                "many-to-many",
                "complex-example",
            ],
        },

//         {
//             text: 'Compare',
//             collapsible: true,
//             expanded: false,
//             prefix: "compare/",
//             children: [
//                 "overview",
//                 "plain-sql",
//                 "sql-dsl",
//                 "collection-dsl",
//                 "limitations",
//                 {
//                     text: 'Tutorials',
//                     collapsible: true,
//                     expanded: false,
//                     prefix: "tutorials/",
//                     children: [
//                         "slick",
//                         "slick-sql",
//                         "protoquill",
//                         "scalasql",
//                     ],
//                 },
//             ],
//         },

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
