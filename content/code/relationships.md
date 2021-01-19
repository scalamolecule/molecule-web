---
title: "Relationships"
weight: 40
menu:
  main:
    parent: code
---

# Relationships

A relationship (or reference) in Molecule is when an entity has a _ref-attribute_ holding the entity id of another entity.

A _ref-attribute_ is defined in our Data Model like this:

```scala
object PersonDataModel {
  
  trait Person {
    val name    = oneString
    val pet     = one[Animal]    // `pet` is a card-one ref attribute
    val hobbies = many[Activity] // `hobbies` is a card-many ref attribute
  }
  
  trait Animal {
    val name = oneString
  }
  
  trait Activity {
    val name = oneString
  }
}
```
`pet` can hold one `Long` which is the id of a referenced Animal entity (the pet).

`hobbies` can hold multiple `Long`s which are the ids of referenced Activity entities (the hobbies). 




## Card-one


### Saving relational data

Given the example Data Model above, we can save a card-one relationship between "Dan" and his pet "Rex":

```scala
val List(danId, rexId) = Person.name("Dan").Pet.name("Rex").save.eids
```
A Dan and a Rex entity were created.

The `pet` ref-attribute of the Dan entity holds `rexId`. That's the relationship from Dan to Rex.

If a `rexId` already existed, we could have saved it directly by applying it to the `pet` ref-attribute:

```scala
val List(danId) = Person.name("Dan").pet(rexId).save.eids
```
Now only Dan was created, having two attributes: `name` with value "Dan" and `pet` with value `rexId`. 


### Retrieving relational data

We can ask for related data by using a Capitalized ref-attribute name `Pet`:

```scala
Person.name.Pet.name.get.head === ("Dan", "Rex")
```

And related entity ids with the lowercase ref-attribute name `pet`:

```scala
Person.e.name_("Dan").pet.get.head === (danId, rexId)
```


### Ref namespace

As you see, Molecule generates a Capitalized version of all ref-attributes serving as a "bridge" to the referenced Namespace attributes. We call these "Ref namespaces".

In our example we used `Pet` to get to the `Animal.name` attribute and `pet` to get the referenced entity id value `rexId`.

>_Capitalized_ ref-attribute names are _Ref namespaces_
>
>_Lowercase_ ref-attributes hold referenced entity ids



### One-to-one or one-to-many

If John is living by himself on 5th Avenue we could talk about a one-to-one relationship between him and his address.

But if several people live on the same address, say entity id `102`, then we have a one-to-many relationship since multiple people entities have a reference to `102`:

```scala
Person.name.home.get === List(
  ("John", 102),
  ("Lisa", 102),
  ("Mona", 102)
)
```
Wether a relationship is a one-to-one or one-to-many relationship is determined by the data. In our Data Model, we just model it as a card-one relationship `one[Address]`.


### Relationship graph

Relationship graphs can become arbitrarily deep. We could for instance in a `Address` namespace have a relationship to a `Country` namespace and then get the country `name` too and so on:

```scala
Person.name.Home.street.city.Country.name.get.head === ("John", "5th Avenue", "Boston", "USA")
// etc...
```









## Card-many


Cardinality-many ref-attributes can simply hold a `Set` of referenced entity ids and are modelled with the `many[<RefNamespace>]` syntax:

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

Note how we in this example make LineItems a component with the `isComponent` option. That means that `LineItem`s are _owned_ by an `Order` and will get automatically retracted if the `Order` is retracted. Subsequent component-defined referenced entities will be recursively retracted too.

Now we can get an Order and its Line Items:

```scala
Order.id.Items.qty.product.price.get === List(
  ("order1", 3, "Milk", 12.00),
  ("order1", 2, "Coffee", 46.00),
  ("order2", 4, "Bread", 5.00)
)
```
The Order data is repeated for each line Item which is kind of redundant. We can avoid that with a "nested" Molecule instead:


### Nested data

We can nest the result from the above example with the Molecule operator `*` indicating "with many":

