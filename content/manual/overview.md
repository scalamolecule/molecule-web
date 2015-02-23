---
date: 2015-01-02T22:06:44+01:00
title: "Manual"
weight: 0
menu:
  main:
    parent: manual
---

# Molecule manual

This is a light-weight manual for Molecule.

For more in-depth examples see 
[core tests](https://github.com/scalamolecule/molecule/tree/master/coretest/src/test/scala/molecule) 
or 
[examples](https://github.com/scalamolecule/molecule/tree/master/examples/src/test/scala/molecule/examples)


- [Schema](/manual/schema) - Overview of schema creation
  - [Files](/manual/schema/files) - Schema file organization
  - [Definition](/manual/schema/definition) - Schema definition types and options
  - [Modelling](/manual/schema/modelling) - Some modelling advice
  - [Boilerplate](/manual/schema/boilerplate) - Boilerplate generated from schema
  - [Datomic](/manual/schema/datomic-schema) - Datomic schema transaction data generated
- [Setup](/manual/setup) - Before your first molecule
- [Insert](/manual/insert) - Data-molecule, Insert-molecule, Insert-molecule as template
- [Update/retract](/manual/update) - Updates/retraction/re-assertions explained
- [Query](/manual/query) - Overview of basic query tools
  - [Builder](/manual/query/builder) - Explict and tacet attribute values
  - [Types](/manual/query/types) - Inferred types from all molecules
  - [Expressions](/manual/query/expressions) - Equality, OR, Negation, Comparison, Fulltext search
  - [Aggregates](/manual/query/aggregates) - Min, max, count, countDistinct, sum, avg, median, variance, stddev, random, sample
  - [Parameterize](/manual/query/parameterize) - Cached Input-molecules for optimized queries
  
  
On our wish list (in no particular order):
  
- Indexes
- Optimization
- Relationships
- FAQ
- Troubleshooting
- Graph
  - Traversal
  - Hyperedges
- Recursive queries
- Schema queries