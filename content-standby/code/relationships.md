---
title: "Relationships"
weight: 40
menu:
  main:
    parent: manual
    identifier: relationships
---

# Relationships

[Tests...](https://github.com/scalamolecule/molecule/blob/master/molecule-tests/src/test/scala/molecule/tests/core/ref)




## Card-one

A relationship in Datomic is simply when a ref attribute of entity A has an entity B id value. Then there is a relationship from A to B!

In the following example, entity `101` has a ref attribute `:Person/home` with a value `102`. That makes the relationship between entity `101` and entity `102`, or that Fred has an Address:


![](/img/page/relationships/ref.png)


We can illustrate the same data as two entities (groups of facts with a shared entity id) with the link between them:

![](/img/page/relationships/card-one.png)

In Molecule we model a cardinality-one relationship in our [Data Model](/setup/data-model) with the `one[<RefNamespace>]` syntax:

```scala
object YourDomainDataModel {
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

```scala
Person.name.home.get.map(_ ==> List(("Fred", 102))
```
This can be practical when we want to get a related entity id like `102` in this case.


### Ref namespace

More often though we want to collect the values of the referenced entity attributes. Molecule therefore also creates an Uppercase method `Home` that allow us to add attributes from the `Home` (`Addr`) namespace:

```scala
Person.name.Home.street.city.get.map(_.head ==> ("Fred", "Baker St. 7", "Boston")
```
Describing the relationship we can simply say that a "Person has an Address". It's important to understand though that the namespace names `Person` and `Addr` are not like SQL tables. So there is no "instance of a Person" but rather _"an **entity** with some Person attribute values"_ (and maybe other attribute values). It's the entity id that tie groups of facts/attribute values together.


### One-to-one or one-to-many

If Fred is living by himself on Baker St. 7 we could talk about a one-to-one relationship between him and his address.

But if several people live on the same address each person entity would reference the same address entity and we would then see each persons relation to the address as a one-to-many relationship since other persons also share the address:

```scala
Person.name.home.get.map(_ ==> List(
  ("Fred", 102),
  ("Lisa", 102),
  ("Mona", 102)
)
```
Wether a relationship is a one-to-one or one-to-many relationship is therefore determined by the data and not the schema.


### Relationship graph

Relationships can nest arbitrarily deep. We could for instance in the `Addr` namespace have a relationship to a `Country` namespace and then get the country `name` too:

```scala
Person.name.Home.street.city.Country.name.get.map(_.head ==> ("Fred", "Baker St. 7", "Boston", "USA")
```








## Card-many


Since datomic has cardinality many attributes, ref attributes can also be of cardinality many.

We could for instance have a classic Order/LineItems card-many example where the `:Order/items` card-many ref attribute has two LineItem entity id values `102` and `103`:

![](/img/page/relationships/card-many.png)



Cardinality-many relationships in Molecule are modelled with the `many[<RefNamespace>]` syntax:

```scala
object OrderDataModel {

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

```scala
Order.id.Items.qty.product.price.get.map(_ ==> List(
  ("order1", 3, "Milk", 12.00),
  ("order1", 2, "Coffee", 46.00),
  ("order2", 4, "Bread", 5.00)
)
```
The Order data is repeated for each line Item which is kind of redundant. We can avoid that with a "nested" Molecule instead:


## Nested data

We can nest the result from the above example with the Molecule operator `*` indicating "with many":

```scala
m(Order.id.Items * LineItem.qty.product.price).get.map(_ ==> List(
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

```scala
m(Ns.int.Refs1 * Ref1.str1) insert List(
  (1, List("a", "b")),
  (2, List()) // (no nested data)
)

// Mandatory nested data
m(Ns.int.Refs1 * Ref1.str1).get.map(_ ==> List(
  (1, List("a", "b"))
)

// Optional nested data
m(Ns.int.Refs1 *? Ref1.str1).get.map(_ ==> List(
  (1, List("a", "b")),
  (2, List())
)
```

Molecule can nest data structures up to 7 levels deep.

All getters have an [asynchronous equivalent](/manual/attributes/basics). Synchronous getters shown for brevity.


### Entity API

We can get a similar - but un-typed - nested hierarchy of data with the Entity API by calling `touch` on an order id:

```scala
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










## Associative

As we saw earlier, [Entities](/manual/entities/) are simply groups of facts that share an entity id:

![](/img/page/entity/entity2.png)

The last fact is kind of a black sheep though since the `:Site/cat` attribute is not in the `Person` namespace.

### Avoid non-intrinsic pollution

Since entities can have attributes from **any** namespace we have a challenge of how to model this in our schema definiton. It would be quick and easy to just make a relationship from a `Person` namespace to the `Site` namespace:


```scala
object YourDomainDataModel {
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

```scala
Person.name.likes.age.Site.cat.get.map(_.head ==> ("Fred", "pizza", 38, "customer")
```


Modelling-wise this is just not the best idea since we could easily end up making lots of redundant relationships from various namespaces to `Site`:

```scala
object YourDomainDataModel {
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

```scala
m(Person.name.likes.age + Site.cat).get.map(_ ==> List(
  (("Fred", "pizza", 38), "customer")
)
```
We make a composite molecule from two sub-molecules `Person.name.likes.age` and `Site.cat`.

The composite result set is a list of tuples with a sub-tuple for each sub-molecule.

Since in this case the last sub-molecule only has one attribute value "customer" a single value for that is returned. If it had 2 attributes we would get a sub-tuple for that too:

```scala
m(Person.name.likes.age + Site.cat.status).get.map(_ ==> List(
  (("Fred", "pizza", 38), ("customer", "good"))
)
```
And so on..

```scala
m(Person.name.likes.age + Site.cat.status + Loc.tags + Emotion.like).get.map(_ ==> List(
  (("Fred", "pizza", 38), ("customer", "good"), Set("inner city", "hipster"), true)
)
```

We can compose up to 22 sub-molecules (!) which should give us plenty of room to model even the most complex composite aspects of our domain.


### ...with expressions

_"Which positive elder hipster customers like what?"_

```scala
m(Person.name.likes.age_.>(35) 
 + Site.cat_("customer")
 + Loc.tags_("hipster") 
 + Emotion.like_(true)).get.map(_ ==> List(
  ("Fred", "pizza")
)
```
The combinations are quite endless - while you can keep your domain model/schema clean and intrinsic!


### Arity 22+ molecules

Since composites are composed of up to 22 sub-molecules we could potentially insert and retrieve mega composite molecules with up to 22 x 22 = 484 attributes!

(All getters have an [asynchronous equivalent](/manual/attributes/basics). Synchronous getters shown for brevity)








## Bidirectional

Relationships in Datomic are unidirectional but can be queried in reverse when needed. When working with graph structures we can benefit from being able to traverse the graph recursively without worrying about in which direction each relationship was created. 

Molecule offers to define bidirectional relationships that makes traversal easy. 

### Unidirectional reference limitations

If we add a friend reference from Ann to Ben

```scala
Person.name("Ann").Friends.name("Ben").save
```
Then we can naturally query to get friends of Ann

```scala
Person.name_("Ann").Friends.name.get.map(_ ==> List("Ben")
```

But what if we want to find friends of Ben? This will give us nothing since our reference only went from Ann to Ben:

```scala
Person.name_("Ben").Friends.name.get.map(_ ==> List()
```

Instead we would have to think backwards to get the back reference "who referenced Ben?":

```scala
 Person.name.Friends.name_("Ben").get.map(_ ==> List("Ann")
```
Since we can't know from which person a friendship reference is made we will always have to query separately in both directions. If we were to traverse say 3 levels into a friendship graph we would end up with 6 queries - one in each direction for all three levels. It can quickly become a pain.

### Bidirectional refs to the rescue...

By defining a relationship in Molecule as bidirectional:
```scala
val friends = manyBi[Person]
```
we can start treating friendship relationships uniformly in both directions and get the intuitively expected results
```scala
 Person.name_("Ann").Friends.name.get.map(_ ==> List("Ben")
 Person.name_("Ben").Friends.name.get.map(_ ==> List("Ann")
```

And the graph example becomes easy
```scala
 Person.name_("Ann").Friends.Friends.Friends.name.get.map(_ ==> List(...)
 
 // Or without recycling to Ann
 Person.name_("Ann").Friends.Friends.name_.not("Ann").Friends.name.not("Ann").get.map(_ ==> List(...)
```

### Direct bidirectional refs

Single direct references to another entity can either go to the same namespace or to another namespace:

### A <---> A / Self-join
The friendship reference we saw above is a classic "self-join" in that it's a cardinality-many relationship between same-kinds: Persons.

A cardinality-one example would be
```scala
val spouse = oneBi[Person]
```
where the relationship goes in both directions but only between two persons. If Ann is spouse to Ben, then Ben is also spouse to Ann and we want to be able to query that information uniformly in both directions:

```scala
 Person.name_("Ann").Spouse.name.get.map(_ ==> List("Ben")
 Person.name_("Ben").Spouse.name.get.map(_ ==> List("Ann")
```


### A <---> B

In a zoo we could (admittedly a bit contrively) say that the caretakers are buddies with the animals they take care of, and reversely the caretakers would be "buddies" of the animals. We define such bidirectional relationship with a reference from each namespace to the other:

```scala
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

```scala
 Person.name_("Joe").Buddies.name.get.map(_ ==> List("Leo", "Gus")
 Animal.name_("Leo").Buddies.name.get.map(_ ==> List("Joe")
 Animal.name_("Gus").Buddies.name.get.map(_ ==> List("Joe")
```
An interesting aspect is that we can give the reference attributes different names on each end. Say Persons have 1 Pet and we model that as a bidirectional cardinality-one reference:

```scala
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

```scala
Person.name("Liz").Pet.name("Rex").save
```
then we can get access to that information uniformly from both ends even though different attribute names are used:

```scala
 Person.name_("Liz").Pet.name.get.map(_ ==> List("Rex")
 Animal.name_("Rex").Master.name.get.map(_ ==> List("Liz")
```


### Property edges

Taking bidirectionality to the next level involves "property edges", a term taken from graph theory where an edge/relationship between two vertices/entities has some property values attached to it. Molecule models this by using a bidirectional reference between one entity and a (property edge) entity, and then between this property edge entity and another entity.

This is actually what we do all the time with references except that they are normally unidirectional! In order to make them bidirectional Molecule offers a convenient solution:


### A <---> Edge.properties... <---> A

If we want to express "how well" two persons know each other we could model the above friendship example instead with at property edge having a `weight` property:

```scala
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

```scala
Person.name.Knows.*(Knows.weight.Person.name).insert("Ann", List((7, "Ben"), (8, "Joe")))
```
And uniformly retrieve that information from any end:

```scala
Person.name_("Ann").Knows.*(Knows.weight.Person.name).get.map(_ ==> List((7, "Ben"), (8, "Joe"))
Person.name_("Ben").Knows.*(Knows.weight.Person.name).get.map(_ ==> List((7, "Ann"))
Person.name_("Joe").Knows.*(Knows.weight.Person.name).get.map(_ ==> List((8, "Ann"))
```

### A <---> Edge.properties... <---> B

Let's add weight to the relationships between caretakers and animals. The edge namespace now has to reference both the Person and Animal namespaces:

```scala
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

```scala
Person.name.CloseTo.*(CloseTo.weight.Animal.name) insert List(("Joe", List((7, "Gus"), (6, "Leo"))))
```
We can now uniformly retrieve the weighed friendship information from any end:

```scala
// Querying from Person
Person.name_("Joe").CloseTo.*(CloseTo.weight.Animal.name).get.map(_ ==> List((7, "Gus"), (8, "Leo"))

// Querying from Animal
Animal.name_("Gus").CloseTo.*(CloseTo.weight.Person.name).get.map(_ ==> List((7, "Joe"))
Animal.name_("Leo").CloseTo.*(CloseTo.weight.Person.name).get.map(_ ==> List((8, "Joe"))
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


### More bidirectional graph examples...

Please have a look at the [Gremlin examples](/intro/compare/gremlin/).



## Self-join

Self-joins can be used to compare values of an attribute of one entity with values of the same attribute for other entities.

Let's consider an example of `Person`s with an `age`, `name` and beverage preferences. 


```scala
m(Person.age.name.Likes * Score.beverage.rating) insert List(
  (23, "Joe", List(("Coffee", 3), ("Cola", 2), ("Pepsi", 3))),
  (25, "Ben", List(("Coffee", 2), ("Tea", 3))),
  (23, "Liz", List(("Coffee", 1), ("Tea", 3), ("Pepsi", 1)))
)
```

Normally we ask for values accross attributes like attr1 AND attr2 AND etc as in age==23 AND name AND rating==Pepsi
```scala
Person.age_(23).name.Likes.beverage_("Pepsi").get.map(_ ==> List("Liz", "Joe")
```
But when we need to compare values of the same attribute across entities we need self-joins.

Here's an example of a self-join where we take pairs of person entities where one is 23 years old and the other 25 years old and then see which of those pairs have a shared preferred beverage. We say that we "unify" by the attribute values that the two entities have in common (`Likes.beverage`). 

Self-joins lets us answer a lot of interesting questions:

What beverages do pairs of 23- AND 25-year-olds like in common?
```scala
// (unifying on Likes.beverage)
Person.age_(23 and 25).Likes.beverage.get.map(_ ==> List("Coffee", "Tea")
// Joe (23) AND Ben (25) likes Coffee (Coffee unifies)
// Liz (23) AND Ben (25) likes Coffee (Coffee unifies)
// Liz (23) AND Ben (25) likes Tea    (Tea unifies)
// Distinct values of Coffee and Tea returned
```

Does 23- and 25-years-old have some common beverage ratings?
```scala
// (unifying on Likes.rating)
Person.age_(23 and 25).Likes.rating.get.map(_ ==> List(2, 3)
```
Any 23- and 25-year-olds with the same name? (no)
```scala
// (unifying on Person.name)
Person.age_(23 and 25).name.get.map(_ ==> List()
```

Which beverages do Joe and Liz both like?
```scala
// (unifying on Likes.beverage)
Person.name_("Joe" and "Liz").Likes.beverage.get.map(_ ==> List("Pepsi", "Coffee")
```
Do Joe and Liz have some common ratings?
```scala
// (unifying on Likes.rating)
Person.name_("Joe" and "Liz").Likes.rating.get.map(_ ==> List(3)
```
Do Joe and Liz have a shared age?
```scala
// (unifying on Person.age)
Person.name_("Joe" and "Liz").age.get.map(_ ==> List(23)
```

Who likes both Coffee and Tea?
```scala
// (unifying on Person.name)
Person.name.Likes.beverage_("Coffee" and "Tea").get.map(_ ==> List("Ben", "Liz")
```
What ages have those who like both Coffe and Tea?
```scala
// (unifying on Person.age)
Person.age.Likes.beverage_("Coffee" and "Tea").get.map(_ ==> List(23, 25)
```
What shared ratings do Coffee and Tea have?
```scala
// (unifying on Score.rating)
Score.beverage_("Coffee" and "Tea").rating.get.map(_ ==> List(3)
```

Who rated both 2 and 3?
```scala
// (unifying on Person.name)
Person.name.Likes.rating_(2 and 3).get.map(_ ==> List("Ben", "Joe")
```
What ages have those who rated both 2 and 3?
```scala
// (unifying on Person.age)
Person.age.Likes.rating_(2 and 3).get.map(_ ==> List(23, 25)
```
Which beverages are rated 2 and 3?
```scala
// (unifying on Likes.beverage)
Score.rating_(2 and 3).beverage.get.map(_ ==> List("Coffee")
```


### Unifying by 2 attributes
Which 23- and 25-year-olds with the same name like the same beverage? (none)
```scala
// (unifying on Person.name and Likes.beverage)
Person.age_(23 and 25).name.Likes.beverage.get.map(_ ==> List()
```
Do Joe and Liz share age and beverage preferences? (yes)
```scala
// (unifying on Person.age and Likes.beverage)
Person.age.name_("Joe" and "Liz").Likes.beverage.get.map(_ ==> List(
  (23, "Coffee"),
  (23, "Pepsi"))
```

### Multiple ANDs

```scala
Person.name_("Joe" and "Ben" and "Liz").Likes.beverage.get.map(_ ==> List("Coffee")
```


### Explicit self-join

All the examples above use the `and` notation to construct simple self-joins. Any of them could be re-written to use a more powerful and expressive `Self`-notation:

```scala
Person.age_(23 and 25).Likes.beverage.get.map(_ ==> List("Coffee", "Tea")

// ..can be re-written to:
Person.age_(23).Likes.beverage._Person.Self
  .age_(25).Likes.beverage_(unify).get.map(_ ==> List("Coffee", "Tea")
```

Let's walk through that one...

First we ask for a tacit `age` of 23 being asserted with one Person (entity). After asking for the `beverage` value of the first person we "go back" with `_Person` to the initial namespace `Person` and then say that we want to make a self-join with `Self` to start defining another Person/entity. We want the other person to be 25 years old. When we define the `beverage` value for the other person we tell molecule to "unify" that value with the equivalent `beverage` value of the first person.

This second notation gives us freedom to fetch more values that shouldn't be unified. Say for instance that we want to know the names of 23-/25-year-olds sharing a beverage preference:

```scala
Person.age_(23).name.Likes.beverage._Person.Self
  .age_(25).name.Likes.beverage_(unify).get.sorted === List(
  ("Joe", "Coffee", "Ben"),
  ("Liz", "Coffee", "Ben"),
  ("Liz", "Tea", "Ben")
)
```
Now we also fetch the name of beverage which is not being unified between the two entities.

Let's add the ratings too
```scala
Person.age_(23).name.Likes.rating.beverage._Person.Self
  .age_(25).name.Likes.beverage_(unify).rating.get.sorted === List(
  ("Joe", 3, "Coffee", "Ben", 2),
  ("Liz", 1, "Coffee", "Ben", 2),
  ("Liz", 3, "Tea", "Ben", 3)
)
```

We can arrange the attributes in the previous molecule in other orders too:
```scala
Person.age_(23).name.Likes.rating.beverage._Person.Self
  .age_(25).name.Likes.rating.beverage_(unify).get.sorted === List(
  ("Joe", 3, "Coffee", "Ben", 2),
  ("Liz", 1, "Coffee", "Ben", 2),
  ("Liz", 3, "Tea", "Ben", 3)
)
// or
Person.age_(23).name.Likes.beverage.rating._Person.Self
  .age_(25).name.Likes.beverage_(unify).rating.get.sorted === List(
  ("Joe", "Coffee", 3, "Ben", 2),
  ("Liz", "Coffee", 1, "Ben", 2),
  ("Liz", "Tea", 3, "Ben", 3)
)
// or
Person.age_(23).name.Likes.beverage.rating._Person.Self
  .age_(25).name.Likes.rating.beverage_(unify).get.sorted === List(
  ("Joe", "Coffee", 3, "Ben", 2),
  ("Liz", "Coffee", 1, "Ben", 2),
  ("Liz", "Tea", 3, "Ben", 3)
)
```

Only higher rated beverages
```scala
Person.age_(23).name.Likes.rating.>(1).beverage._Person.Self
  .age_(25).name.Likes.rating.>(1).beverage_(unify).get.sorted === List(
  ("Joe", 3, "Coffee", "Ben", 2),
  ("Liz", 3, "Tea", "Ben", 3)
)
```

Only highest rated beverages
```scala
Person.age_(23).name.Likes.rating(3).beverage._Person.Self
  .age_(25).name.Likes.rating(3).beverage_(unify).get.sorted === List(
  ("Liz", 3, "Tea", "Ben", 3)
)
```

Common beverage of 23-year-old with low rating and 25-year-old with high rating
```scala
Person.age_(23).name.Likes.rating(1).beverage._Person.Self
  .age_(25).name.Likes.rating(2).beverage_(unify).get.sorted === List(
  ("Liz", 1, "Coffee", "Ben", 2)
)
```

Any 23- and 25-year-olds wanting to drink tea together?
```scala
Person.age_(23).name.Likes.beverage_("Tea")._Person.Self
  .age_(25).name.Likes.beverage_("Tea").get.map(_ ==> List(("Liz", "Ben"))
```

Any 23-year old Tea drinker and a 25-year-old Coffee drinker?
```scala
Person.age_(23).name.Likes.beverage_("Tea")._Person.Self
  .age_(25).name.Likes.beverage_("Coffee").get.map(_ ==> List(("Liz", "Ben"))
```

Any pair of young persons drinking respectively Tea and Coffee?
```scala
Person.age_.<(24).name.Likes.beverage_("Tea")._Person.Self
  .age_.<(24).name.Likes.beverage_("Coffee").get.map(_ ==> List(
  ("Liz", "Joe"),
  ("Liz", "Liz")
)
```
Since Liz is under 24 and drinks both Tea and Coffee she shows up as two persons (one drinking Tea, the other Coffee). We can filter the result to only get different persons:
```scala
Person.e.age_.<(24).name.Likes.beverage_("Tea")._Person.Self
  .e.age_.<(24).name.Likes.beverage_("Coffee").get
  .filter(r => r._1 != r._3).map(r => (r._2, r._4)) === List(
  ("Liz", "Joe")
)
```

### Multiple explicit self-joins

Beverages liked by all 3 different people
```scala
Person.name_("Joe" and "Ben" and "Liz").Likes.beverage.get.map(_ ==> List("Coffee")

// or

Person.name_("Joe").Likes.beverage._Person.Self
  .name_("Ben").Likes.beverage_(unify)._Person.Self
  .name_("Liz").Likes.beverage_(unify).get.map(_ ==> List("Coffee")
```