```scala
m(Order.id.Items * LineItem.qty.product.price).get === List(
  ("order1", List(
    (3, "Milk", 12.00), 
    (2, "Coffee", 46.00))),
  ("order2", List(
    (4, "Bread", 5.00)))
)

// or
Order.id.Items.*(LineItem.qty.product.price).get === List( ...
```
Now each Order has its own list of typed Line Item data and there is no Order redundancy.


### Optional nested data

Optional nested data can be queried with the `*?` operator:

```scala
// Sample data
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



















## Self-join

Self-joins can be used to compare values of the _same attribute_ for multiple entities. They don't require any special definition in our Data Model.

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
Person.age_(23).name.Likes.beverage_("Pepsi").get === List("Liz", "Joe")
```
But when we need to compare values of the same attribute across entities we need self-joins.

Here's an example of a self-join where we take pairs of person entities where one is 23 years old and the other 25 years old and then see which of those pairs have a shared preferred beverage. We say that we "unify" by the attribute values that the two entities have in common (`Likes.beverage`).

Self-joins lets us answer a lot of interesting questions:

What beverages do pairs of 23- AND 25-year-olds like in common?
```scala
// (unifying on Likes.beverage)
Person.age_(23 and 25).Likes.beverage.get === List("Coffee", "Tea")
// Joe (23) AND Ben (25) likes Coffee (Coffee unifies)
// Liz (23) AND Ben (25) likes Coffee (Coffee unifies)
// Liz (23) AND Ben (25) likes Tea    (Tea unifies)
// Distinct values of Coffee and Tea returned
```

Does 23- and 25-years-old have some common beverage ratings?
```scala
// (unifying on Likes.rating)
Person.age_(23 and 25).Likes.rating.get === List(2, 3)
```
Any 23- and 25-year-olds with the same name? (no)
```scala
// (unifying on Person.name)
Person.age_(23 and 25).name.get === List()
```

Which beverages do Joe and Liz both like?
```scala
// (unifying on Likes.beverage)
Person.name_("Joe" and "Liz").Likes.beverage.get === List("Pepsi", "Coffee")
```
Do Joe and Liz have some common ratings?
```scala
// (unifying on Likes.rating)
Person.name_("Joe" and "Liz").Likes.rating.get === List(3)
```
Do Joe and Liz have a shared age?
```scala
// (unifying on Person.age)
Person.name_("Joe" and "Liz").age.get === List(23)
```

Who likes both Coffee and Tea?
```scala
// (unifying on Person.name)
Person.name.Likes.beverage_("Coffee" and "Tea").get === List("Ben", "Liz")
```
What ages have those who like both Coffe and Tea?
```scala
// (unifying on Person.age)
Person.age.Likes.beverage_("Coffee" and "Tea").get === List(23, 25)
```
What shared ratings do Coffee and Tea have?
```scala
// (unifying on Score.rating)
Score.beverage_("Coffee" and "Tea").rating.get === List(3)
```

Who rated both 2 and 3?
```scala
// (unifying on Person.name)
Person.name.Likes.rating_(2 and 3).get === List("Ben", "Joe")
```
What ages have those who rated both 2 and 3?
```scala
// (unifying on Person.age)
Person.age.Likes.rating_(2 and 3).get === List(23, 25)
```
Which beverages are rated 2 and 3?
```scala
// (unifying on Likes.beverage)
Score.rating_(2 and 3).beverage.get === List("Coffee")
```


### Unifying by 2 attributes
Which 23- and 25-year-olds with the same name like the same beverage? (none)
```scala
// (unifying on Person.name and Likes.beverage)
Person.age_(23 and 25).name.Likes.beverage.get === List()
```
Do Joe and Liz share age and beverage preferences? (yes)
```scala
// (unifying on Person.age and Likes.beverage)
Person.age.name_("Joe" and "Liz").Likes.beverage.get === List(
  (23, "Coffee"),
  (23, "Pepsi"))
```

