package edu.tamu.tcat.sda.catalog.relationships.search.solr;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import edu.tamu.tcat.sda.catalog.relationship.Relationship;
import edu.tamu.tcat.sda.catalog.relationship.RelationshipException;
import edu.tamu.tcat.sda.catalog.relationship.RelationshipTypeRegistry;
import edu.tamu.tcat.sda.catalog.relationship.model.RelationshipDV;

public class SolrRelationshipQuery
{

   private final static Logger logger = Logger.getLogger(SolrRelationshipQuery.class.getName());

   public SolrQuery query;
   private Collection<Relationship> relationships;

   public SolrRelationshipQuery()
   {

   }

   public static SolrRelationshipQuery query(URI entry)
   {
      SolrRelationshipQuery solrRelnQuery = new SolrRelationshipQuery();
      solrRelnQuery.buildQuery(entry);
      return solrRelnQuery;
   }

   public Collection<Relationship> getResults(QueryResponse response, RelationshipTypeRegistry typeReg)
   {
      relationships = new HashSet<>();
      String relationshipJson = null;
      RelationshipDV dv = new RelationshipDV();

      SolrDocumentList results = response.getResults();
      for (SolrDocument result : results)
      {
         try
         {
            String relationship = result.getFieldValue("relationshipModel").toString();
            dv = SolrRelationshipSearchService.mapper.readValue(relationship, RelationshipDV.class);
            relationships.add(RelationshipDV.instantiate(dv, typeReg));
         }
         catch (IOException e)
         {
            logger.log(Level.SEVERE, "Failed to parse relationship record: [" + relationshipJson + "]. " + e);
         }
         catch (RelationshipException e)
         {
            logger.log(Level.SEVERE, "Error occurred while instantiating the relationship: [" + dv.id + "]. " + e);
         }

      }

      return relationships;
   }

   void buildQuery(URI entry)
   {
      // HACK: Currently returning all items until we create a query builder.
      query = new SolrQuery();
      query.setQuery("*:*");
   }

}
