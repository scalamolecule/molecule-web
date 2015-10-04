---
date: 2015-01-02T22:06:44+01:00
title: "Boilerplate"
weight: 86
menu:
  main:
    parent: schema
---

# Boilerplate code

SBT will ask the `MoleculeBoilerplate` file in our project folder to use our schema definition files as templates to generate a series of domain-specific boilerplate traits for us. 

With these generated we can then build intuitive molecules like

```scala
Community.name.Neighborhood.name.District.region
```

### Namespace traits

Each namespace is defined as a trait for each arity of our molecule. We start all molecules from a Namespace object like `Community`:

```scala
// (simplified...)
object Community extends Community_0 {
  def apply(e: Long): Community_0 = ???
}

trait Community_0  {
  val name          : Community_1 /* + more types... */ = ???
  val url           : Community_1 /* + more types... */ = ???
  val category      : Community_1 /* + more types... */ = ???
  
  def Neighborhood  : /* types... */ = ???
}

trait Community_1  {
  val name          : Community_2 /* + more types... */ = ???
  val url           : Community_2 /* + more types... */ = ???
  val category      : Community_2 /* + more types... */ = ???
  
  def Neighborhood  : Community_2 /* + more types... */ = ???
}

// etc...
```

### Increasing arity...

When we build `Community.name`, the `name` field of `Community_0` points on to `Community_1` since the arity of our molecule is now 1. We expect `Community.name` to return values for 1 attribute.

All fields are assigned the `???` "unimplemented method" which will be implemented with a Scala macro when we compile our project during development (not the `sbt compile` we do initially).

### In/Out arities

We annotate schema definition traits with `@InOut(x, y)` to tell Molecule the arity of inputs and outputs we expect molecules to be able to accept.

```scala
@InOut(3, 8) // <-- In-arity: 3, Out-arity: 8
object SeattleDefinition {
  // namespaces...
}
```
This will generate up to a `Community_8` but no further since we don't expect in this case to build molecules with more than 8 attributes.

There's no reason to create more boilerplate classes than necessary. We can always adjust the number up or down and recompile with `sbt compile` to re-generate the domain classes if our schema/requirements change.

### Input/Output molecules

An input molecule like `Community.name(?).url(?)` for instance awaits 2 inputs. For now the 
maximum is 3. Given that input values can be expressions like `name("John" or "Lisa")` it seems 
unlikely that we will need to receive input for much more than 3 attributes at a time.

Outputs are the number of attributes we can build a molecule of. `Community.name.url.Neighborhood.name` 
for instance has 3 attributes (in 2 namespaces). We need to be able to return tuples of values from 
molecules so we can't exceed Scala's arity limit of 22 for tuples.
