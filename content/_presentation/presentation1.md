---
date: 2015-01-02T22:06:44+01:00
title: "Presentation"
weight: 0
---

# Molecule presentation sample code snippets




```scala
fredId.retract

Person.name("Fred").get.size === 0 
```


```scala
Person.id.nameMap.one === (101, Map(
    "en" -> "Dmitri Shostakovich",
    "de" -> "Dmitri Schostakowitsch",
    "fr" -> "Dmitri Chostakovitch"
  ))
  
// By key
Person.nameMapK("fr").one === "Dmitri Chostakovitch"

// By value
Person.id.nameMap_("Dmitri Chostakovitch").one === 101
Person.id.nameMap_(".*Shosta.*").one === 101

// By key and value
Person.id.nameMap_("en" -> "Dmitri Shostakovich").one === 101
Person.id.nameMap_("en")(".*Shosta.*").one === 101
```

```scala
Person.id.nameMap.one === (101, Map(
    "en" -> "Dmitri Shostakovich",
    "de" -> "Dmitri Schostakowitsch",
    "fr" -> "Dmitri Chostakovitch"
  ))
  
// By key
Person.nameMapK("fr").one === "Dmitri Chostakovitch"
Person.nameMapK("fr|en").one === "Dmitri Chostakovitch"
```


```scala
// In definition file
val nameMap = mapString
 
// Insert mapped data
Person.id.nameMap.insert(
  101,
  Map(
    "en" -> "Dmitri Shostakovich",
    "de" -> "Dmitri Schostakowitsch",
    "fr" -> "Dmitri Chostakovitch",
    "es" -> "Dmitri Shostakóvich"
  )
)
```


```scala
fredId.retract

Person.name("Fred").get.size === 0 
```


```scala
Person(bob).hobbies.add("stars").update
Person(bob).hobbies.get.head === Set("golf", "cars", "stars")

Person(bob).hobbies.replace("cars" -> "trains").update
Person(bob).hobbies.get.head === Set("golf", "trains", "stars")

Person(bob).hobbies.remove("golf").update
Person(bob).hobbies.get.head === Set("trains", "stars")

Person(bob).hobbies("skiing", "stamps").update
Person(bob).hobbies.get.head === Set("skiing", "stamps")

Person(bob).hobbies().update
Person(bob).hobbies.get.head === Set()
```


```scala
// Assert new fact
Person(fred).likes("pasta").update
Person(fred).likes$.get.head === Some("pasta")

// Retract old fact
Person(fred).likes().update
Person(fred).likes$.get.head === None
```

```scala
// Assert new fact
Person(fred).likes("pasta").update
Person(fred).likes$.get.head === Some("pasta")
```

```scala
Person(fred).likes("pasta").update
Person(fred).likes$.get.head === Some("pasta")
```


```scala
// Re-use insert molecules
val insertPerson = Person.name.likes$.age.insert
val fredId = insertPerson("Fred", Some("pizza"), 38).eid 
```

```scala
Person.name.age.likes$ insert List(
  ("Fred", 38, Some("pizza")),
  ("Lisa", 7, None),
  ("Ben", 5, Some("pizza"))
)

// Re-use insert-molecules
val insertPerson = Person.name.age.likes$.insert
val fred = insertPerson("Fred", 38, Some("pizza")).eid
val ben  = insertPerson("Lisa", 7, None).eid
```

```scala
Person.name.likes$.age insert List(
  ("Fred", Some("pizza"), 38),
  ("Lisa", None, 7),
  ("Ben", Some("pizza"), 5)
)
```

```scala
Person
  .name("Fred")
  .age(38)
  .likes("pizza").save
```

```scala
Person.name.age.likes$.get === List(
  ("Fred", 38, Some("pizza")),
  ("Lisa", 7, None),
  ("Ben", 5, Some("pizza"))
)
```

```scala
Person.name.age.likes$ insert List(
  ("Fred", 38, Some("pizza")),
  ("Lisa", 7, None),
  ("Ben", 5, Some("pizza"))
)
```

```scala
// Save 1 molecule
Person.name("Fred").likes("pizza").age(38).save

// Insert
Person.name.likes$.age insert List(
  ("Fred", Some("pizza"), 38),
  ("Lisa", None, 7),
  ("Ben", Some("pizza"), 5)
)
```



```scala
Person.name("Fred").likes("pizza").age(38).save
```


