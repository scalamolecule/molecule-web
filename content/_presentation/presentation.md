---
date: 2015-01-02T22:06:44+01:00
title: "Presentation"
weight: 0
---

# Molecule presentation sample code snippets



```scala
Person.id.nameMap.get.head === (101, Map(
    "en" -> "Dmitri Shostakovich",
    "de" -> "Dmitri Schostakowitsch",
    "fr" -> "Dmitri Chostakovitch"
  ))
  
// By key
Person.nameMapK("fr").get.head === "Dmitri Chostakovitch"

// By value
Person.id.nameMap_("Dmitri Chostakovitch").get.head === 101
Person.id.nameMap_(".*Shosta.*").get.head === 101

// By key and value
Person.id.nameMap_("en" -> "Dmitri Shostakovich").get.head === 101
Person.id.nameMap_("en")(".*Shosta.*").get.head === 101
```


```scala
Person.id.nameMap.get.head === (101, Map(
    "en" -> "Dmitri Shostakovich",
    "de" -> "Dmitri Schostakowitsch",
    "fr" -> "Dmitri Chostakovitch"
  ))
  
// By key
Person.nameMapK("fr").get.head === "Dmitri Chostakovitch"

// By value
Person.id.nameMap_("Dmitri Chostakovitch").get.head === 101
Person.id.nameMap_(".*Shosta.*").get.head === 101
```

```scala
Person.id.nameMap.get.head === (101, Map(
    "en" -> "Dmitri Shostakovich",
    "de" -> "Dmitri Schostakowitsch",
    "fr" -> "Dmitri Chostakovitch"
  ))
  
// By key
Person.nameMapK("fr").get.head === "Dmitri Chostakovitch"
```

```scala
Person.id.nameMap.get.head === (101, Map(
    "en" -> "Dmitri Shostakovich",
    "de" -> "Dmitri Schostakowitsch",
    "fr" -> "Dmitri Chostakovitch"
  ))
  
// By key
Person.nameMapK("fr").get.head === "Dmitri Chostakovitch"
Person.nameMapK("fr|en").get.head === "Dmitri Chostakovitch"
```

```scala
Person.name
  .Home.street.city._Person
  .Work.street.city.get.head === (
  "Fred", 
  "Baker st. 7", "Boston", 
  "Downtown 1", "Boston"
)
```

```scala
Person.name
  .Home.street.city._Person
  .Cottage.street.city._Person
  .Work.street.city.get.head === (
  "Fred", 
  "Baker st. 7", "Boston", 
  "Mountain way 32", "Woods town", 
  "Downtown 1", "Boston"
)
```


```scala
Person.name.Home.street.city.get.head === (
  "Fred", "Baker St. 7", "Boston"
)
```

```scala
Person.name.age.get  // "We need an M2 with a 'get' method!"
```

```scala
Person.name.age
```

```scala
Person.name.age  // extends M2[String, Int])
```

```scala
m(Person.name.age).get     ...or...       Person.name.age.get
```


```scala
implicit def m[A, B](dsl: M2[A, B]): Molecule2[A, B] = macro from2attr[A, B]
```


```scala
implicit def m[A, B](dsl: M2[A, B]): Molecule2[A, B] = macro from2attr[A, B]

// Macro implementation (simplified)
def from2attr[A, B](dsl: M2[A, B]): Molecule2[A, B] = {
  val query = ??? // todo
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


```scala


