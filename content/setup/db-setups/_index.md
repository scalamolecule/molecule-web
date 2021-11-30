---
title: "Database setups"
weight: 50
menu:
  main:
    parent: setup
    identifier: setup-db-setups
---

# Database setups


Datomic has a flexible and powerful palette of options to configure its database systems.

From a Molecule perspective these can be summarized in a configuration matrix of 3 database systems on the left and 3 types of downloads at the top:  

{{< bootstrap-table "table table-bordered" >}}
| | [Free](https://my.datomic.com/downloads/free) | [Starter/Pro](https://www.datomic.com/get-datomic.html) | [Dev-Tools](https://cognitect.com/dev-tools) |
| :- | :- | :- | :- |
| **[Peer](https://docs.datomic.com/on-prem/peer-getting-started.html)** | [mem](/setup/db-setups/datomic-peer-free-mem/) / [free](/setup/db-setups/datomic-peer-free-free/) | [mem](/setup/db-setups/datomic-peer-pro-mem/) / [dev](/setup/db-setups/datomic-peer-pro-dev/) | |
| **[Peer Server](https://docs.datomic.com/on-prem/peer-server.html)**   | | [mem](/setup/db-setups/datomic-peerserver-mem/) / [dev](/setup/db-setups/datomic-peerserver-dev/) | |
| **[Dev Local (Cloud)](https://docs.datomic.com/cloud/dev-local.html)** | | | [dev-local](/setup/db-setups/datomic-devlocal/) |
{{< /bootstrap-table >}}


The protocols links (_free_, _mem_, _dev_) point to descriptions of various combinations of database system/download that you also see in the side menu. 


## Server Database setups

All the database setup sample projects are contained in the [molecule-database-setups](https://github.com/scalamolecule/molecule-database-setups) github project that your can

```
git clone https://github.com/scalamolecule/molecule-database-setups.git
```

Each project has a readme with setup instructions and a test that you can run to confirm that the setup works.

Feel free to use the example projects as starting points or templates for your own projects.




### Other Storage Services

Several more protocols exist for various [Storage Services](https://docs.datomic.com/on-prem/storage.html) that Datomic uses to save data to disk. We refer to the Datomic  documentation to setup these.



## Client Database setups

For examples of setting up sbt for a Scala.js project, please see the rpc projects in the [molecule sample projects repo](https://github.com/scalamolecule/molecule-samples). Most sbt settings will be equal to the various following Server setups examples.


### Next

Sbt database setup for [Datomic Peer free, mem](/setup/db-setups/datomic-peer-free-mem)



