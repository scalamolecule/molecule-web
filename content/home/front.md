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



<div id="myCarousel" class="carousel slide" data-interval="9000" data-ride="carousel">
	<!-- Carousel indicators -->
    <ol class="carousel-indicators">
        <li data-target="#myCarousel" data-slide-to="0" class="active"></li>
        <li data-target="#myCarousel" data-slide-to="1"></li>
        <li data-target="#myCarousel" data-slide-to="2"></li>
        <li data-target="#myCarousel" data-slide-to="3"></li>
        <li data-target="#myCarousel" data-slide-to="4"></li>
        <li data-target="#myCarousel" data-slide-to="5"></li>
        <li data-target="#myCarousel" data-slide-to="6"></li>
        <li data-target="#myCarousel" data-slide-to="7"></li>
    </ol>   
   <!-- Carousel items -->
    <div class="carousel-inner">
        <div class="active item">
            <div class="carousel-caption">
              <h3>Type-inferred Queries</h3>
<p>Build query molecules with your domain attributes</p>
<pre><code class="language-scala">val persons: Seq[(String, Int)] = Person.name.age.get      
</code></pre>
<p><em>&ldquo;Name and age of Persons&rdquo;</em></p>
            </div>
        </div>   
        <div class="item">
            <div class="carousel-caption">                      
            	<h3>Relationships</h3>
<p>Pick attributes across namespaces</p>

<pre><code class="language-scala">Person.name.age.Address.street.Country.name.get      
</code></pre>

<p><em>&ldquo;Name, age, street and country of residence&rdquo;</em></p>
            </div>
        </div>
        <div class="item">
            <div class="carousel-caption">                   
            	<h3 id="conditional-values:ffcff61ab3a11ef1d50900901a24ec54">Conditional values</h3>

<p>Apply required values for certain attributes</p>

<pre><code class="language-scala">Person.name(&quot;Johnson&quot;).age.Address.City.name(&quot;New York&quot;).get      
</code></pre>

<p><em>&ldquo;Age of Johnsons in New York&rdquo; - name, age and city returned</em></p>
            </div>
        </div>
        <div class="item">
            <div class="carousel-caption">                   
            	<h3 id="control-output:ffcff61ab3a11ef1d50900901a24ec54">Control output</h3>

<p>Add an underscore to omit an attribute from the result set:</p>

<pre><code class="language-scala">Person.name_(&quot;Johnson&quot;).age.Address.City.name_(&quot;New York&quot;).get      
</code></pre>

<p><em>&ldquo;Age of Johnsons in New York&rdquo; - only ages returned</em></p>
            </div>
        </div>
        <div class="item">
            <div class="carousel-caption">                   
            	<h3 id="logic-and-ranges:ffcff61ab3a11ef1d50900901a24ec54">Logic and ranges</h3>

<p>Apply logical options and ranges</p>

<pre><code class="language-scala">Person.name(&quot;Dean&quot; or &quot;Johnson&quot;).age.&lt;(25).Address.Country.iso2(&quot;US&quot;).get      
</code></pre>

<p><em>&ldquo;Young Dean and Johnsons in the US&rdquo;</em></p>
            </div>
        </div>
        <div class="item">
            <div class="carousel-caption">                   
            	<h3 id="insert-multi-level-data:ffcff61ab3a11ef1d50900901a24ec54">Insert multi-level data</h3>

<p>Insert data for multiple namespace levels in one go:</p>

<pre><code class="language-scala">Person.name(&quot;Johnson&quot;).age(35).Address.street(&quot;5th&quot;).City.name(&quot;New York&quot;).add
</code></pre>

<p><em>&ldquo;Add a 35-year-old Johnson living on 5th street in New York&rdquo;</em></p>
            </div>
        </div>
        <div class="item">
            <div class="carousel-caption">                   
            	<h3 id="upsert-data:ffcff61ab3a11ef1d50900901a24ec54">Upsert data</h3>

<p>Add new facts</p>

<pre><code class="language-scala">Person(johnsonId).Address.street(&quot;Broadway&quot;).update
</code></pre>