// Macro implementation (simplified)
def from2attr[A, B](dsl: M2[A, B]): Molecule2[A, B] = {
  val query = ??? // todo
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


```scala




  val query = ??? // todo
  q"""
    new Molecule2[A, B] {
      def get: List[(A, B)] = datomic.Peer.q(${query}, conn.db).asScala.map(
        _.asScala match {
          case Seq(a, b) => (a.asInstanceOf[A], b.asInstanceOf[B])
        }
      )
    }
  """

```


```scala





  q"""
    new Molecule2[A, B] {
      def get: List[(A, B)] = datomic.Peer.q(${query}, conn.db).asScala.map(
        _.asScala match {
          case Seq(a, b) => (a.asInstanceOf[A], b.asInstanceOf[B])
        }
      )
    }
  """

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
val (email, oldPassw) = User(eid).email.passw.get.head
```

```scala
val encryptedNewPassw = encrypt(newPassw)
User(eid).passw(encryptedNewPassw).save
```



```scala
// Actions
val saveJohn      = Person.str("John").int(44).saveTx
val insertMembers = Person.str.int insertTx List(("Lisa", 23), ("Pete", 24))
val updateFred    = Person(fred).int(43).updateTx

val base     = m(Person.name.age)
val expected = List(
  ("Fred", 43), // Updated
  ("John", 44), // Saved
  ("Lisa", 23), // Inserted
  ("Pete", 24)  // Inserted
)

// Test: Order of actions doesn't affect outcome
base.getWith(saveJohn, insertMembers, updateFred) === expected
base.getWith(saveJohn, updateFred, insertMembers) === expected
base.getWith(insertMembers, saveJohn, updateFred) === expected
base.getWith(insertMembers, updateFred, saveJohn) === expected
base.getWith(updateFred, saveJohn, insertMembers) === expected
base.getWith(updateFred, insertMembers, saveJohn) === expected
```



```scala
// Actions
val saveJohn      = Person.str("John").int(44).saveTx
val insertMembers = Person.str.int insertTx List(("Lisa", 23), ("Pete", 24))
val updateFred    = Person(fred).int(43).updateTx

val base     = m(Person.name.age)
val expected = List(
  ("Fred", 43), // Updated
  ("John", 44), // Saved
  ("Lisa", 23), // Inserted
  ("Pete", 24)  // Inserted
)

// Test: Order of actions doesn't affect outcome
base.getWith(saveJohn, insertMembers, updateFred).get.toSeq.sorted === expected
base.getWith(saveJohn, updateFred, insertMembers).get.toSeq.sorted === expected
base.getWith(insertMembers, saveJohn, updateFred).get.toSeq.sorted === expected
base.getWith(insertMembers, updateFred, saveJohn).get.toSeq.sorted === expected
base.getWith(updateFred, saveJohn, insertMembers).get.toSeq.sorted === expected
base.getWith(updateFred, insertMembers, saveJohn).get.toSeq.sorted === expected
```


```scala
val fred = Person.name("Fred").age(42).save.eid
Person.name.age.get === List(("Fred", 42))

Person.name.age.getWith(
  Person.name("John").age(44).saveTx,
  Person.name.age insertTx List(
    ("Lisa", 23),
    ("Pete", 24)
  ),
  Person(fred).age(43).updateTx
) === List(
  ("Fred", 43), // Updated
  ("John", 44), // Saved
  ("Lisa", 23), // Inserted
  ("Pete", 24)  // Inserted
)

// Nothing changed
Person.name.age.get === List(("Fred", 42))
```


```scala
val fred = Person.name("Fred").age(42).save.eid
Person.name.age.get === List(("Fred", 42))

Person.name.age.getWith(
  Person.name.age insertTx List(
    ("Lisa", 23),
    ("Pete", 24)
  )
) === List(
  ("Fred", 42), // unchanged
  ("Lisa", 23), // inserted
  ("Pete", 24)  // inserted
)

// Nothing changed
Person.name.age.get === List(("Fred", 42))
```

```scala
val fred = Person.name("Fred").age(42).save.eid
Person.name.age.get === List(("Fred", 42))

Person.name.age.getWith(
  Person.name("John").age(44).saveTx
) === List(
  ("Fred", 42), // unchanged
  ("John", 44)  // new saved
)

// Nothing changed
Person.name.age.get === List(("Fred", 42))
```

```scala
val fred = Person.name("Fred").age(42).save.eid
Person.name.age.get === List(("Fred", 42))

Person.name.age.getWith(
  Person(fred).age(43).updateTx
) === List(
  ("Fred", 43) // updated
)

// Nothing changed
Person.name.age.get === List(("Fred", 42))
```

```
List(
  [:db/add, 17592186045445, :ns/int, 43]
)
List(
    [:db/add, #db/id[:db.part/user -1000112], :ns/str, John], 
    [:db/add, #db/id[:db.part/user -1000112], :ns/int, 44]
)
List(
    [:db/add, #db/id[:db.part/user -1000113], :ns/str, Lisa], 
    [:db/add, #db/id[:db.part/user -1000113], :ns/int, 23], 
    [:db/add, #db/id[:db.part/user -1000114], :ns/str, Pete], 
    [:db/add, #db/id[:db.part/user -1000114], :ns/int, 24]
)
List(
    [:db/add, 17592186045445, :ns/int, 43]
)
```
```
List(
  [:db/add, 17592186045445, :ns/int, 43]
)
```

```scala
val fred = Person.name("Fred").age(42).save.eid
Person.name.age.get === List(("Fred", 42))

Person.name.age.getWith ( // previously `imagine`
  // Manipulate some data...
) === List(
  // Expected result?
)

// Nothing changed
Person.name.age.get === List(("Fred", 42))
```

```scala
val tx1 = Person.likes("pizza").save
val tx2 = Person.likes("pasta").save
val tx3 = Person.likes("sushi").save
val tx4 = Person.likes("burger").save

// Values since tx2 (tx2 itself excluded)
// (previously `since(tx2).get`)
Person.likes.getSince(tx2) === List("sushi", "burger")  
```

```scala
Person.e.likes.op.txInstant
  .tx_(Audit.user.uc).getHistory === List(
  (fred, "pizza", true, "2017-04-25 13:52:07", "Lisa", "survey"),
  (fred, "pizza", false, "2017-04-26 18:35:41", "Peter", "interview"),
  (fred, "pasta", true, "2017-04-26 18:35:41", "Peter", "interview")
)

Person.e.likes.op.txInstant
  .tx_(Audit.user_("Peter").uc_("interview")).getHistory === List(
  (fred, "pizza", false, "2017-04-26 18:35:41"),
  (fred, "pasta", true, "2017-04-26 18:35:41")
)

Person.e.likes_("pizza").op_(false).txInstant
  .tx_(Audit.uc_("interview")).getHistory === List(
  (fred, "2017-04-26 18:35:41")
)
```


```scala
Person.e.likes.op.txInstant
  .tx_(Audit.user.uc).getHistory === List(
  (fred, "pizza", true, "2017-04-25 13:52:07", "Lisa", "survey"),
  (fred, "pizza", false, "2017-04-26 18:35:41", "Peter", "interview"),
  (fred, "pasta", true, "2017-04-26 18:35:41", "Peter", "interview")
)

Person.e.likes.op.txInstant
  .tx_(Audit.user_("Peter").uc_("interview")).getHistory === List(
  (fred, "pizza", false, "2017-04-26 18:35:41"),
  (fred, "pasta", true, "2017-04-26 18:35:41")
)
```

```scala
Person.e.likes.op.txInstant
  .tx_(Audit.user.uc).getHistory === List(
  (fred, "pizza", true, "2017-04-25 13:52:07", "Lisa", "survey"),
  (fred, "pizza", false, "2017-04-26 18:35:41", "Peter", "interview"),
  (fred, "pasta", true, "2017-04-26 18:35:41", "Peter", "interview")
)
```


```scala
Person.name.likes.op.txInstant
  .tx_(Audit.user.uc).getHistory === List(
  ("Fred", "pizza", true, "2017-04-25 13:52:07", "Lisa", "survey"),
  ("Fred", "pizza", false, "2017-04-26 18:35:41", "Peter", "interview"),
  ("Fred", "pasta", true, "2017-04-26 18:35:41", "Peter", "interview")
)
```


```scala
Person(fred).likes.op
  .tx_(Audit.user.uc).getHistory === List(
  ("pizza", true),
  ("pizza", false),
  ("pasta", true)
)

Person.name.likes.op_(false).txInstant
  .tx_(Audit.user.uc).getHistory === List(
  ("Fred", "pizza", "2017-04-26 18:35:41", "Peter", "interview")
)
```

```scala
Person(fred).likes.op.getHistory === List(
  ("pizza", true),
  ("pizza", false),
  ("pasta", true)
)

Person(fred).likes.getHistory === List("pizza", "pasta")

Person(fred).likes.op_(false).getHistory === List("pizza")

Person.name_("Fred").likes.op_(false).txInstant.getHistory === List(
  ("pizza", "2017-04-26 18:35:41")
)
```


```scala
Person(fred).likes.op.getHistory === List(
  ("pizza", true),
  ("pizza", false),
  ("pasta", true)
)

Person(fred).likes.getHistory === List("pizza", "pasta")

Person(fred).likes.op_(false).getHistory === List("pizza")

Person.e.likes.op_(false).txInstant.getHistory === List(
  (fred, "pizza", "2017-04-26 18:35:41")
)
```


```scala
Person(fred).likes.op.getHistory === List(
  ("pizza", true),
  ("pizza", false),
  ("pasta", true)
)

Person(fred).likes.getHistory === List("pizza", "pasta")

Person(fred).likes.op_(false).getHistory === List("pizza")

Person.name.likes.op_(false).txInstant.getHistory === List(
  ("Fred", "pizza", "2017-04-26 18:35:41")
)
```

```scala
Person(fred).likes.op.getHistory === List(
  ("pizza", true),
  ("pizza", false),
  ("pasta", true)
)

Person(fred).likes.getHistory === List("pizza", "pasta")

Person(fred).likes.op_(false).getHistory === List("pizza")
```

```scala
Person(fred).likes.op.getHistory === List(
  ("pizza", true),
  ("pizza", false),
  ("pasta", true)
)

Person(fred).likes.getHistory === List("pizza", "pasta")
```

```scala
Db.e.a.v.tx.t.txInstant.op.getHistory === List(
  (fred, ":person/name",  "Fred",  tx1, t1, date1, true),
  (fred, ":person/likes", "pizza", tx1, t1, date1, true),
  (fred, ":person/likes", "pizza", tx2, t2, date2, false),
  (fred, ":person/likes", "pasta", tx2, t2, date2, true)
)
```


```scala
Person(fred).a.v.tx.t.txInstant.op.getHistory === List(
  (":person/name",  "Fred",  tx1, t1, date1, true),
  (":person/likes", "pizza", tx1, t1, date1, true),
  (":person/likes", "pizza", tx2, t2, date2, false),
  (":person/likes", "pasta", tx2, t2, date2, true)
)
```

```scala
Person.e.likes.tx.t.txInstant.op.getHistory === List(
  (fred, "pizza", tx1, t1, date1, true),
  (fred, "pizza", tx2, t2, date2, false),
  (fred, "pasta", tx2, t2, date2, true)
)
```

```scala
Person(fred).likes.t.op.getHistory === List(
  ("pizza", t1, true),
  ("pizza", t2, false),
  ("pasta", t2, true)
)
```

```scala
Person(fred).likes.op.getHistory === List(
  ("pizza", true),
  ("pizza", false),
  ("pasta", true)
)
```


```scala
Person(fred).likes.op.getHistory === List( // previously `history.get`
  ("pizza", true),
  ("pizza", false),
  ("pasta", true)
)
```



```scala
val result = Person.name("Fred").likes("pizza").save
val fred   = result.eid
val t1     = result.t
Person(fred).likes.get.head === "pizza"

val t2 = Person(fred).likes("pasta").update.t
Person(fred).likes.get.head === "pasta"

Person(fred).likes.getAsOf(t1).head === "pizza" // previously `asOf(t1).get`
Person(fred).likes.getAsOf(t2).head === "pasta"
```



```scala
val result = Person.name("Fred").likes("pizza").save
val fred   = result.eid
val t1     = result.t
Person(fred).likes.get.head === "pizza"

val t2 = Person(fred).likes("pasta").update.t
Person(fred).likes.get.head === "pasta"
```


```scala
val result = Person.name("Fred").likes("pizza").save
val fred   = result.eid
val t1     = result.t
Person(fred).likes.get.head === "pizza"
```


```scala
Person.likes.tx_(Audit.user_("Lisa").uc_("survey")).get === List("pizza")
```


```scala
Person.name.tx_(Audit.uc_("survey")).get === List("Fred")
```

```scala
Person(e5).name.txInstant.tx(Audit.user.uc).get === List( 
  ("Fred", "Tue Apr 26 18:35:41 CEST 2017", "Lisa", "survey"))
```

```scala
Person(e5).name.tx(Audit.user.uc).get === List(
  ("Fred", "Lisa", "survey"))
```

```scala
Person(e5).name.tx_(Audit.user.uc).get === List(("Fred", "Lisa", "survey"))
```

```scala
Person.name("Fred").likes("pizza")
  .tx_(Audit.user_("Lisa").uc_("survey")).save
```

```scala
trait Audit {
  val user = oneString
  val uc   = oneString
}
```

```scala
Person(e5).name_.txInstant.get.head === date1
Person(e5).name_.tx.get.head === t4
```

```scala
Person(e5).name_.txInstant.get.head === date1  // Tue Apr 26 18:35:41
```

```scala
Person(e5).name_.tx.get.head === tx4  // 13194139534340L
```

```scala
val e5 = Person.name("Fred").likes("pizza").save.eid
```
```scala
Person.name("Fred").likes("pizza").save
```

```scala
// Property graph
Person.name_("Ann").Knows.weight.Person.name.get === List((7, "Ben"))
Person.name_("Ben").Knows.weight.Person.name.get === List((7, "Ann"))
```


```scala
Person.name.Knows.*(Knows.weight.Person.name).insert(
  "Ann", List((7, "Ben"), (8, "Joe")))

Person.name_("Ann").Knows.weight.Person.name.get === 
  List((7, "Ben"), (8, "Joe"))

Person.name_("Ben").Knows.weight.Person.name.get === 
  List((7, "Ann"))

Person.name_("Joe").Knows.weight.Person.name.get === 
  List((8, "Ann"))
```


```scala
// Entity
object Person extends Person
trait Person {
  val knows = 
    manyBiEdge[Knows.person.type]
  
  val name = oneString
}

// Property edge
object Knows extends Knows
trait Knows {
  val person = 
    target[Person.knows.type]
  
  // Property
  val weight = oneInt
}
```


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

```scala
// Graph
Person.name("Ann").Friends.name("Ben").save
Person.name("Ben").Friends.name("Joe").save

Person.name_("Ann").Friends.Friends.name.not("Ann").get === List("Joe")
Person.name_("Joe").Friends.Friends.name.not("Joe").get === List("Ann")
```

```scala
// Graph
Person.name("Ann").Friends.name("Ben").save
Person.name("Ben").Friends.name("Joe").save

//                    Ben   Ann/Joe
Person.name_("Ann").Friends.Friends.name.get === List("Ann", "Joe")
```

```scala
Person.name("Ann").Friends.name("Ben").save

Person.name_("Ann").Friends.name.get === List("Ben")
Person.name_("Ben").Friends.name.get === List("Ann")

Ann --> Ben
Ben <-- Ann // reverse ref
```

```scala
trait Person {
  val name    = oneString
  val friends = manyBi[Person]
}
```

```scala
Person.name.Friends.name_("Ben").get === List("Ann")
```

```scala
Person.name_("Ben").Friends.name.get === List()
```

```scala
Person.name_("Ann").Friends.name.get === List("Ben")
```

```scala
Person.name("Ann").Friends.name("Ben").save

Person.name_("Ann").Friends.name.get === List("Ben")
Person.name_("Ben").Friends.name.get === List()

Person.name.Friends.name_("Ben").get === List("Ann")
```

```scala
Person.name("Ann").Friends.name("Ben").save

Person.name_("Ann").Friends.name.get === List("Ben")
Person.name_("Ben").Friends.name.get === List()
```

```scala
Person.name("Ann").Friends.name("Ben").save

Person.name_("Ann").Friends.name.get === List("Ben")
```

```scala
trait Person {
  val name    = oneString
  val friends = many[Person]
}
```

```scala
trait Person {
  // A ==> a
  val spouse  = oneBi[Person]
  val friends = manyBi[Person]

  // A ==> b
  val pet     = oneBi[Animal.master.type]
  val buddies = manyBi[Animal.buddies.type]

  // A ==> edge -- a
  val loves = oneBiEdge[Loves.person.type]
  val knows = manyBiEdge[Knows.person.type]

  // A ==> edge -- b
  val favorite = oneBiEdge[Favorite.animal.type]
  val closeTo  = manyBiEdge[CloseTo.animal.type]

  val name = oneString
}
```


```scala
// Mix input, static expressions and relationships...
val americansYoungerThan = m(Person.id.age_.<(?).Country.name_("USA"))
val americanKids         = americansYoungerThan(13).get
val americanBabies       = americansYoungerThan(1).get
```

```scala
// Multiple input parameters + logic
val person = m(Person.id.name_(?).age_(?))

// AND
val john42 = person("John" and 42).get.head

// AND/OR
val john42orJonas38  = person(("John" and 42) or ("Jonas" and 38)).get 
val john42orJonas38b = person(("John", 42), ("Jonas", 38)).get
```


```scala
// Mix input, static expressions and relationships...
val americansYoungerThan = m(Person.id.age_.<(?).Country.name_("USA"))
val americanKids         = americansYoungerThan(13).get
val americanBabies       = americansYoungerThan(1).get
```

```scala
// Multiple input parameters + logic
val person = m(Person.id.name_(?).age_(?))

// AND
val john42 = person("John" and 42).get.head
```

```scala
// 1 input parameter
val person = m(Person.id.name_(?))

val john     = person("John").get.head
val students = person(allStudentNames).get.head
```

```scala
val persons = datomic.Peer.q(
  """
    |[:find ?id
    | :in $ ?names
    | :where
    |   [?e :person/name ?names]]
  """.stripMargin, conn.db, "John")
```


```scala
fredId.retract

Person.name("Fred").get.size === 0 
```




```scala
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
trait Person {
  val id      = oneInt
  val nameMap = mapString
}
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
  ":person/age"	-> 38,
  ":site/cat" -> "customer"
)

val siteCat_? : Option[String] = fredId[String](":site/cat")
```

```scala
fredId.touch === Map(
  ":db/id" -> 101L,
  ":person/name" -> "Fred",
  ":person/likes" -> "pizza",
  ":person/age"	-> 38,
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
Person.name.Home.street.city.get.head === (
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
Person.age(min).get.head           === 5 
Person.age(max).get.head           === 38 
Person.age(sum).get.head           === 50 
Person.age(count).get.head         === 3 
Person.age(countDistinct).get.head === 3 
Person.age(avg).get.head           === 16.66666667
Person.age(median).get.head        === 7 
Person.age(variance).get.head      === 228.2222222222 
Person.age(stddev).get.head        === 15.107025591499
 
Person.age(distinct).get.head  === Vector(5, 7, 38) 
Person.age(min(2)).get.head    === Vector(5, 7) 
Person.age(max(2)).get.head    === Vector(38, 7) 
Person.age(rand(2)).get.head   === Stream(5, ?) 
Person.age(sample(2)).get.head === Vector(7, 38) 
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
Person.name.get: List[String] === List(
  "Fred", "Lisa", "Ben"
)
```
```scala
Person.name.age.get: List[(String, Int)] === List(
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
-------
