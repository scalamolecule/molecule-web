---
title: "Presentation"
weight: 0
---

# Molecule presentation sample code snippets




```
fredId.retract

Person.name("Fred").get.size === 0 
```


```
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

```
Person.id.nameMap.one === (101, Map(
    "en" -> "Dmitri Shostakovich",
    "de" -> "Dmitri Schostakowitsch",
    "fr" -> "Dmitri Chostakovitch"
  ))
  
// By key
Person.nameMapK("fr").one === "Dmitri Chostakovitch"
Person.nameMapK("fr|en").one === "Dmitri Chostakovitch"
```


```
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


```
fredId.retract

Person.name("Fred").get.size === 0 
```


```
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


```
// Assert new fact
Person(fred).likes("pasta").update
Person(fred).likes$.get.head === Some("pasta")

// Retract old fact
Person(fred).likes().update
Person(fred).likes$.get.head === None
```

```
// Assert new fact
Person(fred).likes("pasta").update
Person(fred).likes$.get.head === Some("pasta")
```

```
Person(fred).likes("pasta").update
Person(fred).likes$.get.head === Some("pasta")
```


```
// Re-use insert molecules
val insertPerson = Person.name.likes$.age.insert
val fredId = insertPerson("Fred", Some("pizza"), 38).eid 
```

```
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

```
Person.name.likes$.age insert List(
  ("Fred", Some("pizza"), 38),
  ("Lisa", None, 7),
  ("Ben", Some("pizza"), 5)
)
```

```
Person
  .name("Fred")
  .age(38)
  .likes("pizza").save
```

```
Person.name.age.likes$.get === List(
  ("Fred", 38, Some("pizza")),
  ("Lisa", 7, None),
  ("Ben", 5, Some("pizza"))
)
```

```
Person.name.age.likes$ insert List(
  ("Fred", 38, Some("pizza")),
  ("Lisa", 7, None),
  ("Ben", 5, Some("pizza"))
)
```

```
// Save 1 molecule
Person.name("Fred").likes("pizza").age(38).save

// Insert
Person.name.likes$.age insert List(
  ("Fred", Some("pizza"), 38),
  ("Lisa", None, 7),
  ("Ben", Some("pizza"), 5)
)
```



```
Person.name("Fred").likes("pizza").age(38).save
```


```
m(Person.name.likes.age ~ Site.cat.status ~ Loc.tag).get === List(
  (("Fred", "pizza", 38), ("customer", "good"), "city")
)
```


```
m(Person.name.likes.age ~ Site.cat.status).get === List(
  (("Fred", "pizza", 38), ("customer", "good"))
)
```

```
m(Person.name.likes.age ~ Site.cat).get === List(
  (("Fred", "pizza", 38), "customer")
)
```

```
m(Person.name.likes.age ~ Site.cat).get === List(
  (("Fred", "pizza", 38), "customer")
)

val ((name, likes, age), cat) = m(Person.name.likes.age ~ Site.cat).get.head
```
```
Person.name.likes.age ~ Site.cat
```
```
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

```
Person.name.likes.age.Site.cat
```
```
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

```
Person.name.likes.age.Site.cat
```
```
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

```
Person.name.likes.age .. ? .. Site.cat
```
```
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



```
fredId.touch === Map( // Map[String, Any]
  ":db/id" -> 101L,
  ":Person/name" -> "Fred",
  ":Person/likes" -> "pizza",
  ":Person/age" -> 38,
  ":Site/cat" -> "customer"
)

val siteCat_? : Option[String] = fredId[String](":Site/cat")
```

```
fredId.touch === Map(
  ":db/id" -> 101L,
  ":Person/name" -> "Fred",
  ":Person/likes" -> "pizza",
  ":Person/age" -> 38,
  ":Site/cat" -> "customer"
)
```

