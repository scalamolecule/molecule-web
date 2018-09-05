---
date: 2014-05-14T02:13:50Z
title: "Boilerplate"
weight: 20
menu:
  main:
    parent: dev
    identifier: dev-boilerplate
---

# Boilerplate code generation

An attribute definition like
```scala
@InOut(3, 8)
trait SeattleDefinition {
  trait Community {
    val name = oneString.fulltextSearch
  }
}
```
would generate the following boilerplate code

```scala
object Community extends Community_0 {
  def apply(e: Long): Community_0 = ???
}

trait Community {
  class name [Ns, In] extends OneString [Ns, In] with FulltextSearch[Ns, In] with Indexed
}

trait Community_0 extends Community with Out_0[Community_0, Community_1, Community_In_1_0, Community_In_1_1] {
  lazy val name : name[Community_1[String], Community_In_1_1[String, String]] with Community_1[String] = 
    new name[Community_1[String], Community_In_1_1[String, String]] with Community_1[String] { 
      override val _kw = ":community/name" 
    }
  lazy val name_ : name[Community_0, Community_In_1_0[String]] with Community_0 = ???
}
         
trait Community_1[A] extends Community with Out_1[Community_1, Community_2, Community_In_1_1, Community_In_1_2, A] {
  lazy val name  : name[Community_2[A, String], Community_In_1_2[String, A, String]] with Community_2[A, String] = ???
  lazy val name_ : name[Community_1[A], Community_In_1_1[String, A]] with Community_1[A] = ???
}
         
trait Community_2[A, B] extends Community with Out_2[Community_2, Community_3, Community_In_1_2, Community_In_1_3, A, B] {
  // etc up to arity 8...

```

The type information passed on from each step of building a molecule enables our IDE to infer the type of our molecule 
and suggest possible further building blocks.

### Input molecules

The `Community_In_1_0` is a trait that the builder jumps to if we make en input molecule like `Community.name(?)`. 
From there on Molecule knows that we expect to receive an input for the placeholder `?`.

### Arity
The annotation `@InOut(3,8)` on the schema trait tells molecule how long molecules we expect to create. This is to 
not always generate the possible arity of 22 which causes a significant chunk of boilerplate code!

We also have to remember that this is not a limitation of _how many attribute values_ we can find but rather a 
limitation of how many _where clauses_ we can have since each added attribute to our molecules generates it's 
corresponding where clause. In that respect it seems a very seldom necessity to have 22 where clauses! 

### Entity API

A way to gather additional/optional values from a query is to use the entity API by applying an attribute name 
to an entity id from the query:

```scala
communityId(":community/url") === Some("http://eastballard.com/")
```
This gives us an optional value back if an attribute value has been asserted for this entity.

[See entity api example](https://github.com/scalamolecule/molecule/blob/master/examples/src/test/scala/molecule/examples/seattle/SeattleTests.scala#L24-L52)


### Tacit attributes

The `name_` attributes are "tacit" attributes that cause a :where clause in the query but doesn't return any data 
(no variable in the :find section of the produced query). This is to be able to skip returning data of attributes 
that we for instance apply a constant value to

```scala
Person.name.age_(28).get // no need to return age values (all 28)
```

### Type inferring
The end user doesn't have to know about any of the generated boilerplate code since the IDE will take care of 
inferring and suggesting the possible types to proceed with.