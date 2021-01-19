---
title: "Database setups"
weight: 50
menu:
  main:
    parent: setup
    identifier: setup-examples
---

# Database setups


Datomic has a flexible and powerful palette of options to configure its database systems.

From a Molecule perspective these can be summarized in a configuration matrix of 3 database systems on the left and 3 types of downloads at the top:  

{{< bootstrap-table "table table-bordered" >}}
|                 | [Free](https://my.datomic.com/downloads/free)       | [Starter/Pro](https://www.datomic.com/get-datomic.html) | [Dev-Tools](https://cognitect.com/dev-tools) |
| :-                    | :-         | :-          | :-        |
| **[Peer](https://docs.datomic.com/on-prem/peer-getting-started.html)**              | [mem](/setup/examples/datomic-peer-free-mem/) / [free](http://localhost:1313/setup/examples/datomic-peer-free-free/) | [mem](http://localhost:1313/setup/examples/datomic-peer-pro-mem/) / [dev](http://localhost:1313/setup/examples/datomic-peer-pro-dev/)   |           |
| **[Peer Server](https://docs.datomic.com/on-prem/peer-server.html)**       |            | [mem](http://localhost:1313/setup/examples/datomic-peerserver-mem/) / [dev](http://localhost:1313/setup/examples/datomic-peerserver-dev/)   |           |
| **[Dev Local (Cloud)](https://docs.datomic.com/cloud/dev-local.html)** |            |             | [dev-local](http://localhost:1313/setup/examples/datomic-devlocal/) |
{{< /bootstrap-table >}}


The protocols links (_free_, _mem_, _dev_) point to descriptions of various combinations of database system/download that you also see in the side menu. 


### Database setup projects on Github

All the database setup sample projects are contained in the [molecule-database-setups](https://github.com/scalamolecule/molecule-sample-projects) github project that your can

```
git clone https://github.com/scalamolecule/molecule-database-setups.git
```

Each project has a readme with setup instructions and a test that you can run to confirm that the setup works.

Feel free to use the example projects as starting points or templates for your own projects.




### Other Storage Services

Several more protocols exist for various [Storage Services](https://docs.datomic.com/on-prem/storage.html) that Datomic uses to save data to disk. We refer to the Datomic  documentation to setup these.



