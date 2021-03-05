---
title: "Dynamic molecules"
weight: 110
menu:
  main:
    parent: code
---

# Dynamic molecules

For molecules expecting to return a single row of data, we can add a code body that will have access to the data of that row through a self reference. 

In the following case we access a Person object through the self reference `p` to build some logic that is relevant for this particular set of data:

```scala
// Retrieve typed and named data and add functionality directly
val person = m(Person.name("Ben").age.gender.Address.street,City.name.getObj) { p =>
  def info = s"${p.name} (${p.age}, ${p.gender}) lives on ${p.Address.street}, ${p.Address.City.name}"
}

// Access object properties
person.age === 23
person.gender === "male"
person.Address.street === "Broadway"
person.Address.City.street === "New York"
// Call body method on object
person.info === "Ben (23, male) lives on Broadway, New York"
```

Normally one would populate already defined domain classes with data from a database which more tediously involves 4 steps:

```scala
// 1. Define domain model classes
case class City(name: String)
case class Address(street: String, city: City)
case class Person(name: String, age: String, gender: String, address: Address) {
  // Having centralized (rigid) functionality in domain class
  def info = s"$name ($age, $gender) lives on ${address.street}, ${address.City.name}"
}

// 2. Retrieve data from db
val (name, age, gender, street, city) = Person.name.age.gender.Address.street.City.name.get.head

// 3. Instantiate and populate domain model
val person = Person(name, age, gender Address(street), City(name))

// 4. Access populated domain model
person.age === 23
person.gender === "male"
person.Address.street === "Broadway"
person.Address.City.street === "New York"
person.info === "Ben (23, male) lives on Broadway, New York"
```
Pushing data into domain classes to immediately after pulling it out again is kind of a waste of effort, specially if the domain classes have no distinctive domain logic but are basically just CRUD containers with nice names.

Furthermore each domain class will eventually grow and grow to serve all local requirements and variations, and you would have to be more and more careful to change functionality since it could break local uses.

With a dynamic molecule you instead simply retrieve exactly the data that you need add exactly the functionality that you need, no more or less for a specific context.