<p><em>&ldquo;Johnson now lives on Broadway&rdquo;</em></p>
            </div>
        </div>
        <div class="item">
            <div class="carousel-caption">                   
            	<h3 id="more-up-the-sleeve:ffcff61ab3a11ef1d50900901a24ec54">More up the sleeve..</h3>
<ul>
<li><a href="/manual/aggregates">Aggregates</a></li>
<li><a href="/manual/graphs">Graphs</a></li>
<li>etc&hellip;</li>
</ul>
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


Molecule is a Scala meta-DSL that translates "molecules" of your domain attributes to queries for [Datomic](http://www.datomic.com) - the database of immutable facts. 

Given a simple schema of your domain namespaces and their attributes, Molecule generates boilerplate code so that you can compose query molecules in endless combinations that suits your domain. Accessing Datomic probably couldn't be easier.


### How does it work?


1. #### Define schema (once)
   Our domain could have a `Person` with attributes `name` and `age` having a relationship to an `Address` so we define a schema for our domain:
    
    ```
    trait Person {
      val name    = oneString
      val age     = oneInt
      val address = one[Address]
    }
    trait Address {
      val street = oneString
    }
    ```
We run `sbt compile` and Molecule generates the necessary boilerplate code to compose intuitive and powerful queries. This step is only done once in the beginning (or when you need to change the schema). 

2. #### Make molecules
    Now we can make Molecule queries _with the words of our domain_ as we saw above:

    ```scala
    Person.name.age.Address.street.get
    Person.name("Lisa").age.get
    Person.name("Lisa").age.<(18).get
    Person.name("Lisa" or "Linda").age.Address.street.contains("5th").get
    // etc..        
    ```
The implicit macro `get` turns our molecule into a valid Datalog query at compile time. That means we can even infer the return type:

    ```
    val persons: Seq[(String, Int, String)] = Person.name.age.Address.street.get
    ```
 
3. #### Run queries
   The generated Datalog queries are executed against Datomic and the results returned as either tuples or Shapeless HLists as you like.

### Guarantees

- Valid queries (won't compile otherwise)
- Expected result sets without null values (we ask for existing attributes)
- Type-interferred results

   
### Get started

- [Introduction](/home/introduction) to Datomic/Molecule
- [Setup Database](/manual/database-setup): initiate a Datomic database and create a database schema with Molecule
- [Populate Database](/manual/populate-database): populate a Datomic database with Molecule
- [Molecule Seattle tutorial](/tutorials/seattle) examples of using Molecule (based on the 
[Datomic Seattle tutorial](http://docs.datomic.com/tutorial.html))

### Download code

1. `git clone https://github.com/scalamolecule/molecule.git`
2. `sbt compile`
3. Open in your IDE
4. Run tests and poke around...

### Try demo


[datomic]: http://www.datomic.com
[seattle]: http://docs.datomic.com/tutorial.html
[moleculegroup]: https://groups.google.com/forum/#!forum/molecule-dsl
[pullrequests]: https://github.com/scalamolecule/pulls
[issues]: https://github.com/scalamolecule/issues
[moleculesbt]: https://github.com/scalamolecule/blob/master/project/build.scala

[intro]: https://github.com/scalamolecule/wiki/Quick-introduction-to-Datomic-and-Molecule
[setup]: https://github.com/scalamolecule/wiki/Setup-a-Datomic-database
[scheme]: https://github.com/scalamolecule/wiki/Setup-a-Datomic-database#defining-a-schema
[deffile]: https://github.com/scalamolecule/blob/master/examples/src/main/scala/examples/seattle/schema/SeattleDefinition.scala
[populate]: https://github.com/scalamolecule/wiki/Populate-the-database
[tutorial]: https://github.com/scalamolecule/wiki/Molecule-Seattle-tutorial
[tutorialcode]: https://github.com/scalamolecule/blob/master/examples/src/test/scala/examples/seattle/SeattleTests.scala
[tutorialqueries]: https://github.com/scalamolecule/blob/master/examples/src/test/scala/examples/seattle/SeattleQueryTests.scala
[tutorialtransformations]: https://github.com/scalamolecule/blob/master/examples/src/test/scala/examples/seattle/SeattleTransformationTests.scala