### Multiple ANDs

```scala
Person.name_("Joe" and "Ben" and "Liz").Likes.beverage.get === List("Coffee")
```


### Explicit self-join

All the examples above use the `and` notation to construct simple self-joins. Any of them could be re-written to use a more powerful and expressive `Self`-notation:

```scala
Person.age_(23 and 25).Likes.beverage.get === List("Coffee", "Tea")

// ..can be re-written to:
Person.age_(23).Likes.beverage._Person.Self
  .age_(25).Likes.beverage_(unify).get === List("Coffee", "Tea")
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
  .age_(25).name.Likes.beverage_("Tea").get === List(("Liz", "Ben"))
```

Any 23-year old Tea drinker and a 25-year-old Coffee drinker?
```scala
Person.age_(23).name.Likes.beverage_("Tea")._Person.Self
  .age_(25).name.Likes.beverage_("Coffee").get === List(("Liz", "Ben"))
```

Any pair of young persons drinking respectively Tea and Coffee?
```scala
Person.age_.<(24).name.Likes.beverage_("Tea")._Person.Self
  .age_.<(24).name.Likes.beverage_("Coffee").get === List(
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
Person.name_("Joe" and "Ben" and "Liz").Likes.beverage.get === List("Coffee")

// or

Person.name_("Joe").Likes.beverage._Person.Self
  .name_("Ben").Likes.beverage_(unify)._Person.Self
  .name_("Liz").Likes.beverage_(unify).get === List("Coffee")
```













## Bidirectional

Relationships in Datomic are unidirectional but can be queried in reverse when needed. 

When working with graph structures we can benefit from being able to traverse the graph recursively without worrying about in which direction each relationship was created. 

Molecule offers to define bidirectional relationships that makes uniform traversals easy. 

### Unidirectional reference limitations

If we add a friend reference from Ann to Ben

```scala
Person.name("Ann").Friends.name("Ben").save
```
Then we can naturally query to get friends of Ann

```scala
Person.name_("Ann").Friends.name.get === List("Ben")
```

But what if we want to find friends of Ben? This will give us nothing since our reference only went from Ann to Ben:

```scala
Person.name_("Ben").Friends.name.get === List()
```

Instead we would have to think backwards to get the back reference "who referenced Ben?":

```scala
 Person.name.Friends.name_("Ben").get === List("Ann")
```
Since we can't know from which person a friendship reference is made we will always have to query separately in both directions. If we were to traverse say 3 levels into a friendship graph we would end up with 6 queries - one in each direction for all three levels. It can quickly become a pain.

### Bidirectional refs to the rescue...

By defining a relationship in Molecule as bidirectional:
```scala
val friends = manyBi[Person]
```
we can start treating friendship relationships uniformly in both directions and get the intuitively expected results
```scala
 Person.name_("Ann").Friends.name.get === List("Ben")
 Person.name_("Ben").Friends.name.get === List("Ann")
```