```scala
m(Person.name.likes.age ~ Site.cat.status ~ Loc.tag).get === List(
  (("Fred", "pizza", 38), ("customer", "good"), "city")
)
```


```scala
m(Person.name.likes.age ~ Site.cat.status).get === List(
  (("Fred", "pizza", 38), ("customer", "good"))
)
```

```scala
m(Person.name.likes.age ~ Site.cat).get === List(
  (("Fred", "pizza", 38), "customer")
)
```

```scala
m(Person.name.likes.age ~ Site.cat).get === List(
  (("Fred", "pizza", 38), "customer")
)

val ((name, likes, age), cat) = m(Person.name.likes.age ~ Site.cat).get.head
```
```scala
Person.name.likes.age ~ Site.cat
```
```scala
object YourDomainDefinition {
  trait Person {
    val name  = oneString
    val likes = oneString
    val age   = oneInt
  }
  trait Site {
    val cat    = oneString
    val status = oneString
  }
}
```

```scala
Person.name.likes.age.Site.cat
```
```scala
object YourDomainDefinition {
  trait Person {
    val name  = oneString
    val likes = oneString
    val age   = oneInt
    val site  = one[Site] // Not intrinsic!
  }
  trait Site {
    val cat    = oneString
    val status = oneString
  }
}
```

```scala
Person.name.likes.age.Site.cat
```
```scala
object YourDomainDefinition {
  trait Person {
    val name  = oneString
    val likes = oneString
    val age   = oneInt
    val site  = one[Site]
  }
  trait Site {
    val cat    = oneString
    val status = oneString
  }
}
```

```scala
Person.name.likes.age .. ? .. Site.cat
```
```scala
object YourDomainDefinition {
  trait Person {
    val name  = oneString
    val likes = oneString
    val age   = oneInt
  }
  trait Site {
    val cat    = oneString
    val status = oneString
  }
}
```



```scala
fredId.touch === Map( // Map[String, Any]
  ":db/id" -> 101L,
  ":person/name" -> "Fred",
  ":person/likes" -> "pizza",
  ":person/age" -> 38,
  ":site/cat" -> "customer"
)

val siteCat_? : Option[String] = fredId[String](":site/cat")
```

```scala
fredId.touch === Map(
  ":db/id" -> 101L,
  ":person/name" -> "Fred",
  ":person/likes" -> "pizza",
  ":person/age" -> 38,
  ":site/cat" -> "customer"
)
```

```scala
m(Order.orderid.LineItems * (
  LineItem.product.price.quantity.Comments * (
    Comment.text.descr.Authors * Person.name))).get === List(
      (23, List(
        (chocolateId, 48.00, 1, List(
          ("first", "1a", List("Marc Grue")),
          ("product", "1b", List("Marc Grue")))),
        (whiskyId, 38.00, 2, List(
          ("second", "2b", List("Don Juan", "Stuart Halloway")),
          ("is", "2b", List("Nick Smith")),
          ("best", "2c", List("test"))))
      ))
    )
```

```scala
// Touch entity facts hierarchy recursively
orderId.touch === Map(
  ":db/id" -> 101L,
  ":order/lineItems" -> List(
    Map(
      ":db/id" -> 102L, 
      ":lineItem/qty" -> 3, 
      ":lineItem/product" -> "Milk",
      ":lineItem/price" -> 12.0),
    Map(
      ":db/id" -> 103L, 
      ":lineItem/qty" -> 2, 
      ":lineItem/product" -> "Coffee",
      ":lineItem/price" -> 46.0)))

// Optional typed attribute value
fredId[String](":person/likes") === Some("pizza")
```

```scala
// Nested molecule
m(Order.id.Items * LineItem.qty.product.price).get === List(
  (1, List(
    (3, "Milk", 12.0), 
    (2, "Coffee", 46.0)))
)

// Entity api - .touch
101.touch === Map(
  ":db/id" -> 101L,
  ":order/lineItems" -> List(
    Map(
      ":db/id" -> 102L, 
      ":lineItem/qty" -> 3, 
      ":lineItem/product" -> "Milk",
      ":lineItem/price" -> 12.0),
    Map(
      ":db/id" -> 103L, 
      ":lineItem/qty" -> 2, 
      ":lineItem/product" -> "Coffee",
      ":lineItem/price" -> 46.0)))
```


