---
title: "Relationships"
weight: 60
menu:
  main:
    parent: code
    identifier: relationships
up:   /manual/entities
prev: /manual/entities
next: /manual/relationships/card-one
down: /manual/crud
---

# Relationships

[Tests...](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/ref/Relations.scala)


To understand how Molecule treats relationships it is valuable to get an idea of how they work in Datomic.


### Ref attributes connect entities

A relationship in Datomic is simply when a ref attribute of entity A has an entity B id value. Then there is a relationship from A to B!

In the following example, entity `101` has a ref attribute `:Person/home` with a value `102`. That makes the relationship between entity `101` and entity `102`, or that Fred has an Address:

![](/img/relationships/ref.jpg)

We can illustrate the same data as two entities (groups of facts with a shared entity id) with the link between them:

![](/img/relationships/entityref.jpg)

[Card-one relationships](/manual/relationships/card-one/)...


### Card-many ref attributes

Since datomic has cardinality many attributes, ref attributes can also be of cardinality many.

We could for instance have a classic Order/LineItems card-many example where the `:Order/items` card-many ref attribute has two LineItem entity id values `102` and `103`:

![](/img/relationships/entityrefs.jpg)


## Card-one relationships

[Tests...](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/ref/Relations.scala)

In Molecule we model a cardinality-one relationship in our [schema definition file](/manual/schema/) with the `one[<RefNamespace>]` syntax:

```
object YourDomainDefinition {
  trait Person {
    val name = oneString
    val home = one[Addr]
  }
  trait Addr {
    val street = oneString
    val city   = oneString
  }
}
```
The ref attribute `home` is a card-one relationship to namespace `Addr`. When our schema is then translated to Molecule boilerplate code our `home` ref attribute is accessible as a value by using its lower case name (`home` instead of `Home`):

```
Person.name.home.get === List(("Fred", 102))
```
This can be practical when we want to get a related entity id like `102` in this case.


### Ref namespace

More often though we want to collect the values of the referenced entity attributes. Molecule therefore also creates an Uppercase method `Home` that allow us to add attributes from the `Home` (`Addr`) namespace:

```
Person.name.Home.street.city.get.head === ("Fred", "Baker St. 7", "Boston")
```
Describing the relationship we can simply say that a "Person has an Address". It's important to understand though that the namespace names `Person` and `Addr` are not like SQL tables. So there is no "instance of a Person" but rather _"an **entity** with some Person attribute values"_ (and maybe other attribute values). It's the entity id that tie groups of facts/attribute values together.


### One-to-one or one-to-many

If Fred is living by himself on Baker St. 7 we could talk about a one-to-one relationship between him and his address.

But if several people live on the same address each person entity would reference the same address entity and we would then see each persons relation to the address as a one-to-many relationship since other persons also share the address:

```
Person.name.home.get === List(
  ("Fred", 102),
  ("Lisa", 102),
  ("Mona", 102)
)
```
Wether a relationship is a one-to-one or one-to-many relationship is therefore determined by the data and not the schema.


### Relationship graph

Relationships can nest arbitrarily deep. We could for instance in the `Addr` namespace have a relationship to a `Country` namespace and then get the country `name` too:

```
Person.name.Home.street.city.Country.name.get.head === ("Fred", "Baker St. 7", "Boston", "USA")
```
And so on...

(All getters have an [asynchronous equivalent](/manual/attributes/basics). Synchronous getters shown for brevity)


## Card-many relationships

[Tests...](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/ref/Relations.scala)

Cardinality-many relationships in Molecule are modelled with the `many[<RefNamespace>]` syntax:

```
object OrderDefinition {

  trait Order {
    val id    = oneString
    val items = many[LineItem].isComponent
  }

  trait LineItem {
    val qty     = oneInt
    val product = oneString
    val price   = oneDouble
  }
}
```
An `Order` can have multiple `LineItem`s so we define a cardinality-many ref attribute `items` that points to the `LineItem` namespace.

Note how we also make LineItems a component with the `isComponent` option. That means that `LineItem`s are _owned_ by an `Order` and will get automatically retracted if the `Order` is retracted. Subsequent component-defined referenced entities will be recursively retracted too.

Now we can get an Order and its Line Items:

```
Order.id.Items.qty.product.price.get === List(
  ("order1", 3, "Milk", 12.00),
  ("order1", 2, "Coffee", 46.00),
  ("order2", 4, "Bread", 5.00)
)
```
The Order data is repeated for each line Item which is kind of redundant. We can avoid that with a "nested" Molecule instead:


