---
title: "Optimizations"
weight: 40
menu:
  main:
    parent: dev
    identifier: dev-optimizations
---

# Optimizations



## Save on compilation time

The simplest way of bringing in the molecule api is with this import
```scala
import molecule.datomic.api._
```
This enable molecules with any arity.

If your code starts to have huge numbers of molecules, you can save time on compilation by importing a specialized Molecule api that sets a threshold for how many attributes are allowed in a molecule. This minimizes the amount of implicits waiting in vain to serve you and can reduce compilation time. 


So, if your longest molecule in a code file has 7 attributes, then you could for instance set the api import to allow for 7 attributes:

```scala
import molecule.datomic.api.out7._

// The above specialized import would be enough to allow this or smaller molecules:
Person.e.firstName.lastName.age.Address.street.zip.city.get
```
Varying api arities can be set for each code file. 

The Molecule library has more than 1300 tests with easily 30-100 molecules each, and we have simply set the api import to match the highest molecule arity in each file.

If you use input molecules then you can add `inX` where X is how many [inputs](/manual/attributes/#input-molecules) (1, 2 or 3) your molecule expects:
```scala
import molecule.datomic.api.in2_out4._

// The above specialized import would be enough to allow this input molecule:
val personsOfAgeGender = m(Person.firstName.lastName.age_(?).gender_(?).Address.street.zip)
val male22 = personsOfAgeGender(22, "male").get
```


## Automatic Query optimization

Molecule transparently optimize all queries sent to Datomic.

Most selective Clauses are automatically grouped first in the :where section of the Datomic query as per the recommendation in [Datomic Best Practices](https://docs.datomic.com/on-prem/best-practices.html).

This brings dramatic performance gains of in some cases beyond 100x compared to un-optimized queries. The optimization happens automatically in the background so that you can focus entirely on your domain without concern for the optimal order of attributes in your molecules.