```scala
// Adjacent facts
Order.id.Items.qty.product.price.get === List(
  (1, 3, "Milk", 12.0),
  (1, 2, "Coffee", 46.0)
)

// Nested facts
m(Order.id.Items * LineItem.qty.product.price).get === List(
  (1, List(
    (3, "Milk", 12.0), 
    (2, "Coffee", 46.0)))
)
```

```scala
// Adjacent facts
Order.id.Items.qty.product.price.get === List(
  (1, 3, "Milk", 12.0),
  (1, 2, "Coffee", 46.0)
)
```

```scala
object OrderDefinition {

  trait Order {
    val id    = oneInt
    val items = many[LineItem].isComponent
  }

  trait LineItem {
    val qty     = oneInt
    val product = oneString
    val price   = oneDouble
  }
}
```


```scala
object ProductsOrderDefinition {

  trait Order {
    val id    = oneInt
    val items = many[LineItem].isComponent
  }

  trait LineItem {
    val product = one[Product]
    val price   = oneDouble
    val qty     = oneInt
  }

  trait Product {
    val description = oneString
  }
}
```

```scala
Person.name
  .Home.street.city._Person
  .Work.street.city.one === (
  "Fred", 
  "Baker st. 7", "Boston", 
  "Downtown 1", "Boston"
)
```

```scala
Person.name
  .Home.street.city._Person
  .Cottage.street.city._Person
  .Work.street.city.one === (
  "Fred", 
  "Baker st. 7", "Boston", 
  "Mountain way 32", "Woods town", 
  "Downtown 1", "Boston"
)
```


```scala
Person.name.Home.street.city. ..??
```

```scala
object YourDomainDefinition {
  trait Person {
    val name = oneString
    val age  = oneInt
    val home = one[Addr]
  }
  trait Addr {
    val street = oneString
    val city   = oneString
  }
}
```

```scala
object YourDomainDefinition {
  trait Person {
    val name = oneString
    val age  = oneInt
    val home = one[Addr]
    val work = one[Addr]
  }
  trait Addr {
    val street = oneString
    val city   = oneString
  }
}
```

```scala
Person.name.Home.street.city
Person.name.Work.street.city
```
```scala
Person.name.Home.street.city
```

```scala
Person.name.Home.street.city.one === (
  "Fred", "Baker St. 7", "Boston"
)
```


```scala
Person.name.age(5 or 6 or 7).get === List(
  ("Lisa", 7),
  ("Ben", 5)
)
Person.name.age(5, 6, 7).get === List(
  ("Lisa", 7),
  ("Ben", 5)
)
Person.name.age(Seq(5, 6, 7)).get === List(
  ("Lisa", 7),
  ("Ben", 5)
)
```

```scala
Person.age(min).one           === 5 
Person.age(max).one           === 38 
Person.age(sum).one           === 50 
Person.age(count).one         === 3 
Person.age(countDistinct).one === 3 
Person.age(avg).one           === 16.66666667
Person.age(median).one        === 7 
Person.age(variance).one      === 228.2222222222222 
Person.age(stddev).one        === 15.107025591499546
 
Person.age(distinct).one  === Vector(5, 7, 38) 
Person.age(min(2)).one    === Vector(5, 7) 
Person.age(max(2)).one    === Vector(38, 7) 
Person.age(rand(2)).one   === Stream(5, ?) 
Person.age(sample(2)).one === Vector(7, 38) 
```

```scala
Person.name.age.>(7).get === List(
  ("Fred", 38)
)
Person.name.age.>=(7).get === List(
  ("Fred", 38),
  ("Lisa", 7)
)
Person.name.age.<(7).get === List(
  ("Ben", 5)
)
Person.name.age.<=(7).get === List(
  ("Lisa", 7),
  ("Ben", 5)
)
```

```scala
Person.name.likes_(nil).get === List(
  "Lisa"
)
```
```scala
Person.name.age.not(38).get === List(
  ("Lisa", 7),
  ("Ben", 5)
)
Person.name.age.!=(38).get === List(
  ("Lisa", 7),
  ("Ben", 5)
)
```
```scala
// Equality - exact matches only
Person.name("Fred").age.get === List()

Person.name("Fred Ben").age.get === List(
  ("Fred Ben", 38)
)
```
```scala
Person.name.contains("Ben").get === List(
  "Fred Ben", "Ben"
)

Person.name.contains("Be").get === List()
```
```scala
Person.name.age_(38).get === List(
  "Fred"
)
```

