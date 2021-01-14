Hi all,

Just released v0.16.1 of the meta-DSL library [Molecule](http://www.scalamolecule.org) ([Github](https://github.com/scalamolecule/molecule) | [Gitter](https://gitter.im/scalamolecule/Lobby) | [Docs](http://www.scalamolecule.org/api/molecule/)) that makes it intuitive and type-safe in Scala to access the [Datomic](https://www.datomic.com/on-prem.html) accumulate-only database where you can also query data back in time.

Molecule is a "meta-DSL" in that it generates boilerplate code from your initial schema definition so that 
you can intuitively use the tokens of your domain as the core building blocks of queries and transactions:

```
Await.result(
  // Asynchronously get Person data and related Address
  Person.name.age.Address.street.getAsync, 
  1 second
) === List(
  ("Lisa", 20, "Broadway"),
  ("John", 22, "Fifth Avenue")
)
```

Compare Molecule syntax with: [SQL](http://www.scalamolecule.org/compare/sql/), [Slick](http://www.scalamolecule.org/compare/sql/slick/), [Datalog](http://www.scalamolecule.org/compare/datomic/), [Gremlin](http://www.scalamolecule.org/compare/gremlin/). 


Hope you'll give it a spin! Any feedback is welcome.

Cheers,

Marc Grue

---------

Highlights of [Molecule](http://www.scalamolecule.org):

- Intuitively use your domain terms as query tokens
- Fully implemented Sync/Async APIs
- Model any RDBMS, Graph, Document, KV-store etc.
- Datalog queries for Datomic built at compile time
- Nested queries of hierarchical data
- Atomic transaction functions
- Add meta data to transactions
- Cross-cutting associative relationships
- Developed and tested over the last 4 years (reached some maturity)
- Fully documented ([Docs](http://www.scalamolecule.org/api/molecule/))


Highlights of [Datomic](https://www.datomic.com/on-prem.html):

- Fully transactional, cloud-ready, distributed database
- Elastic scaling
- Built-in caching - extremely low latency
- Built-in auditing - query entire history of data
- ACID-compliant transactions
- Flexible and sound Data Model