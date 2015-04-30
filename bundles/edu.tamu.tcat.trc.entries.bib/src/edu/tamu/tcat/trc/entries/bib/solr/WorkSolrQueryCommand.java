package edu.tamu.tcat.trc.entries.bib.solr;

import java.io.IOException;
import java.time.Year;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.SolrParams;

import edu.tamu.tcat.trc.entries.bib.WorkQueryCommand;
import edu.tamu.tcat.trc.entries.bib.dto.WorkInfo;

public class WorkSolrQueryCommand implements WorkQueryCommand
{
   private final static Logger logger = Logger.getLogger(WorkSolrQueryCommand.class.getName());

   // Solr field name values for works

   private Collection<String> criteria = new ArrayList<>();

   private SolrServer solr;

   private String q;
   private String titleQuery;
   private String[] authorIds;
   private String authorName;
   private Year after;
   private Year before;
   private int start = 0;
   private int maxResults = 25;

   private String location;

   public WorkSolrQueryCommand(SolrServer solr)
   {
      this.solr = solr;
   }

   @Override
   public List<WorkInfo> getResults()
   {
      List<WorkInfo> works = new ArrayList<>();
      String workInfo = null;
      WorkInfo wi = new WorkInfo();
      QueryResponse response;

      try
      {
         response = solr.query(getQuery());
         SolrDocumentList results = response.getResults();

         for (SolrDocument doc : results)
         {
            try
            {
               workInfo = doc.getFieldValue("workInfo").toString();
               wi = BiblioEntriesSearchService.mapper.readValue(workInfo, WorkInfo.class);
               works.add(wi);
            }
            catch (IOException ioe)
            {
               logger.log(Level.SEVERE, "Failed to parse relationship record: [" + workInfo + "]. " + ioe);
            }
         }
      }
      catch(SolrServerException e)
      {
         logger.log(Level.SEVERE, "The following error occurred while querying the works core :" + e);
      }

      return works;
   }

   private SolrParams getQuery()
   {
      SolrQuery query = new SolrQuery();

      query.setStart(Integer.valueOf(start));
      query.setRows(Integer.valueOf(this.maxResults));
      // NOTE this looks like a bad idea. probably set internal state and build based on that state
//      String queryString = Joiner.on(" AND ").join(criteria);
//      query.setQuery(queryString);


      return query;
   }

   @Override
   public WorkQueryCommand setQuery(String q)
   {
      this.q = q;
      // NOTE query against all fields, boosted appropriately, free text
      //      I think that means *:(q)
      // NOTE in general, if this is applied, the other query params are unlikely to be applied
      return null;
   }

   @Override
   public WorkQueryCommand setTitleQuery(String q)
   {
      this.titleQuery = q;
      return null;
   }

   @Override
   public WorkQueryCommand setAuthorName(String authorName)
   {
      this.authorName = authorName;
//      criteria.add("authorNames\"" + authorName + "\"");
      return this;
   }

   @Override
   public WorkQueryCommand filterByAuthor(String... ids)
   {
      // NOTE these should be joined by OR's
      this.authorIds = ids;
      return null;
   }

   @SuppressWarnings("hiding")
   @Override
   public WorkQueryCommand filterByDate(Year after, Year before)
   {
      this.after = after;
      this.before = before;

      return null;
   }

   @Override
   public WorkQueryCommand setStartIndex(int start)
   {
      this.start = start;
      return null;
   }

   @Override
   public WorkQueryCommand filterByLocation(String location)
   {
      this.location = location;
      return this;
   }

   @Override
   public WorkQueryCommand setMaxResults(int max)
   {
      this.maxResults = max;
      return this;
   }
}
