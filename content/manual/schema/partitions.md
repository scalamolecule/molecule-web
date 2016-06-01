---
date: 2015-01-02T22:06:44+01:00
title: "Partitions"
weight: 25
menu:
  main:
    parent: schema
---

# Defining Partitions in a Schema

From the [Datomic schema][schema] reference:

"All entities created in a database reside within a partition. Partitions group data together, providing locality of reference when executing queries across a collection of entities. In general, you want to group entities based on how you'll use them. Entities you'll often query across - like the community-related entities in our sample data - should be in the same partition to increase query performance. Different logical groups of entities should be in different partitions. Partitions are discussed in more detail in the Indexes topic."

In Molecule we can organize namespaces in partitions with objects:

### 

```scala
@InOut(0, 4)
object PartitionTestDefinition {

  object gen {
    trait Person {
      val name   = oneString
      val gender = oneEnum('male, 'female)
    }
    // ..more namespaces in the `gen` partition
  }

  object lit {
    trait Book {
      val title  = oneString
      val author = one[gen.Person]
      // To avoid attr/partition name clashes we can prepend the definition object name
      // (in case we would have needed an attribute named `gen` for instance)
      val editor = one[PartitionTestDefinition.gen.Person]
      val cat    = oneEnum('good, 'bad)
    }
    // ..more namespaces in the `lit` partition
  }
}
```
Here we have a `gen` (general) partition and a `lit` (litterature) partition. Each partition can contain as many namespaces as you want. This can be a way also to structure large domains conceptually. The partition name is prepended to the namespaces it contains. But we only need to use it for the first namespace starting a molecule:

```scala
lit_Book.title.cat.Author.name.gender.get === ...
```

Since `Author` is already defined as a related namespace we don't need to prepend the partition name there.

When we insert a `Person` the created entity will automatically be saved in the `gen` partition (or whatever we call it).



[datomic]: http://www.datomic.com
[seattle]: http://docs.datomic.com/tutorial.html
[schema]: http://docs.datomic.com/schema.html

[populate]: https://github.com/scalamolecule/wiki/Populate-the-database
[tutorial]: https://github.com/scalamolecule/wiki/Molecule-Seattle-tutorial