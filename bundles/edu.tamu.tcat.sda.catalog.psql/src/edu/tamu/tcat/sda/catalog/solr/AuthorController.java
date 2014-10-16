package edu.tamu.tcat.sda.catalog.solr;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;

import edu.tamu.tcat.sda.catalog.events.dv.HistoricalEventDV;
import edu.tamu.tcat.sda.catalog.people.dv.PersonDV;
import edu.tamu.tcat.sda.catalog.people.dv.PersonNameDV;

public class AuthorController
{
   Logger log = Logger.getLogger("edu.tamu.tcat.sda.catalog.solr.authorcontroller");
   private final SolrServer solr;

   private final static String personId = "id";
   private final static String familyName = "familyName";
   private final static String syntheticName = "syntheticName";
   private final static String displayName = "displayName";
   private final static String birthLocation = "bLocation";
   private final static String birthDate = "bDate";
   private final static String deathLocation = "dLocation";
   private final static String deathDate = "dDate";
   private final static String summary = "summary";

   private String collectionName = "authors";
   private String rootSolrEndpoint = "https://sda-dev.citd.tamu.edu/solr/";

   public AuthorController()
   {
      solr = new HttpSolrServer(rootSolrEndpoint + collectionName);
   }

   private String guardNull(String value)
   {
      return value == null ? "" : value;
   }

   private String convertDate(Date event)
   {
      // TODO we need some more tooling around dates.
      String dateRep = "";

      SimpleDateFormat calendar = new SimpleDateFormat("yyyy-MM-dd");
      SimpleDateFormat time = new SimpleDateFormat("HH:mm:SS");
      dateRep = calendar.format(event) + "T" + time.format(event) + "Z";

      return dateRep;
   }

//   <T> SolrDocumentAdapter<T> getAdapter(Class<T> type) throws UnsupportedTypeException
//   {
//      // lookup from some registry
//
//      return null;
//   }

   /**
    * Adapts the supplied data vehicle into a {@link SolrInputDocument}.
    *
    * @param person The data to bee added to the database.
    * @return The document to index.
    */
   private SolrInputDocument adapt(PersonDV person)
   {
      SolrInputDocument document = new SolrInputDocument();

      document.addField(personId, person.id);
      document.addField(syntheticName, constructSyntheticName(person.names));
      for(PersonNameDV name : person.names)
      {
         document.addField(familyName, guardNull(name.familyName));
         document.addField(displayName, guardNull(name.displayName));
      }

      HistoricalEventDV birth = person.birth;
      document.addField(birthLocation, birth.location == null ? "" : birth.location);
      Date bDate = birth.eventDate;
      if (bDate != null)
         document.addField(birthDate, convertDate(bDate));

      HistoricalEventDV death = person.birth;
      document.addField(deathLocation, death.location == null ? "" : death.location);
      if (death.eventDate != null)
         document.addField(deathDate, convertDate(death.eventDate));

      document.addField(summary, guardNull(person.summary));

      // TODO need default display name
      //      maybe first sentence of summary
      // 'defualtDisplayName' : Neal Audenaert (1978 - )
      // First sentence of summary
      return document;
   }

   /**
    * Constructs a synthetic name that contains the various values (title, first name,
    * family name, etc) from different names associated with this person. Each portion
    * of a person's name is collected into a set of 'name parts' that is then concatenated
    * to form a string-valued synthetic name. This allows all of the various name tokens to
    * be included in the search.
    *
    * @param names A set of names associated with a person.
    * @return A synthetic name that contains a union of the different name fields.
    */
   private String constructSyntheticName(Set<PersonNameDV> names)
   {
      Set<String> nameParts = new HashSet<>();
      for(PersonNameDV name : names)
      {
         nameParts.add(name.title);
         nameParts.add(name.givenName);
         nameParts.add(name.middleName);
         nameParts.add(name.familyName);
      }

      StringBuilder sb = new StringBuilder();
      for (String part : nameParts)
      {
         if (part == null)
            continue;

         sb.append(part).append(" ");
      }

      return sb.toString().trim();
   }

   public void addDocument(PersonDV author)
   {
      SolrInputDocument document = adapt(author);

      try
      {
         solr.add(document);
         solr.commit();
      }
      catch (IOException e)
      {
         log.severe("An error occured in the transmition of the document:" + e);
      }
      catch (SolrServerException e)
      {
         log.severe("An error occured with Solr Server:" + e);
      }
   }

   public void clean()
   {
      try
      {
         solr.deleteByQuery("*:*");
      }
      catch (IOException e)
      {
         log.severe("An error occured in the transmition of the document:" + e);
      }
      catch (SolrServerException e)
      {
         log.severe("An error occured with Solr Server:" + e);
      }
   }

   public void reindex()
   {

   }
}