And the graph example becomes easy
```scala
 Person.name_("Ann").Friends.Friends.Friends.name.get === List(...)
 
 // Or without recycling to Ann
 Person.name_("Ann").Friends.Friends.name_.not("Ann").Friends.name.not("Ann").get === List(...)
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
 Person.name_("Ann").Spouse.name.get === List("Ben")
 Person.name_("Ben").Spouse.name.get === List("Ann")
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
 Person.name_("Joe").Buddies.name.get === List("Leo", "Gus")
 Animal.name_("Leo").Buddies.name.get === List("Joe")
 Animal.name_("Gus").Buddies.name.get === List("Joe")
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
 Person.name_("Liz").Pet.name.get === List("Rex")
 Animal.name_("Rex").Master.name.get === List("Liz")
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
Person.name_("Ann").Knows.*(Knows.weight.Person.name).get.head === List((7, "Ben"), (8, "Joe"))
Person.name_("Ben").Knows.*(Knows.weight.Person.name).get.head === List((7, "Ann"))
Person.name_("Joe").Knows.*(Knows.weight.Person.name).get.head === List((8, "Ann"))
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


### More bidirectional graph examples...

Please have a look at the [Gremlin examples](/intro/compare/gremlin/).






















## Associative

Datomic allows for a very powerful special type of relationship that Molecule calls an _associative relationship_. This is simply when entities contain attributes from different namespaces. In the SQL world it would be like the ability to "borrow" columns from other tables.


You might recall how [entities](/intro/building-blocks/#entity) are composed of attributes sharing the same entity id, like the pizza-liking 24-year-old John:

{{< bootstrap-table "table table-bordered" >}}
Entity id | Attribue      | Value    
:---:     | :---          | :---  
101       | :Person/name  | "John"
101       | :Person/likes | "pizza"
101       | :Person/age   | 24
{{< /bootstrap-table >}}

Say, John registers with our website, and we want to categorize John as a customer. Then the challenge arises, how should we model that John is in a certain category? Since categories are applied to many things, this is a classical [cross-cutting concern](https://en.wikipedia.org/wiki/Cross-cutting_concern).

A traditional way of modelling this is to make a relationship from Person to Site.cat, from this to Site.cat, from that to Site.cat etc. Not optimal. Having an external join table is not optimal either.

Instead we can simply create an _association_ by simply letting a Datom with a `Site.cat` attribute have a John id!: 

{{< bootstrap-table "table table-bordered" >}}
Entity id | Attribue       | Value    
:---:     | :---           | :---  
101       | :Person/name   | "John"
101       | :Person/likes  | "pizza"
101       | :Person/age    | 24
**101**   | **:Site/cat**  | **"customer"**
{{< /bootstrap-table >}}

In Molecule we associate with the `+` operator. We could therefore save John like this:

```scala
m(Person.name("John").age(24).likes("pizza") + Site.cat("customer")).save

// or

