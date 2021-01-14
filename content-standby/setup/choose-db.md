---
title: "Choose Datomic db"
weight: 40
menu:
  main:
    parent: setup
---

# Choose Datomic database

Molecule transparently runs a unified Scala interface with only minor differences against the full matrix of Datomic database systems which from a Molecule perspective boils down to 3 systems:

<br>

{{< bootstrap-table "table table-bordered" >}}
System | Datomic Library | Download | License                 
:---   | :---     | :---     | :---                     
[Peer (On-Prem)](https://docs.datomic.com/on-prem/peer-getting-started.html)                                    | Peer   | [Free](https://my.datomic.com/downloads/free) <br> [Starter/Pro](https://www.datomic.com/get-datomic.html) | Free <br> [Perpetual use, 1-year updates](https://www.datomic.com/on-prem-eula.html)
[Peer-Server](https://docs.datomic.com/on-prem/peer-server.html)                                                | Client | [Starter/Pro](https://www.datomic.com/get-datomic.html) | [Perpetual use, 1-year updates](https://www.datomic.com/on-prem-eula.html)  
[Cloud](https://docs.datomic.com/cloud/index.html) / [Dev-Local](https://docs.datomic.com/cloud/dev-local.html) | Client | [Dev-tools](https://cognitect.com/dev-tools)                    | Email registration               
{{< /bootstrap-table >}}



## Datomic Peer

The Datomic Peer database 

### Free

The only caveat is that it hasn't been updated for quite some time although nearly all functionality is available, compared to the up-to-date Datomic Starter and Pro versions.

```scala
"com.datomic" % "datomic-free" % "0.9.5697"
```




#### Datomic Peer (On-Prem)
Here, we imported the free Datomic Peer library which is a natural choice to start with. The only caveat is that it hasn't been updated for quite some time although nearly all functionality is available, compared to the up-to-date Datomic Starter and Datomic Pro versions.

#### Datomic Starter / Datomic Pro (On-Prem)
the latest Datomic Starter or Datomic Pro versions that are up-to-date.


since it can be used as a local in-memory database .

You can also  with Datomic Peer, either

Later, you can decide to save data on disk or















Once you have defined your [Data Model](/setup/data-model), sbt needs to be set up to use Molecule and be told where your Data Model is.



[free]:https://my.datomic.com/downloads/free
[pro]:https://www.datomic.com/get-datomic.html
[dev]:https://cognitect.com/dev-tools