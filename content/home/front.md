---
date: 2015-01-02T22:06:44+01:00
title: "Home"
weight: 10
menu:
  main:
    parent: home
---

<br>
![](/img/logo/MoleculeLogo697.png)



#### _Write powerful [Datomic] queries in Scala with the words of your domain:_



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
<pre><code class="language-scala">val persons: Iterable[(String, Int)] = Person.name.age.get      
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
            	<h3 id="more-up-the-sleeve:ffcff61ab3a11ef1d50900901a24ec54">More..</h3>
<ul>
<li><a href="/home/introduction">Introduction</a></li>
<li><a href="/tutorials/seattle">Seattle Tutorial</a></li>
<li><a href="/home/setup">Setup</a></li>
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


Molecule is a Scala meta-DSL that translates "molecules" of your domain attributes to typed Datalog queries 
for [Datomic](http://www.datomic.com) - the database of immutable facts. 


### Videos

Marc Grue presents his Molecule library in 4 parts:


<div class="media">
  <div class="media-left">
    <a href="/learn/videos/2017-04-25_Marc_Grue">
      <img class="media-object" src="/img/presentation.jpg" alt="...">
    </a>
  </div>
  <div class="media-body">
      <ol>
          <li>
            <a href="/learn/videos/2017-04-25_Marc_Grue#1:e8a80e11a9ea81b9e6071cec3c4864a9">Datomic data model</a>
            <p>Introduction to the data model of Datomic that Molecule is based on.</p>
          </li>
          <li>
            <a href="/learn/videos/2017-04-25_Marc_Grue#2:e8a80e11a9ea81b9e6071cec3c4864a9">Building a DSL with Scala macros</a>
            <p>A look behind the scenes of how Molecule is build.</p>
          </li>
          <li>
            <a href="/learn/videos/2017-04-25_Marc_Grue#3:e8a80e11a9ea81b9e6071cec3c4864a9">Molecule tour</a>
            <p>A walk through of Molecule features.</p>
          </li>
          <li>
            <a href="/learn/videos/2017-04-25_Marc_Grue#4:e8a80e11a9ea81b9e6071cec3c4864a9">Molecule domain modelling</a>
            <p>Using Molecule in your domain modelling.</p>
            <br>
            <a href="/learn/videos/2017-04-25_Marc_Grue">More info</a>
          </li>
      </ol>
  </div>
</div>


<br>


### How does it work?

<div class="sequence-block">
	<div class="bullet-block">
		<div class="sequence-step">1</div>
	</div>
	<div class="section">
		<h4 id="define-schema-once:ffcff61ab3a11ef1d50900901a24ec54">Define schema (once)</h4>
        <p>Our domain could have a <code>Person</code> with attributes 
        <code>name</code> and <code>age</code> having a relationship to an 
        <code>Address</code> so we define a schema for our domain:</p>

<pre><code>trait Person {
  val name    = oneString
  val age     = oneInt
  val address = one[Address]
}
trait Address {
  val street = oneString
}
</code></pre>

	</div>
</div>

<div class="sequence-block">
    <div class="bullet-block">
        <div class="sequence-step">2</div>
    </div>
    <div class="section">
        <h4 id="compile-once:ffcff61ab3a11ef1d50900901a24ec54">Compile (once)</h4>

<pre><code>> cd yourProjectRoot
> sbt compile
</code></pre>

        <p>Molecule uses our schema as a template to 
        generate some boilerplate code so that we can compose intuitive and powerful 
        query molecules. This step is only needed when we create or change our schema.</p>
    </div>
</div>

<div class="sequence-block">
    <div class="bullet-block">
        <div class="sequence-step">3</div>
    </div>
    <div class="section">
        <h4 id="use-molecules:ffcff61ab3a11ef1d50900901a24ec54">Use molecules</h4>
        <p>Now we can insert data with molecules:</p>

<pre><code class="language-scala">Person.name.age.Address.street insert List(
  ("Lisa", 20, "Broadway"),
  ("John", 22, "Fifth Avenue")
)
</code></pre>

        <p>And retrieve data:</p>
        
<pre><code class="language-scala">Person.name.age.Address.street.get === List(
  ("Lisa", 20, "Broadway"),
  ("John", 22, "Fifth Avenue")
)     
</code></pre>

        <p>The implicit macros <code>insert</code> and <code>get</code> turns our molecules
         into type-safe Datalog inserts and queries at compile time. So there's no runtime overhead.</p> 
        
        
        
    </div>
</div>

### Try demo

1. `git clone https://github.com/scalamolecule/molecule-demo.git`
2. `cd molecule-demo`
3. `sbt compile`
4. Open in your IDE
5. Run app - and build new molecules...


### Next

[Get started...](/manual/getting-started)
   
### Read more

- [Introduction](/home/introduction) to Datomic/Molecule
- [Setup Database](/manual/setup): initiate a Datomic database and create a database schema with Molecule
- [Populate Database](/manual/insert): populate a Datomic database with Molecule
- [Molecule Seattle tutorial](/tutorials/seattle) examples of using Molecule (based on the 
[Datomic Seattle tutorial](http://docs.datomic.com/tutorial.html))


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