```
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

```
// Touch entity facts hierarchy recursively
orderId.touch === Map(
  ":db/id" -> 101L,
  ":Order/lineItems" -> List(
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

// Optional typed attribute value
fredId[String](":Person/likes") === Some("pizza")
```

```
// Nested molecule
m(Order.id.Items * LineItem.qty.product.price).get === List(
  (1, List(
    (3, "Milk", 12.0), 
    (2, "Coffee", 46.0)))
)

// Entity api - .touch
101.touch === Map(
  ":db/id" -> 101L,
  ":Order/lineItems" -> List(
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


```
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

```
// Adjacent facts
Order.id.Items.qty.product.price.get === List(
  (1, 3, "Milk", 12.0),
  (1, 2, "Coffee", 46.0)
)
```

```
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


```
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

```
Person.name
  .Home.street.city._Person
  .Work.street.city.one === (
  "Fred", 
  "Baker st. 7", "Boston", 
  "Downtown 1", "Boston"
)
```

```
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


```
Person.name.Home.street.city. ..??
```

```
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

```
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

```
Person.name.Home.street.city
Person.name.Work.street.city
```
```
Person.name.Home.street.city
```

```
Person.name.Home.street.city.one === (
  "Fred", "Baker St. 7", "Boston"
)
```


```
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

```
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

```
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

```
Person.name.likes_(nil).get === List(
  "Lisa"
)
```
```
Person.name.age.not(38).get === List(
  ("Lisa", 7),
  ("Ben", 5)
)
Person.name.age.!=(38).get === List(
  ("Lisa", 7),
  ("Ben", 5)
)
```
```
// Equality - exact matches only
Person.name("Fred").age.get === List()

Person.name("Fred Ben").age.get === List(
  ("Fred Ben", 38)
)
```
```
Person.name.contains("Ben").get === List(
  "Fred Ben", "Ben"
)

Person.name.contains("Be").get === List()
```
```
Person.name.age_(38).get === List(
  "Fred"
)
```

```
Person.name.likes_("pizza").get === List(
  "Fred", "Ben"
)
```
```
Person.name.likes("pizza").get === List(
  ("Fred", "pizza"),
  ("Ben", "pizza")
)
```

```
Person.name.get === List(
  "Fred", "Lisa", "Ben"
)
```
```
Person.name.get: List[String] === List(
  "Fred", "Lisa", "Ben"
)
```
```
Person.name.age.get: List[(String, Int)] === List(
  ("Fred", 38),
  ("Lisa", 7),
  ("Ben", 5)
)
```
```
Person.name.age.likes.get === List(
  ("Fred", 38, "pizza"),
  ("Ben", 5, "pizza")
)
```
```
Person.name.age.likes_.get === List(
  ("Fred", 38),
  ("Ben", 5)
)
```
```
Person.name.age.likes$.get === List(
  ("Fred", 38, Some("pizza")),
  ("Lisa", 7, None),
  ("Ben", 5, Some("pizza"))
)
```
```
Person.e(101).name.likes.age.get === List(
  (101, "Fred", "pizza", 38)
)
```
```
Person.name.age
```
```
val persons: List[(String, Option[String])] = Person.name.age_.likes$.get
```
```
Person.name.age.get
```
```
Person.name("Fred").age(39).saveD
```
```
Person.name.age.updateD
```

```
Person.name.age extends M2[String, Int]
```

```
object oneString
object oneInt
...
```

```
object YourDomainDefinition {
  trait Person {
    val name = oneString
    val age  = oneInt
  }
}
```

```
trait M1[A] {
  def get: List[A] = ???
}
trait M2[A, B] {
  def get: List[(A, B)] = ???
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

```
implicit def m[A, B](dsl: M2[A, B]): Molecule2[String, Int] = macro from2attr[A, B]
```

```
implicit def m[A, B](dsl: M2[A, B]): Molecule2[A, B] = macro from2attr[A, B]

// Macro implementation (simplified)
def from2attr[A, B](dsl: M2[A, B]): Molecule2[A, B] = {
  val query = ??? // todo
  q"""
    new Molecule2[A, B] {
      def get: List[(A, B)] = datomic.Peer.q(${query}, conn.db).asScala.map(
        _.asScala match {
          case List(a, b) => (a.asInstanceOf[A], b.asInstanceOf[B])
        }
      )
    }
  """
}
```

```
// Macro implementation (simplified)
def from2attr[A, B](dsl: M2[A, B]): Molecule2[A, B] = {

  def traverse(dsl: Tree, elements: Seq[Attr] = Nil): Seq[Attr] = dsl match {
    case q"$prev.$attr" => traverse(prev) :+ Attr(attr.ns, attr.name, attr.tpe, attr.card)
    case q"$ns"         => elements
  }
  val query = traverse(dsl.tree).toQueryString
  
  q"""
    new Molecule2[A, B] {
      def get: List[(A, B)] = datomic.Peer.q(${query}, conn.db).asScala.map(
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
  :db/add   'tempId                          :Person/name    Values(Eq(List(Fred)),None)
  :db/add   'e                               :Person/age     Values(Eq(List(39)),None))
---------------------------------
List(
  :db/add   #db/id[:db.part/user -1000008]   :Person/name    Fred                       
  :db/add   #db/id[:db.part/user -1000008]   :Person/age     39)
========================================================================



```
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

```
implicit def m[A, B](dsl: M2[A, B]): Molecule2[A, B] = macro from2attr[A, B]
```

```
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

```
Person.name.age
```

```
val persons = Person.name.age.get
```
```
val persons = datomic.Peer.q(
  """
    |[:find ?name ?age
    | :where
    |   [?e :Person/name ?name]
    |   [?e :Person/age ?age]]
  """.stripMargin, conn.db)
```


```
val persons: List[(String, Int)] = Person.name.age.get
```
```
val persons: java.util.Collection[java.util.List[Object]] = datomic.Peer.q(
  """
    |[:find ?name ?age
    | :where
    |   [?e :Person/name ?name]
    |   [?e :Person/age ?age]]
  """.stripMargin, conn.db)
```


```
val persons: List[(String, Int)] = datomic.Peer.q(
  """
    |[:find ?name ?age
    | :where
    |   [?e :Person/name ?name]
    |   [?e :Person/age ?age]]
  """.stripMargin, conn.db
).asScala.map(_.asScala match {
    case Seq(name, age) =>
      (name.asInstanceOf[String], age.asInstanceOf[Long].toInt)
  }
)
```

```
val personsMolecule: Molecule2[String, Int] = m(Person.name.age)
```

```
new Molecule2[String, Int] {
  def get: List[(String, Int)] = datomic.Peer.q(
    """
      |[:find ?name ?age
      | :where
      |   [?e :Person/name ?name]
      |   [?e :Person/age ?age]]
    """.stripMargin, conn.db
  ).asScala.map( _.asScala match {
    case Seq(name, age) => 
      (name.asInstanceOf[String], age.asInstanceOf[Long].toInt)
    }
  )
```

```
val personM: Molecule2[String, Int] = m(Person.name.age)
val persons: List[(String, Int)] = personM.get
```

```
// compile-time
val personMolecule: Molecule2[String, Int] = m(Person.name.age)

// runtime
val persons: List[(String, Int)] = personMolecule.get
```

```
implicit def m[A, B](dsl: M2[A, B]) = macro MakeMolecule.from2attr[A, B]

// compile-time
val personMolecule: Molecule2[String, Int] = m(Person.name.age)

// runtime
val persons: List[(String, Int)] = personMolecule.get

// compile-time + runtime
val persons: List[(String, Int)] = Person.name.age.get
```

```
// implicit def macro
implicit def m[A, B](dsl: M2[A, B]) = macro MakeMolecule.from2attr[A, B]

// compile-time + runtime
val persons: List[(String, Int)] = Person.name.age.get
```

```
// implicit def macro
implicit def m[A, B](dsl: M2[A, B]): Molecule2[String, Int] = macro from2attr[A, B]

// compile-time
val personMolecule: Molecule2[String, Int] = m(Person.name.age)

// runtime
val persons: List[(String, Int)] = personMolecule.get

// compile-time + runtime
val persons: List[(String, Int)] = Person.name.age.get
```



-------
