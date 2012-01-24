<@ie.page "Introduction">
<style type="text/css">
  .service { border: 1px solid #808080; padding: 0.5em }
  .disabled { color: #808080; }
</style>
<div class="yui3-g">
  <div class="yui3-u-1-2">
    <div style="margin: 1em">
      <h1>What is Interedition?</h1>

      <p><a href="http://www.interedition.eu/" title="Interedition Homepage">Interedition</a> is a
        <a href="http://www.cost.esf.org/" title="ESF COST Homepage">European-funded</a> development
        <a href="https://github.com/interedition/" title="GitHub Homepage">collective</a> whose aim it is to promote the
        interoperability of the tools and methodology we use in the field of digital scholarly editing and research.</p>

      <h1>What are Microservices?</h1>

      <p>Microservices are small
        <a href="http://en.wikipedia.org/wiki/Representational_state_transfer" title="Wikipedia: REST">RESTful web services</a>
        supporting specific tasks in any digital work flow pertaining to larger scholarly tasks. They are the small
        reusable web published building blocks of digital scholarly tools. They provide the basic solution to
        interoperability and sustainability for digital scholarly tools Interedition is striving for.</p>
    </div>
  </div>
  <div class="yui3-u-1-2">
    <h2 class="boxed">Microservice: CollateX</h2>

    <p>CollateX is a service for collating textual sources, for example, to produce a critical apparatus. It is the
      designated successor of Peter Robinson's
      <a href="http://www.sd-editions.com/" title="Peter Robinson: Scholarly Digital Editions">Collate</a>.</p>
    
    <p>To express textual variance, CollateX uses a graph-based data model which has been thought up by
      Desmond Schmidt and Robert Colomb
      (<a href="http://dx.doi.org/10.1016/j.ijhcs.2009.02.001" title="Schmidt, D. and Colomb, R, 2009. A data structure for representing multi-version texts online, International Journal of Human-Computer Studies, 67.6, 497-514.">Schmidt 2009</a>).
      On top of this model it supports several algorithms to progressively align multiple text versions.</p>
    
    <p>You can use CollateX either online via the <a href="${cp}/collate/console" title="CollateX Console">web console</a> 
      or – if you are a developer – via its <a href="${cp}/collate/apidocs" title="CollateX API Documentation">API</a>.</p>

    <h2 class="boxed color12">Microservice: Text Repository (forthcoming…)</h2>

    <p class="color12">Interedition's Text Repository Service will allow to store, transform and annotate texts online. Its text model
      supports range-based markup via standoff annotations, thus facilitating collaborative workflows and integration
      of different services operating on larger text corpora.</p>
    
    <p class="color12">While the model is already implemented and in use by other software, the service's user interface
      and the RESTful  API, exposing its functionality to textual scholars and software developers alike, are still work
      in progress. The Text Repository Service will supplement CollateX in a forthcoming release.</p>
  </div>
</div>

</@ie.page>