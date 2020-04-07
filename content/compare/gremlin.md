---
date: 2014-05-14T02:13:50Z
title: "Gremlin"
weight: 30
menu:
  main:
    parent: compare
---

# Gremlin vs. Molecule examples


Let's compare the queries/traversals in the [Gremlin Getting Started Tutorial](http://tinkerpop.apache.org/docs/current/tutorials/getting-started/)
with equivalent 
[Molecule queries](https://github.com/scalamolecule/molecule/blob/master/examples/src/test/scala/molecule/examples/gremlin/gettingStarted/Friends.scala).


For the examples we'll use the same "Modern" graph as the tutorial:

<br>

![](/img/compare/tinkerpop-modern.png)

<br>




## Insert data

Gremlin is untyped, so data is inserted directly in a generic way where "types" are created on-the-go:
```
Graph graph = TinkerGraph.open(); (1)
Vertex marko = graph.addVertex(T.label, "person", T.id, 1, "name", "marko", "age", 29); (2)
Vertex vadas = graph.addVertex(T.label, "person", T.id, 2, "name", "vadas", "age", 27);
Vertex lop = graph.addVertex(T.label, "software", T.id, 3, "name", "lop", "lang", "java");
Vertex josh = graph.addVertex(T.label, "person", T.id, 4, "name", "josh", "age", 32);
Vertex ripple = graph.addVertex(T.label, "software", T.id, 5, "name", "ripple", "lang", "java");
Vertex peter = graph.addVertex(T.label, "person", T.id, 6, "name", "peter", "age", 35);
marko.addEdge("knows", vadas, T.id, 7, "weight", 0.5f); (3)
marko.addEdge("knows", josh, T.id, 8, "weight", 1.0f);
marko.addEdge("created", lop, T.id, 9, "weight", 0.4f);
josh.addEdge("created", ripple, T.id, 10, "weight", 1.0f);
josh.addEdge("created", lop, T.id, 11, "weight", 0.4f);
peter.addEdge("created", lop, T.id, 12, "weight", 0.2f);
```

In Molecule we first define a schema. Since we won't use the `weight` properties yet, we'll start off defining a schema
without those:

```
object ModernGraph1Definition {

  trait Person {
    val name = oneString
    val age  = oneInt

    // Normal (uni-directional) reference
    val software = many[Software]

    // Bidirectional self-reference
    val friends = manyBi[Person]
  }

  trait Software {
    val name = oneString
    val lang = oneString
  }
}
```
When we compile our project with `sbt compile`, the sbt-molecule plugin will generate the necessary boilerplate code so
that we can enter typed data to populate the graph:

```
// Create database and save implicit connection
implicit val conn = recreateDbFrom(ModernGraph1Schema)

// Software
val List(lop, ripple) = Software.name.lang insert Seq(
  ("lop", "java"),
  ("ripple", "java")
) eids

// People and software created
val List(marko, vadas, josh, peter) = Person.name.age.software insert Seq(
  ("marko", 29, Set(lop)),
  ("vadas", 27, Set[Long]()),
  ("josh", 32, Set(lop, ripple)),
  ("peter", 35, Set(lop))
) eids

// Friendships
Person(marko).friends(vadas, josh).update
```
We'll use the variables with entity ids above for the coming examples.


## The first 5 minutes

Now we are ready to make queries.


### Generic vs domain language

Since Gremlin traverses generic edges and vertices it is also communicating intentions in a generic way like the following
 "query":
 
_Get the value of the `name` property on vertex with the unique identifier of "1"._

```
gremlin> g.V(1).values('name')
==>marko
```

Since Molecule uses the domain terms as custom building blocks directly, we can formulate our intentions with
the language of our domain directly:

_What is the Person name of entity `marko`?_

```
Person(marko).name.get.head === "marko"
```

### Edges / Entities

_Get the edges with the label "knows" for the vertex with the unique identifier of "1":_
```
gremlin> g.V(1).outE('knows')
==>e[7][1-knows->2]
==>e[8][1-knows->4]
```

_Marko's friends_

```
Person(marko).friends.get.head === Set(vadas, josh)
```

### Values

_Get the names of the people that the vertex with the unique identifier of "1" "knows"._
```
gremlin> g.V(1).out('knows').values('name')
==>vadas
==>josh
```

In Molecule we can jump from one namespace like `Person` to `Friends` since there's a relationship defined
 between the two. That way we can get Marko's referenced Friends entities:

```
Person(marko).Friends.name.get.head === Set("vadas", "josh")
```
Note though, that a namespace is not like a SQL table but rather just a meaningful prefix for a group of attributes.


### Expressions

_Get the names of the people vertex "1" knows who are over the age of 30._
```
gremlin> g.V(1).out('knows').has('age', gt(30)).values('name') //(7)
==>josh
```

_Names of Marko's friends over the age of 30._

```
Person(marko).Friends.name.age_.>(30).get === Set("josh")
```


## The next 15 minutes


_Find Marko in the graph_
```
gremlin> g.V().has('name','marko')
==>v[1]
```

Prepending the generic attribute `e` before an attribute finds the entity that it belongs to:

```
Person.e.name_("marko").get.head === marko
```
We also append an underscore `_` to the `name` attribute so that it becomes `name_`. In Molecule this makes
the attribute "tacit", or "silent", meaning that we don't need to return its value "marko" since we're already
applying it as a value that we expect the attribute to have.


### Traversing / relationships

_Gremlin has reached the "software that Marko created", he has access to the properties of 
the "software" vertex and you can therefore ask Gremlin to extract the value of the "name" property_
```
gremlin> g.V().has('name','marko').out('created').values('name')
==>lop
```

_What software did Marko create?_ - here we use a relationship again to get to the referenced Software entities and their names

```
Person.name_("marko").Software.name.get === Seq("lop")
```

### OR logic

_Find the "age" values of both "vadas" and "marko"_
```
gremlin> g.V().has('name',within('vadas','marko')).values('age')
==>29
==>27
```

To get both names we us Molecule's OR-logic by applying multiple values to an attribute. 
`name` should be either "marko" OR "vadas" and we can use various syntaxes:

```
Person.name_("marko", "vadas").age.get === Seq(27, 29)       // Vararg
Person.name_("marko" or "vadas").age.get === Seq(27, 29)     // `or`
Person.name_(Seq("marko", "vadas")).age.get === Seq(27, 29)  // Seq
```

### Aggregates

_Average age of "vadas" and "marko"_
```
gremlin> g.V().has('name',within('vadas','marko')).values('age').mean()
==>28.0
```

Molecule implements Datomics aggregate functions by applying the keyword `avg` to a number attribute like `age`

```
Person.name_("marko", "vadas").age(avg).get.head === 28.0
```

### Where / 2-step queries

_"Who are the people that marko develops software with?"_

```
gremlin> g.V().has('name','marko').out('created').in('created').values('name')
==>marko
==>josh
==>peter
```

It's idiomatic with Datomic to split such query and use the output of
one query as input for the next one:

```
// First find ids of software projects that marko has participated in
val markoSoftware = Person.name_("marko").software.get.head

// Then find names of persons that have participated in those projects
Person.name.software_(markoSoftware).get === Seq("peter", "josh", "marko")
```
<br>
Excluding marko from the result

```
gremlin> g.V().has('name','marko').as('exclude').out('created').in('created').where(neq('exclude')).values('name')
==>josh
==>peter
```

```
Person.name.not("marko").software_(markoSoftware).get === Seq("peter", "josh")
```


### Group

_"Group all the vertices in the graph by their vertex label"_

```
gremlin> g.V().group().by(label).by('name')
==>[software:[lop,ripple],person:[marko,vadas,josh,peter]]
```

Since Molecule is typed we would probably ask for specific `name` attribute values:

```
Person.name.get === Seq("peter", "vadas", "josh", "marko")
Software.name.get === Seq("ripple", "lop")
```


## Bonus molecules...

Let's ask a few more complex questions - a reminder of the graph could be useful here

<br>

![](/img/compare/tinkerpop-modern.png)

<br>

```
// Who knows young people?
// Since we save bidirectional references we get friendships in both directions:
Person.name.Friends.name.age.<(30).get === List(
  ("vadas", "marko", 29), // vadas knows marko who is 29
  ("josh", "marko", 29), // josh knows marko who is 29
  ("marko", "vadas", 27) // marko knows vadas who is 27
)

// How many young friends does the older people have?
Person.name.age.>=(30).friends(count).age_.<(30).get === List(
  ("josh", 32, 1) // josh (32) knows 1 young person (Marko, 29)
)
```

### More examples

See more Molecule query examples in 
the [Friends](https://github.com/scalamolecule/molecule/blob/master/examples/src/test/scala/molecule/examples/gremlin/gettingStarted/Friends.scala) tests...



## Edge property queries

The Gremlin tutorial doesn't actually use any of the `weight` properties so we 
made the [Knows](https://github.com/scalamolecule/molecule/blob/master/examples/src/test/scala/molecule/examples/gremlin/gettingStarted/Knows.scala) 
test suite to see how we can work with edge property molecules.