## Nested data

[Tests...](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/ref/nested/NestedRef.scala)

We can nest the result from the above example with the Molecule operator `*` indicating "with many":

```
m(Order.id.Items * LineItem.qty.product.price).get === List(
  ("order1", List(
    (3, "Milk", 12.00), 
    (2, "Coffee", 46.00))),
  ("order2", List(
    (4, "Bread", 5.00)))
)
```
Now each Order has its own list of typed Line Item data and there is no Order redundancy.


### Optional nested data

Optional nested data can be queried with the `*?` operator:

```
m(Ns.int.Refs1 * Ref1.str1) insert List(
  (1, List("a", "b")),
  (2, List()) // (no nested data)
)

// Mandatory nested data
m(Ns.int.Refs1 * Ref1.str1).get === List(
  (1, List("a", "b"))
)

// Optional nested data
m(Ns.int.Refs1 *? Ref1.str1).get === List(
  (1, List("a", "b")),
  (2, List())
)
```

Molecule can nest data structures up to 7 levels deep.

All getters have an [asynchronous equivalent](/manual/attributes/basics). Synchronous getters shown for brevity.


### Entity API

[Tests...](https://github.com/scalamolecule/molecule/blob/master/examples/src/test/scala/molecule/examples/dayOfDatomic/ProductsAndOrders.scala)

We can get a similar - but un-typed - nested hierarchy of data with the Entity API by calling `touch` on an order id:

```
// Touch entity facts hierarchy recursively
orderId.touch === Map(
  ":db/id" -> 101L,
  ":Order/id" -> "order1",
  ":Order/items" -> List(
    Map(
      ":db/id" -> 102L, 
      ":LineItem/qty" -> 3, 
      ":LineItem/product" -> "Milk",
      ":LineItem/price" -> 12.0),
    Map(
      ":db/id" -> 103L, 
      ":LineItem/qty" -> 2, 
      ":LineItem/product" -> "Coffee",
      ":LineItem/price" -> 46.0)))
```

## Composites

[Tests...](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/ref/Composite.scala)

As we saw earlier, [Entities](/manual/entities/) are simply groups of facts that share an entity id:

![](/img/entity/entity5.jpg)

The last fact is kind of a black sheep though since the `:Site/cat` attribute is not in the `Person` namespace.

### Avoid non-intrinsic pollution

Since entities can have attributes from **any** namespace we have a challenge of how to model this in our schema definiton. It would be quick and easy to just make a relationship from a `Person` namespace to the `Site` namespace:


```
object YourDomainDefinition {
  trait Person {
    val name  = oneString
    val likes = oneString
    val age   = oneInt
    val addr  = one[Addr]
    val site  = one[Site]
  }
  trait Addr {
    val street = oneString
    val city   = oneString
  }
  trait Site {
    val cat    = oneString
  }
}
```
Then we could easily build a molecule to get the `Site` category:

```
Person.name.likes.age.Site.cat.get.head === ("Fred", "pizza", 38, "customer")
```


Modelling-wise this is just not the best idea since we could easily end up making lots of redundant relationships from various namespaces to `Site`:

```
object YourDomainDefinition {
  trait Person {
    val site  = one[Site]
  }
  trait Company {
    val site  = one[Site]
  }
  trait Project {
    val site  = one[Site]
  }
  // etc...
  
  trait Site {
    val cat    = oneString
  }
}
```
Relationships to `Site` are simply not _intrinsic_ to or a natural core part of neither `Person`, `Company` or `Project`. Littering non-intrinsic relationships to `Site` - and possibly other cross-cutting namespaces like `Tags`, `Likes` etc - all over the place quickly clutters and pollutes our domain model.

Instead we want to create an "associative relationship" to `Site`. This is what Datomic allow us to do by letting an entity id tie _any_ attributes together as we see in the list of facts at the top of this page.


### Composite modelling

In Molecule we model associative relationships as Composites by chaining "sub-molecules" with the `+` operator:

```
m(Person.name.likes.age + Site.cat).get === List(
  (("Fred", "pizza", 38), "customer")
)
```
We make a composite molecule from two sub-molecules `Person.name.likes.age` and `Site.cat`.

The composite result set is a list of tuples with a sub-tuple for each sub-molecule.

Since in this case the last sub-molecule only has one attribute value "customer" a single value for that is returned. If it had 2 attributes we would get a sub-tuple for that too:

```
m(Person.name.likes.age + Site.cat.status).get === List(
  (("Fred", "pizza", 38), ("customer", "good"))
)
```
And so on..

```
m(Person.name.likes.age + Site.cat.status + Loc.tags + Emotion.like).get === List(
  (("Fred", "pizza", 38), ("customer", "good"), Set("inner city", "hipster"), true)
)
```

We can compose up to 22 sub-molecules (!) which should give us plenty of room to model even the most complex composite aspects of our domain.


### ...with expressions

_"Which positive elder hipster customers like what?"_

```
m(Person.name.likes.age_.>(35) 
 + Site.cat_("customer")
 + Loc.tags_("hipster") 
 + Emotion.like_(true)).get === List(
  ("Fred", "pizza")
)
```
The combinations are quite endless - while you can keep your domain model/schema clean and intrinsic!


### Arity 22+ molecules

Since composites are composed of up to 22 sub-molecules we could potentially insert and retrieve mega composite molecules with up to 22 x 22 = 484 attributes!

(All getters have an [asynchronous equivalent](/manual/attributes/basics). Synchronous getters shown for brevity)

## Bidirectional references

[Tests...](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/bidirectionals/self/OneSelf.scala)


### Unidirectional reference limitations

Normal Datomic references are unidirectional. If we add a friend reference from Ann to Ben

```
Person.name("Ann").Friends.name("Ben").save
```
Then we can naturally query to get friends of Ann

```
Person.name_("Ann").Friends.name.get === List("Ben")
```

But what if we want to find friends of Ben? This will give us nothing since our reference only went from Ann to Ben:

```
Person.name_("Ben").Friends.name.get === List()
```

Instead we would have to think backwards to get the back reference "who referenced Ben?":

```
 Person.name.Friends.name_("Ben").get === List("Ann")
```
Since we can't know from which person a friendship reference is made we will always have to query separately in both directions. If we were to traverse say 3 levels into a friendship graph we would end up with 6 queries - one in each direction for all three levels. It can quickly become a pain.

### Bidirectional refs to the rescue...

By defining a relationship in Molecule as bidirectional:
```
val friends = manyBi[Person]
```
we can start treating friendship relationships uniformly in both directions and get the intuitively expected results
```
 Person.name_("Ann").Friends.name.get === List("Ben")
 Person.name_("Ben").Friends.name.get === List("Ann")
```

And the graph example becomes easy
```
 Person.name_("Ann").Friends.Friends.Friends.name.get === List(...)
 
 // Or without recycling to Ann
 Person.name_("Ann").Friends.Friends.name_.not("Ann").Friends.name.not("Ann").get === List(...)
```

### Direct bidirectional refs

Single direct references to another entity can either go to the same namespace or to another namespace:

### A <---> A / Self-join
The friendship reference we saw above is a classic "self-join" in that it's a cardinality-many relationship between same-kinds: Persons.

A cardinality-one example would be
```
val spouse = oneBi[Person]
```
where the relationship goes in both directions but only between two persons. If Ann is spouse to Ben, then Ben is also spouse to Ann and we want to be able to query that information uniformly in both directions:

```
 Person.name_("Ann").Spouse.name.get === List("Ben")
 Person.name_("Ben").Spouse.name.get === List("Ann")
```


### A <---> B

In a zoo we could (admittedly a bit contrively) say that the caretakers are buddies with the animals they take care of, and reversely the caretakers would be "buddies" of the animals. We define such bidirectional relationship with a reference from each namespace to the other:

```
object Person extends Person
trait Person {
  val buddies = manyBi[Animal.buddies.type]
  
  val name = oneString
}

object Animal extends Animal
trait Animal {
  val buddies = manyBi[Person.buddies.type]
  
  val name = oneString
}
```
Each `manyBi` reference definition takes a type parameter that points back to the other definition. This is so that Molecule can keep track of the references back and forth.

As with friends we can now query uniformly no matter from which end the reference was entered:

```
 Person.name_("Joe").Buddies.name.get === List("Leo", "Gus")
 Animal.name_("Leo").Buddies.name.get === List("Joe")
 Animal.name_("Gus").Buddies.name.get === List("Joe")
```
An interesting aspect is that we can give the reference attributes different names on each end. Say Persons have 1 Pet and we model that as a bidirectional cardinality-one reference:

```
object Person extends Person
trait Person {
  val pet = oneBi[Animal.master.type]
  
  val name = oneString
}

object Animal extends Animal
trait Animal {
  val master = oneBi[Person.pet.type]
}
```
If we then enter a pet ownership

```
Person.name("Liz").Pet.name("Rex").save
```
then we can get access to that information uniformly from both ends even though different attribute names are used:

```
 Person.name_("Liz").Pet.name.get === List("Rex")
 Animal.name_("Rex").Master.name.get === List("Liz")
```


### Property edges

Taking bidirectionality to the next level involves "property edges", a term taken from graph theory where an edge/relationship between two vertices/entities has some property values attached to it. Molecule models this by using a bidirectional reference between one entity and a (property edge) entity, and then between this property edge entity and another entity.

This is actually what we do all the time with references except that they are normally unidirectional! In order to make them bidirectional Molecule offers a convenient solution:


### A <---> Edge.properties... <---> A

If we want to express "how well" two persons know each other we could model the above friendship example instead with at property edge having a `weight` property:

```
// Entity
object Person extends Person
trait Person {
  // A ==> edge -- a
  val knows = manyBiEdge[Knows.person.type]
  
  val name = oneString
}

// Property edge
object Knows extends Knows
trait Knows {
  // a --- edge ==> a
  val person: AnyRef = target[Person.knows.type]
  
  // Property
  val weight = oneInt
}
```
Now we use the `manyBiEdge` definition that takes a type parameter pointing to the reference in the edge namespace that points back here. In the edge namespace we define the reference back with the `target` definition.

Now we can add some weighed friendships:

```
Person.name.Knows.*(Knows.weight.Person.name).insert("Ann", List((7, "Ben"), (8, "Joe")))
```
And uniformly retrieve that information from any end:

```
Person.name_("Ann").Knows.*(Knows.weight.Person.name).get.head === List((7, "Ben"), (8, "Joe"))
Person.name_("Ben").Knows.*(Knows.weight.Person.name).get.head === List((7, "Ann"))
Person.name_("Joe").Knows.*(Knows.weight.Person.name).get.head === List((8, "Ann"))
```

### A <---> Edge.properties... <---> B

Let's add weight to the relationships between caretakers and animals. The edge namespace now has to reference both the Person and Animal namespaces:

```
// Entity A
object Person extends Person
trait Person {
  // Ref to edge
  // A ==> edge -- b
  val closeTo = manyBiEdge[CloseTo.animal.type]
  
  val name = oneString
}

// Property edge
object CloseTo extends CloseTo
trait CloseTo {
  // Ref to Person
  // a <== edge --- b
  val person: AnyRef = target[Person.closeTo.type]
  
  // Ref to Animal
  // a --- edge ==> b
  val animal: AnyRef = target[Animal.closeTo.type]
  
  // Property
  val weight = oneInt
}

// Entity B
object Animal extends Animal
trait Animal {
  // Ref to edge
  // a -- edge <== B
  val closeTo  = manyBiEdge[CloseTo.person.type]
  
  val name = oneString
}
```
Adding data from one end (we could as well have done it from the Animal end)

```
Person.name.CloseTo.*(CloseTo.weight.Animal.name) insert List(("Joe", List((7, "Gus"), (6, "Leo"))))
```
We can now uniformly retrieve the weighed friendship information from any end:

```
// Querying from Person
Person.name_("Joe").CloseTo.*(CloseTo.weight.Animal.name).get.head === List((7, "Gus"), (8, "Leo"))

// Querying from Animal
Animal.name_("Gus").CloseTo.*(CloseTo.weight.Person.name).get.head === List((7, "Joe"))
Animal.name_("Leo").CloseTo.*(CloseTo.weight.Person.name).get.head === List((8, "Joe"))
```

### How it works

For each bidirectional reference created, Molecule creates a reverse reference:

```
Ann --> Ben
Ben <-- Ann // reverse ref
```
and for edges a full reverse edge entity with properties is created:

```
Ann --> annLovesBen (7) -->  Ben
  \                         /
    <-- benLovesAnn (7) <--       // reverse edge
```

Since Molecule is a closed eco-system it can manage this redundancy with 100% control. The advantages of uniform queries should easily outweigh the impact of a bit of additional information for the reverse references.


### More examples...

Please have a look at the implementation of the [Gremlin graph](https://github.com/scalamolecule/molecule/blob/master/examples/src/test/scala/molecule/examples/gremlin/gettingStarted/).

(All getters have an [asynchronous equivalent](/manual/attributes/basics). Synchronous getters shown for brevity)



## Self-join


Please see the following
[Self-join examples of Molecule](https://github.com/scalamolecule/molecule/blob/master/coretests/src/test/scala/molecule/coretests/ref/SelfJoin.scala#L1)


