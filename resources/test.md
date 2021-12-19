






With sbt you can do:
```scala
jsEnv := new JSDOMNodeJSEnv(
  JSDOMNodeJSEnv.Config()
    .withArgs(List("--dns-result-order=ipv4first")) 
)
```
According to https://github.com/scala-js/scala-js-js-envs/issues/12 this "hack" shouldn't be necessary when scala-js 1.8 is out.













// Scala news, discord


Happy to announce Molecule 1.0 (https://www.scalamolecule.org / https://github.com/scalamolecule/molecule), a non-blocking asynchronous domain-customizable database query language for Scala and Scala.js against the powerful Datomic (https://www.datomic.com) database.

Molecule makes database transactions and queries intuitive and type-safe. Here's a simple example of building molecules using the words of some custom Person domain data model:

```scala
for {
  _ <- Person.name("Bob").age(42).Address.street("Sesame Street 4").save
  _ <- Person.name.age.Address.street.get.map(_.head ==>
    ("Bob", 42, "Sesame Street 4")
  )
} yield ()
```
This gets automatically translated to transaction data and query at compile time and executed against the Datomic database at runtime with minimal overhead.

Since Molecule cross-compiles into both jvm bytecode and javascript, it can also be used for transparent RPCs where you can call the database directly from client/javascript code using molecules without any server code! Much like GraphQL, just with type-safe attribute-inferred code.

Molecule can fetch tuples (as shown above), property objects or json. The powerful atomic data model of Datomic allows Molecule to handle column-oriented (sql-like), graph and hierarchical data in a single system. Make time queries, nested queries, associative relationships, auditing meta data and much more.

Molecule is great for complex domain data models where you want compact intuitive database transaction/queries.

Give one of the sample projects a spin!

Cheers, 
Marc

















// Datomic forum...


Hi All,

After several years of work, I have now completed and released Molecule 1.0 ([web](https://www.scalamolecule.org), [github](https://github.com/scalamolecule/molecule)) - a non-blocking asynchronous domain-customizable database query language for Scala and Scala.js against the Datomic database.

Molecule makes Datomic transactions and queries intuitive and type-safe. Here's a simple example of building molecules using the words of some Person domain data model:

```
Person.name("Bob").age(42).Address.street("Sesame Street 4").save

Person.name.age.Address.street.get.map(_.head ==> 
  ("Bob", 42, "Sesame Street 4")
)
```
This gets automatically translated to transaction data and Datalog query at compile time and executed against the Datomic database at runtime with minimal overhead.

Since Molecule compiles into both Java byte code and JavaScript, it can also be used for transparent [RPCs](https://en.wikipedia.org/wiki/Remote_procedure_call) where you can call the database directly from client/javascript code using molecules without any server code. Much like GraphQL, just with type-safe attribute-inferred code.

Molecule can fetch typed tuples (as shown above), property objects or json and implements nearly all Datomic functionality, including nested hierarchies, associative relationships, time queries, transaction functions, reified transactions and more.

Both the Peer and Client apis are targeted.

Check out a [sample project](https://github.com/scalamolecule/molecule-samples) and give it a spin!

Cheers, Marc












Hi all,

Just released v0.16.1 of the meta-DSL library [Molecule](http://www.scalamolecule.org) ([Github](https://github.com/scalamolecule/molecule) | [Gitter](https://gitter.im/scalamolecule/Lobby) | [Docs](http://www.scalamolecule.org/api/molecule/)) that makes it intuitive and type-safe in Scala to access the [Datomic](https://www.datomic.com/on-prem.html) accumulate-only database where you can also query data back in time.

Molecule is a "meta-DSL" in that it generates boilerplate code from your initial schema definition so that 
you can intuitively use the tokens of your domain as the core building blocks of queries and transactions:

```
Await.result(
  // Asynchronously get Person data and related Address
  Person.name.age.Address.street.getAsync, 
  1 second
).map(_ ==> List(
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