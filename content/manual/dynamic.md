---
title: "Dynamic molecules"
weight: 110
menu:
  main:
    parent: manual
---

# Dynamic molecules

Molecules can have a Scala code body applied that defines functionality that is relevant to the requested data. A self reference to the data object gives access to the fetched data. Any data in scope (even from outside this code body) can be accessed too. Adding functionality this way to the data gives a great flexibility to add "dynamic" (still type safe) contextualized functionality. 

In the following example we access a Person object through the self reference `person` to build some logic that is relevant for this particular set of data:

```scala
for {
  // Initial data
  _ <- Person.name("Ben").age(23).save

  // Fetch data with with molecule having a dynamic method
  person <- m(Person.name.age) { person =>
    def nextAge: Int = person.age + 1 // Using person data property
  }.map(_.head)

  _ = {
    person.name ==> "Ben"
    person.age ==> 23
    person.nextAge ==> 24 // Calling dynamic method on molecule object
  }
} yield ()
```

In a traditional setup one would likely define and populate a domain class with fetched data and then call a method on the domain object:

```scala
// Domain class
case class PersonClass(name: String, age: String) {
  // Having centralized (rigid) functionality in domain class
  def nextAge: Int = age + 1
}

for {
  // Initial data
  _ <- PersonClass.name("Ben").age(23).save
  
  // Fetching data with molecule (without dynamic method)
  (name, age) <- Person.name.age.get.map(_.head)
  
  // Instantiating domain class with fetched data
  person = PersonClass(name, age)

  _ = {
    person.name ==> "Ben"
    person.age ==> 23
    person.nextAge ==> 24 // Calling method on domain object
  }
} yield ()
```

Domain classes with methods that are only used in one place are good candidates for creating a dynamic molecule having the same functionality defined instead. With a dynamic molecule you simply retrieve exactly the data that you need and _add exactly the functionality that you need!_