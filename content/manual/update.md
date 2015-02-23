---
date: 2015-01-02T22:06:44+01:00
title: "Update/retract"
weight: 40
menu:
  main:
    parent: manual
    identifier: update
---

# Update/retract data


### Update

An "update" is a two-step process in Datomic:

1. Retract old fact
2. Assert new fact

Datomic doesn't overwrite data. "Retract" is a statement that says "this data is no longer current" which means that it won't turn up when you query for it _as of now_. If you query for it _as of before_ you will see it! 

Being able to see how data develops over time is a brillant core feature of Datomic. We don't need to administrate cumbersome historial changes manually. It's all built in to Datomic.


### Entities are updated

We need an entity id to update data.

```scala
// Grap entity id of the Belltown community
val belltownId = Community.e.name_("belltown").one

// Update name of the Belltown entity
Community(belltownId).name("Belltown 2").update
```
Molecule uses the belltown id to 

1. find the current `name` value ("Belltown") and retract that value
2. assert the new `name` value "Belltown 2"


### Cardinality-many values

#### Updating

Cardinality-many attributes have sets of values so we need to specify which of those values we want to update:

```scala
// Cardinality-many attribute value updated
Community(belltownId).category("news" -> "Cool news").update
```
Here we tell that the "news" value should change to "Cool news". As before the old value is retracted and the new value asserted so that we can go back in time and see what the values were before our update.

We can update several values in one go

```scala
Community(belltownId).category(
  "Cool news" -> "Super cool news",
  "events" -> "Super cool events").update
```

#### Adding

To add a value to the set of values a cardinality-many attriute can have we `add` the value:

```scala
Community(belltownId).category.add("extra category").update
```

#### Removing
We can remove a specific value from a set of values

```scala
Community(belltownId).category.remove("extra category").update
```

### Retract attribute value(s)


Applying nothing (empty parenthesises) finds and retract all values of an attribute

```scala
Community(belltownId).name().category().update
```

Note that all values of a cardinality many attribute are retracted this way.


### Retract whole entities

To delete a whole entity with all its attribute values we call `retract` on an entity id

```scala
belltownId.retract
```
Simple as that.