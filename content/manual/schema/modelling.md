---
date: 2015-01-02T22:06:44+01:00
title: "Modelling"
weight: 80
menu:
  main:
    parent: schema
---

# Modelling

A Datomic `schema` defines the set of `attributes` you can assign to `entities`. 
We organize `attributes` in `namespaces` to group related qualities:
 
![Schema](/img/DatomicElements1.png)

This makes it easier to overview our domain data structures. 

#### Namespace != Table
If coming from an sql background one might at first think of a namespace as
a table having columns (attributes). But this is not the case. An
entity in Datomic can associate values of attributes _from any namespace_:


![](/img/DatomicElements2.png)


This gives us great freedom to model our domain with more "loose" namespaces rather than "hardcoded things" as table definitions. Later we can compose entities grabbing specific attributes from various namespaces as needed.

### Relating namespaces

If we recall the Seattle domain then we have some attributes organized in 3 
related namespaces:

![Seattle Model](/img/DatomicElements3.png)

`Community.type`, `Community.orgtype` and `District.region` all have fixed 
enumerated values to choose from. And `Community.category` is the only 
many-cardinality attribute allowed to have multiple values.