Person.name("John").age(24).likes("pizza").+(Site.cat("customer")).save
```
And we can retrieve "customers", also using the `+` operator:

```scala
// With tacit associated attribute category value "customer"
Person.name.age.likes.+(Site.cat_("customer")).get.head === ("John", 24, "pizza")
```

### Avoiding non-intrinsic model pollution

If we imagine that we had created a normal relationship from a Person namespace to a Site namespace in order to save the category connection, we could say that we had _polluted the semantic integrity of the Person namespace!_

A traditional relationships from `Person` to `Site` would simply not be _intrinsic_ to or a natural core part of what a `Person` is. 

Littering non-intrinsic relationships to `Site` - and possibly other cross-cutting namespaces like `Tags`, `Likes` etc - all over the place, would quickly clutter and pollute our Data Model.

Instead we want to create associative relationships to `Site`, `Tags`, `Likes` etc. 



## Composite molecules

We call molecules with one or more associations a _composite_ molecule, or simply a _composite_.

A composite contains two or more _sub-molecules_. Sub-molecules are like normal molecules.

When we get data with composites, each sub-molecule is returned as a sub-tuple, and a single value if it has only one attribute:

```scala
m(Person.name.likes.age + Site.cat).get === List(
  (("John", "pizza", 24), "customer")
)
```
This composite had two sub-molecules: `Person.name.likes.age` and `Site.cat`.

If the last sub-molecule had 2 attributes we would get a sub-tuple for that too:
```scala
m(Person.name.likes.age + Site.cat.status).get === List(
  (("John", "pizza", 24), ("customer", "good"))
)
```
And we can add even more sub-molecules...
```scala
m(Person.name.likes.age + Site.cat.status + Loc.tags + Emotion.like).get === List(
  (("John", "pizza", 24), ("customer", "good"), Set("inner city", "hipster"), true)
)
```

And expressions too...
```scala
// Which positive elder hipster customers like what?
m(Person.name.likes.age_.>(35) 
 + Site.cat_("customer")
 + Loc.tags_("hipster") 
 + Emotion.like_(true)).get === List(
  ("John", "pizza")
)
```
The combinations are quite endless - while you can keep your domain model/schema clean and intrinsic!


### Arity 22+ molecules

Composites can be composed of up to 22 sub-molecules! So, we can potentially insert and retrieve mega composite molecules with up to 22 x 22 = 484 attributes!

Since sub-molecules don't necessarily have to be about another namespace, we can simply use the same mechanism to add sub-molecules with more attributes from the same namespace. Here's an example of inserting composite data with 3 sub-molecules having 23 attributes in total:
```scala
// Insert composite data with 3 sub-molecules
Ns.bool.bools.date.dates.double.doubles.enum.enums +
  Ns.float.floats.int.ints.long.longs.ref1 +
  Ns.refSub1.str.strs.uri.uris.uuid.uuids.refs1 insert Seq(
  // Two rows with tuples of 3 sub-tuples that type-safely match the 3 sub-molecules above
  (
    (true, Set(true), date1, Set(date2, date3), 1.0, Set(2.0, 3.0), "enum1", Set("enum2", "enum3")),
    (1f, Set(2f, 3f), 1, Set(2, 3), 1L, Set(2L, 3L), r1),
    (r2, "a", Set("b", "c"), uri1, Set(uri2, uri3), uuid1, Set(uuid2), Set(42L))
  ),
  (
    (false, Set(false), date4, Set(date5, date6), 4.0, Set(5.0, 6.0), "enum4", Set("enum5", "enum6")),
    (4f, Set(5f, 6f), 4, Set(5, 6), 4L, Set(5L, 6L), r3),
    (r4, "d", Set("e", "f"), uri4, Set(uri5, uri6), uuid4, Set(uuid5), Set(43L))
  )
)
```
Retrieve the same composite data:
```scala
m(Ns.bool.bools.date.dates.double.doubles.enum.enums +
  Ns.float.floats.int.ints.long.longs.ref1 +
  Ns.refSub1.str.strs.uri.uris.uuid.uuids.refs1).get === Seq(
  (
    (false, Set(false), date4, Set(date5, date6), 4.0, Set(5.0, 6.0), "enum4", Set("enum5", "enum6")),
    (4f, Set(5f, 6f), 4, Set(5, 6), 4L, Set(5L, 6L), r3),
    (r4, "d", Set("e", "f"), uri4, Set(uri5, uri6), uuid4, Set(uuid5), Set(42L))
  ),
  (
    (true, Set(true), date1, Set(date2, date3), 1.0, Set(2.0, 3.0), "enum1", Set("enum2", "enum3")),
    (1f, Set(2f, 3f), 1, Set(2, 3), 1L, Set(2L, 3L), r1),
    (r2, "a", Set("b", "c"), uri1, Set(uri2, uri3), uuid1, Set(uuid2), Set(43L))
  )
)
```

..or a subset of the same composite data:
```scala
m(Ns.bool.bools.date.dates +
  Ns.float.floats.int +
  Ns.refSub1.str.strs).get === Seq(
  (
    (false, Set(false), date4, Set(date5, date6)),
    (4f, Set(5f, 6f), 4),
    (r4, "d", Set("e", "f"))
  ),
  (
    (true, Set(true), date1, Set(date2, date3)),
    (1f, Set(2f, 3f), 1),
    (r2, "a", Set("b", "c"))
  )
)
```

Since the subset uses less than 22 attributes, we can return single tuples without the need for a composite:
```scala
m(Ns.bool.bools.date.dates
  .float.floats.int
  .refSub1.str.strs).get === Seq(
  (
    false, Set(false), date1, Set(date2, date3),
    1f, Set(2f, 3f), 1,
    r2, "a", Set("b", "c")
  ),
  (
    true, Set(true), date4, Set(date5, date6),
    4f, Set(5f, 6f), 4,
    r4, "d", Set("e", "f")
  )
)
```




### Next

[Transactions...](/code/transactions)