```scala
Person.name.likes_("pizza").get === List(
  "Fred", "Ben"
)
```
```scala
Person.name.likes("pizza").get === List(
  ("Fred", "pizza"),
  ("Ben", "pizza")
)
```

```scala
Person.name.get === List(
  "Fred", "Lisa", "Ben"
)
```
```scala
Person.name.get: Iterable[String] === List(
  "Fred", "Lisa", "Ben"
)
```
```scala
Person.name.age.get: Iterable[(String, Int)] === List(
  ("Fred", 38),
  ("Lisa", 7),
  ("Ben", 5)
)
```
```scala
Person.name.age.likes.get === List(
  ("Fred", 38, "pizza"),
  ("Ben", 5, "pizza")
)
```
```scala
Person.name.age.likes_.get === List(
  ("Fred", 38),
  ("Ben", 5)
)
```
```scala
Person.name.age.likes$.get === List(
  ("Fred", 38, Some("pizza")),
  ("Lisa", 7, None),
  ("Ben", 5, Some("pizza"))
)
```
```scala
Person.e(101).name.likes.age.get === List(
  (101, "Fred", "pizza", 38)
)
```
```scala
Person.name.age
```
```scala
val persons: Iterable[(String, Option[String])] = Person.name.age_.likes$.get
```
```scala
Person.name.age.get
```
```scala
Person.name("Fred").age(39).saveD
```
```scala
Person.name.age.updateD
```

```scala
Person.name.age extends M2[String, Int]
```

```scala
object oneString
object oneInt
...
```

```scala
object YourDomainDefinition {
  trait Person {
    val name = oneString
    val age  = oneInt
  }
}
```

```scala
trait M1[A] {
  def get: Iterable[A] = ???
}
trait M2[A, B] {
  def get: Iterable[(A, B)] = ???
}

object Person {
  val name : Person1[String] = ???
  val age  : Person1[Int]    = ???
}
class Person1[A] extends M1[A] {
  val name : Person2[A, String] = ???
  val age  : Person2[A, Int]    = ???
}
class Person2[A, B] extends M2[A, B]
```

```scala
implicit def m[A, B](dsl: M2[A, B]): Molecule2[String, Int] = macro from2attr[A, B]
```

```scala
implicit def m[A, B](dsl: M2[A, B]): Molecule2[A, B] = macro from2attr[A, B]

// Macro implementation (simplified)
def from2attr[A, B](dsl: M2[A, B]): Molecule2[A, B] = {
  val query = ??? // todo
  q"""
    new Molecule2[A, B] {
      def get: Iterable[(A, B)] = datomic.Peer.q(${query}, conn.db).asScala.map(
        _.asScala match {
          case Iterable(a, b) => (a.asInstanceOf[A], b.asInstanceOf[B])
        }
      )
    }
  """
}
```

```scala
// Macro implementation (simplified)
def from2attr[A, B](dsl: M2[A, B]): Molecule2[A, B] = {

  def traverse(dsl: Tree, elements: Seq[Attr] = Nil): Seq[Attr] = dsl match {
    case q"$prev.$attr" => traverse(prev) :+ Attr(attr.ns, attr.name, attr.tpe, attr.card)
    case q"$ns"         => elements
  }
  val query = traverse(dsl.tree).toQueryString
  
  q"""
    new Molecule2[A, B] {
      def get: Iterable[(A, B)] = datomic.Peer.q(${query}, conn.db).asScala.map(
        _.asScala match {
          case Seq(a, b) => (a.asInstanceOf[A], b.asInstanceOf[B])
        }
      )
    }
  """
}
```

## 1 ## output.Molecule.saveD 
========================================================================
Model(
  Atom(person,name,String,1,Eq(List(Fred)),None,List(),List())
  Atom(person,age,Long,1,Eq(List(39)),None,List(),List()))
