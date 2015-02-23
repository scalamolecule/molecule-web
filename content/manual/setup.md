---
date: 2015-01-02T22:06:44+01:00
title: "Setup"
weight: 20
menu:
  main:
    parent: manual
    identifier: setup
---

# Database setup


To create a fresh in-memory Datomic database we simply pass an URI string to 
`Peer.createDatabase` (to be sure that this URI is not already populated we 
first delete it):

```scala
val uri = "datomic:mem://seattle"
Peer.deleteDatabase(uri)
Peer.createDatabase(uri)
implicit val conn = Peer.connect(uri)
```
We save the returned Datomic Connection as an implicit value so that our 
molecules can later issue queries against it.

