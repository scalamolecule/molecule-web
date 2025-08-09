
# Parameter binding

We can add value placeholders to attributes of the molecule so that we can later apply concrete values. This way, we can let the database cache the query structure for better performance and supply varying input parameters to the same query structure:

```scala
// Input molecule
val namesByAge = Person.name.age_(?).query

// Re-use the query
namesByAge(42).get ==> List("Bob")
namesByAge(36).get ==> List("Liz")
```


##### [<i class="fas fa-handshake" style="margin-right: 4px;"></i> Binding compliance tests](https://github.com/scalamolecule/molecule/tree/main/db/compliance/shared/src/test/scala/molecule/db/compliance/test/bind)