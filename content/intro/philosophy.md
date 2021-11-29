---
title: "Philosophy"
weight: 20
menu:
  main:
    parent: intro
---


# The Molecular Domain Model

Traditionally, domain classes are too often:

- **Dumb** crud-containers of domain data being populated and consumed back and forth.
- **Rigid** abstractions being bad fits for complex business processes.
- **Bloated** with more and more properties and methods serving business processes scattered all over.
- **Fragmented** in ever more sub-classed hierarchies or hard-to-overview complex design pattern arrangements.

Too much abstraction can easily make it hard to reason about _what the program does_ from a process/use case level.

>A "Person" can be endlessly many things in various contexts and doesn't like being trapped in any domain class.

The impedance mismatch between fluent business processes and rigid class-based data abstractions pollutes our mental model about our domain. The atomic attribute building blocks of molecules allow us to model complex and specific data structures that exactly match our mental model in any context.


## Filling the gap

GraphQL filled the impedance mismatch gap by delivering flexible data structures with a more intuitive query language. The fact that the industry has adopted it so widely speaks volume of the strong need and value of a flexible data layer.


Molecule fills the same gap by offering a similar intuitive way of handling flexible data structures. 

Instead of a separate query language, Molecule offers you to use your domain Data Model attributes directly as your custom query language with plain Scala.



## Retrieve exactly the data you need

Molecules can match the exact data needs of business processes, from case to case.

A "domain model" can be seen semantically as 3 layers, each supporting the layer above:

- Business processes of interacting entities
- Entities (molecules) of attributes 
- Atomic attributes - our _Data Model_


Attributes are the slowest changing part of a domain model since the intrinsic definitions of say an "atomic" `firstName` attribute will rarely, if ever, change. We define those attributes in a [Data model](/setup/data-model/). 


In the layer above, groups of attributes form entity data-structures, or "molecules", that are tailored to the _exact_ needs of business processes of the top layer. 

Molecules allows this more fluid and flexible "Molecular Domain Modeling" that can be tailored to the exact requirements of high-level processes in each context/use case. 



### Next

[Building blocks...](/intro/building-blocks)
