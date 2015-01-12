---
date: 2015-01-02T22:06:44+01:00
title: "Modelling"
weight: 80
menu:
  main:
    parent: manual
---

# Modelling

A Datomic `schema` defines the set of `attributes` you can assign to `entities`. 
We organize `attributes` in `namespaces` to group related qualities:
 
![Schema](/molecule/img/DatomicElements1.png)

This makes it easier to overview our domain data structures. 

#### Schema != Table
If coming from an sql background one might at first think of a namespace as
a table having columns (attributes). But this is not the case. An
entity in Datomic can associate values of attributes _from any namespace_:


![](/molecule/img/DatomicElements2.png)


This gives us great freedom to model our domain with more "loose" namespaces rather than "hardcoded things" as table definitions. Later we can compose entities grabbing specific attributes from various namespaces as needed.