-------------------------------------
List(
  :db/add   'tempId                          :person/name    Values(Eq(List(Fred)),None)
  :db/add   'e                               :person/age     Values(Eq(List(39)),None))
---------------------------------
List(
  :db/add   #db/id[:db.part/user -1000008]   :person/name    Fred                       
  :db/add   #db/id[:db.part/user -1000008]   :person/age     39)
========================================================================



```scala
implicit def m[A, B](dsl: M2[A, B]): Molecule2[A, B] = 
  macro from2attr[A, B]

def from2attr[A: c.WeakTypeTag, B: c.WeakTypeTag]
    (c: Context)
    (dsl: c.Expr[M2[A, B]])
: c.Expr[Molecule2[A, B]] = {
  import c.universe._
  c.Expr(
    q"""
      new Molecule2[${c.weakTypeOf[A]}, ${c.weakTypeOf[B]}] {
        def get = ??? // query + casting...
      }
    """
  )
}
```

```scala
implicit def m[A, B](dsl: M2[A, B]): Molecule2[A, B] = macro from2attr[A, B]
```

```scala
implicit def m[A, B](dsl: M2[A, B]): Molecule2[A, B] = macro from2attr[A, B]

def from2attr[A: c.WeakTypeTag, B: c.WeakTypeTag]
    (c: Context)
    (dsl: c.Expr[M2[A, B]])
: c.Expr[Molecule2[A, B]] = {
  val query = ???
  c.Expr(
    q"""
      new Molecule2[${c.weakTypeOf[A]}, ${c.weakTypeOf[B]}] {
        def get = datomic.Peer.q(${query}, conn.db).asScala.map( _.asScala match {
            case Seq(a, b) =>
              (a.asInstanceOf[${c.weakTypeOf[A]}], b.asInstanceOf[${c.weakTypeOf[B]}])
          }
        )
      }
    """
  )
}
```

```scala
Person.name.age
```

```scala
val persons = Person.name.age.get
```
```scala
val persons = datomic.Peer.q(
  """
    |[:find ?name ?age
    | :where
    |   [?e :person/name ?name]
    |   [?e :person/age ?age]]
  """.stripMargin, conn.db)
```


```scala
val persons: Iterable[(String, Int)] = Person.name.age.get
```
```scala
val persons: java.util.Collection[java.util.List[Object]] = datomic.Peer.q(
  """
    |[:find ?name ?age
    | :where
    |   [?e :person/name ?name]
    |   [?e :person/age ?age]]
  """.stripMargin, conn.db)
```


```scala
val persons: Iterable[(String, Int)] = datomic.Peer.q(
  """
    |[:find ?name ?age
    | :where
    |   [?e :person/name ?name]
    |   [?e :person/age ?age]]
  """.stripMargin, conn.db
).asScala.map(_.asScala match {
    case Seq(name, age) =>
      (name.asInstanceOf[String], age.asInstanceOf[Long].toInt)
  }
)
```

```scala
val personsMolecule: Molecule2[String, Int] = m(Person.name.age)
```

```scala
new Molecule2[String, Int] {
  def get: Iterable[(String, Int)] = datomic.Peer.q(
    """
      |[:find ?name ?age
      | :where
      |   [?e :person/name ?name]
      |   [?e :person/age ?age]]
    """.stripMargin, conn.db
  ).asScala.map( _.asScala match {
    case Seq(name, age) => 
      (name.asInstanceOf[String], age.asInstanceOf[Long].toInt)
    }
  )
```

```scala
val personM: Molecule2[String, Int] = m(Person.name.age)
val persons: Iterable[(String, Int)] = personM.get
```

```scala
// compile-time
val personMolecule: Molecule2[String, Int] = m(Person.name.age)

// runtime
val persons: Iterable[(String, Int)] = personMolecule.get
```

```scala
implicit def m[A, B](dsl: M2[A, B]) = macro MakeMolecule.from2attr[A, B]

// compile-time
val personMolecule: Molecule2[String, Int] = m(Person.name.age)

// runtime
val persons: Iterable[(String, Int)] = personMolecule.get

// compile-time + runtime
val persons: Iterable[(String, Int)] = Person.name.age.get
```

```scala
// implicit def macro
implicit def m[A, B](dsl: M2[A, B]) = macro MakeMolecule.from2attr[A, B]

// compile-time + runtime
val persons: Iterable[(String, Int)] = Person.name.age.get
```

```scala
// implicit def macro
implicit def m[A, B](dsl: M2[A, B]): Molecule2[String, Int] = macro from2attr[A, B]

// compile-time
val personMolecule: Molecule2[String, Int] = m(Person.name.age)

// runtime
val persons: Iterable[(String, Int)] = personMolecule.get

// compile-time + runtime
val persons: Iterable[(String, Int)] = Person.name.age.get
```



-------
