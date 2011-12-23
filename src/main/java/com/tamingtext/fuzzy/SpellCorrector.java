package com.tamingtext.fuzzy;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import org.apache.lucene.search.spell.StringDistance;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

//<start id="did-you-mean.corrector"/>
public class SpellCorrector {

  private SolrServer solr;
  private SolrQuery query;
  private StringDistance sd;
  private float threshold;
  
  public SpellCorrector(StringDistance sd, float threshold) 
    throws MalformedURLException { 
    solr = new CommonsHttpSolrServer(new URL("http://localhost:8983/solr"));
    query = new SolrQuery();
    query.setFields("word");
    query.setRows(50); //<co id="co.dym.num"/>
    this.sd = sd;
    this.threshold = threshold;
  }
  
  public String topSuggestion(String spelling) throws SolrServerException {
    query.setQuery("wordNGram:"+spelling); //<co id="co.dym.field"/>
    QueryResponse response = solr.query(query);
    SolrDocumentList dl = response.getResults();
    Iterator<SolrDocument> di = dl.iterator();
    float maxDistance = 0;
    String suggestion = null;
    while (di.hasNext()) {
      SolrDocument doc = di.next();
      String word = (String) doc.getFieldValue("word");
      float distance = sd.getDistance(word, spelling); //<co id="co.dym.edit"/>
      if (distance > maxDistance) {
        maxDistance = distance;
        suggestion = word; //<co id="co.dym.max"/>
      }
    }
    if (maxDistance > threshold) { //<co id="co.dym.threshold"/>
      return suggestion;
    }
    return null;
  }  
}
/*
<calloutlist>
<callout arearefs="co.dym.num"><para>The number of n-gram matches to consider.</para></callout>
<callout arearefs="co.dym.field"><para>Query the field which contains the n-gram.</para></callout>
<callout arearefs="co.dym.edit"><para>Compute the edit distance.</para></callout>
<callout arearefs="co.dym.max"><para>Keep best suggestion.</para></callout>
<callout arearefs="co.dym.threshold"><para>Check threshold otherwise return no suggestion.</para></callout>
</calloutlist>
 */
//<end id="did-you-mean.corrector"/>
