---
title: "Performance"
weight: 11
menu:
  main:
    parent: quick-start
    
up:   /manual/
prev: /manual/quick-start/introduction
next: /manual/setup
down: /manual/setup
---

# Automatic Query optimization

Molecule transparently optimize all queries sent to Datomic. 

Most selective
Clauses are automatically grouped first in the :where section of the Datomic query as per
the recommendation in [Datomic Best Practices](https://docs.datomic.com/on-prem/best-practices.html#most-selective-clauses-first). 

This brings dramatic performance gains of in some cases beyond 100x compared to 
un-optimized queries. The optimization happens automatically in the background 
so that you can focus entirely on your domain without concern for the optimal 
order of attributes in your molecules.


### Next...

[Setup molecule](/manual/setup/)
