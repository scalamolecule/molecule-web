---
date: 2015-01-02T22:06:44+01:00
title: "Home"
weight: 10
url: "/"
menu:
  main:
    parent: home
---
<br>


# Molecule

#### **_Write powerful [Datomic] queries with the words of your domain:_**

<div id="myCarousel" class="carousel slide" data-interval="0" data-ride="carousel">
	<!-- Carousel indicators -->
    <ol class="carousel-indicators">
        <li data-target="#myCarousel" data-slide-to="0" class="active"></li>
        <li data-target="#myCarousel" data-slide-to="1"></li>
        <li data-target="#myCarousel" data-slide-to="2"></li>
    </ol>   
   <!-- Carousel items -->
    <div class="carousel-inner">
        <div class="active item">
            <div class="carousel-caption">                      
            
              <h3>Relationships</h3>
              <p>Find Smith's in New York:</p>
              <pre><code class="language-scala hljs"><span class="hljs-type">Person</span>.name(<span class="hljs-string">"Smith"</span>).<span class="hljs-type">Address</span>.<span class="hljs-type">City</span>.name(<span class="hljs-string">"New York"</span>)
</code></pre>
Note how bla bla
              
            </div>
        </div>
        <div class="item">
            <div class="carousel-caption">                   
            
              <h3>Graph traversals</h3>
              <p>Find Collaborators of Collaborators of John Lennon:</p>
              <pre><code class="language-scala hljs"><span class="hljs-type">Artist</span>.name(<span class="hljs-string">"John Lennon"</span>).name.get
</code></pre>
Note how bla bla
              
            </div>
        </div>
        <div class="item">
            <div class="carousel-caption">                   
            
              <h3>Logic operations</h3>
              <p>Apply conditional logic to attribute values</p>
              <pre><code class="language-scala hljs"><span class="hljs-type">Artist</span>.name(<span class="hljs-string">"Lisa"</span> or <span class="hljs-string">"Linda"</span>).age.&lt;(<span class="hljs-number">18</span>).get
</code></pre>
Note how bla bla
              
            </div>
        </div>
    </div>
    <!-- Carousel nav -->
    <a class="carousel-control left" href="#myCarousel" data-slide="prev">
        <span class="glyphicon glyphicon-chevron-left"></span>
    </a>
    <a class="carousel-control right" href="#myCarousel" data-slide="next">
        <span class="glyphicon glyphicon-chevron-right"></span>
    </a>
</div>

Molecule is a Scala meta-DSL that translates your "domain molecules" of attributes to queries for [Datomic](http://www.datomic.com) - the database of immutable facts. 

#### How does it work?

Our domain could have a `Person` with attributes `name` and `age` having a relationship to an `Address`:

```
trait Person {
  val name    = oneString
  val age     = oneInt
  val address = oneRef[Address]
} 

trait Address {
  val street = oneString
}
```
From this simple schema definition, Molecule generates the necessary code to compose intuitive and powerful queries - _your domain language becomes the query language:_

```scala
Person.name.age.Address.street.get
Person.name("Lisa").age.get
Person.name("Lisa").age.<(18).get
Person.name("Lisa" or "Linda").age.Address.street.contains("5th").get
// etc..        
```

Since we use type-interferred code to query for molecular combinations of atomic attributes we are guaranteed to:

- Only make valid queries (won't compile otherwise)
- Only get query results that satisfy our queries (no null checks etc)
- Always get type-interferred query results

   
### Get started

- [Introduction](/molecule/home/introduction) to Datomic/Molecule
- [Setup Database](/molecule/manual/database-setup): initiate a Datomic database and create a database schema with Molecule
- [Populate Database](/molecule/manual/populate-database): populate a Datomic database with Molecule
- [Molecule Seattle tutorial](/molecule/tutorials/seattle) examples of using Molecule (based on the 
[Datomic Seattle tutorial](http://docs.datomic.com/tutorial.html))

### Download code

1. `git clone https://github.com/marcgrue/molecule.git`
2. `sbt compile`
3. Open in your IDE
4. Run tests and poke around...

### Try demo


[datomic]: http://www.datomic.com
[seattle]: http://docs.datomic.com/tutorial.html
[moleculegroup]: https://groups.google.com/forum/#!forum/molecule-dsl
[pullrequests]: https://github.com/marcgrue/molecule/pulls
[issues]: https://github.com/marcgrue/molecule/issues
[moleculesbt]: https://github.com/marcgrue/molecule/blob/master/project/build.scala

[intro]: https://github.com/marcgrue/molecule/wiki/Quick-introduction-to-Datomic-and-Molecule
[setup]: https://github.com/marcgrue/molecule/wiki/Setup-a-Datomic-database
[scheme]: https://github.com/marcgrue/molecule/wiki/Setup-a-Datomic-database#defining-a-schema
[deffile]: https://github.com/marcgrue/molecule/blob/master/examples/src/main/scala/molecule/examples/seattle/schema/SeattleDefinition.scala
[populate]: https://github.com/marcgrue/molecule/wiki/Populate-the-database
[tutorial]: https://github.com/marcgrue/molecule/wiki/Molecule-Seattle-tutorial
[tutorialcode]: https://github.com/marcgrue/molecule/blob/master/examples/src/test/scala/molecule/examples/seattle/SeattleTests.scala
[tutorialqueries]: https://github.com/marcgrue/molecule/blob/master/examples/src/test/scala/molecule/examples/seattle/SeattleQueryTests.scala
[tutorialtransformations]: https://github.com/marcgrue/molecule/blob/master/examples/src/test/scala/molecule/examples/seattle/SeattleTransformationTests.scala
