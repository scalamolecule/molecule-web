---
title: "Home"
---

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
<pre><code class="language-scala">val persons: List[(String, Int)] = Person.name.age.get      
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

<p>Save data for multiple namespace levels in one go:</p>

<pre><code class="language-scala">Person.name(&quot;Johnson&quot;).age(35).Address.street(&quot;5th&quot;).City.name(&quot;New York&quot;).save
</code></pre>

<p><em>&ldquo;Add a 35-year-old Johnson living on 5th street in New York&rdquo;</em></p>
            </div>
        </div>
        <div class="item">
            <div class="carousel-caption">                   
            	<h3 id="upsert-data:ffcff61ab3a11ef1d50900901a24ec54">Update data</h3>

<p>Update molecules with new data</p>

<pre><code class="language-scala">Person(johnsonId).Address.street(&quot;Broadway&quot;).update
</code></pre>

<p><em>&ldquo;Johnson now lives on Broadway&rdquo;</em></p>
            </div>
        </div>
        <div class="item">
            <div class="carousel-caption">                   
            	<h3 id="more-up-the-sleeve:ffcff61ab3a11ef1d50900901a24ec54">More..</h3>
<ul>
    <li><a href="/manual/quick-start/introduction/">Introduction</a></li>
    <li><a href="/manual/setup">Setup</a></li>
    <li><a href="/resources/tutorials/seattle">Seattle Tutorial</a></li>
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


### Molecule videos

Marc Grue presents his Molecule library in 4 parts:


<div class="media">
  <div class="media-left">
    <a href="/resources/videos/2017-04-25_marc_grue">
      <img class="media-object" src="/img/presentation.jpg" alt="...">
    </a>
  </div>
  <div class="media-body">
      <ol>
          <li>
            <a href="/resources/videos/2017-04-25_marc_grue/#1">Datomic data model</a>
            <p>Introduction to the data model of Datomic that Molecule is based on.</p>
          </li>
          <li>
            <a href="/resources/videos/2017-04-25_marc_grue/#2">Building a DSL with Scala macros</a>
            <p>A look behind the scenes of how Molecule is build.</p>
          </li>
          <li>
            <a href="/resources/videos/2017-04-25_marc_grue/#3">Molecule tour</a>
            <p>A walk through of Molecule features.</p>
          </li>
          <li>
            <a href="/resources/videos/2017-04-25_marc_grue/#4">Molecule domain modelling</a>
            <p>Using Molecule in your domain modelling.</p>
            <br>
            <a href="/resources/videos/2017-04-25_marc_grue">More info</a>
          </li>
      </ol>
  </div>
</div>


<br>


### How does Molecule work?


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


### Read more

- [Introduction](/docs/documentation/introduction/) to Datomic/Molecule
- [Getting started](/docs/getting-started/) with Molecule
- [Setup](/docs/getting-started/setup/) your Molecule project


[Datomic]: http://www.datomic.com