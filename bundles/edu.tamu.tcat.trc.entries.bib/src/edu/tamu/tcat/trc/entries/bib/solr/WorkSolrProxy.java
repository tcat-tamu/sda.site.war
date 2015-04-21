package edu.tamu.tcat.trc.entries.bib.solr;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.solr.common.SolrInputDocument;

import edu.tamu.tcat.catalogentries.events.dv.DateDescriptionDV;
import edu.tamu.tcat.trc.entries.bib.Edition;
import edu.tamu.tcat.trc.entries.bib.Volume;
import edu.tamu.tcat.trc.entries.bib.Work;
import edu.tamu.tcat.trc.entries.bib.dto.AuthorRefDV;
import edu.tamu.tcat.trc.entries.bib.dto.EditionDV;
import edu.tamu.tcat.trc.entries.bib.dto.PublicationInfoDV;
import edu.tamu.tcat.trc.entries.bib.dto.TitleDV;
import edu.tamu.tcat.trc.entries.bib.dto.VolumeDV;
import edu.tamu.tcat.trc.entries.bib.dto.WorkDV;

public class WorkSolrProxy
{
   private final static String id = "id";
   private final static String authorIds = "authorIds";
   private final static String authorNames = "authorNames";
   private final static String authorRoles = "authorRole";
   private final static String titleTypes = "titleTypes";
   private final static String language = "lang";
   private final static String titles = "titles";
   private final static String subtitles = "subtitles";
   private final static String publisher = "publisher";
   private final static String pubLocation = "publisherLocation";
   private final static String pubDateString = "publishDateString";
   private final static String pubDateValue = "publishDateValue";
   private final static String docSeries = "series";
   private final static String docSummary = "summary";

   private final static String editionId = "editionId";
   private final static String editionName = "editionName";

   private final static String volumeId = "volumeId";
   private final static String volumeNumber = "volumeNumber";


   private SolrInputDocument document;
   private Map<String,Object> fieldModifier;
   private final static String SET = "set";

   public WorkSolrProxy()
   {
      document = new SolrInputDocument();
   }

   public static WorkSolrProxy createWork(Work work)
   {
      WorkSolrProxy proxy = new WorkSolrProxy();
      WorkDV workDV = new WorkDV(work);

      proxy.addField(id, workDV.id);
      proxy.addAuthors(workDV.authors);
      proxy.addTitle(workDV.titles);
      proxy.addField(docSeries, workDV.series);
      proxy.addField(docSummary, workDV.summary);
      return proxy;
   }

   public static WorkSolrProxy createEdition(String workId, Edition edition)
   {
      EditionDV editionDV = new EditionDV(edition);
      StringBuilder editionId = new StringBuilder(workId)
                               .append(":")
                               .append(editionDV.id);

      WorkSolrProxy proxy = new WorkSolrProxy();
      proxy.addField(id, editionId.toString());
      proxy.addField(editionName, editionDV.editionName);
      proxy.addAuthors(editionDV.authors);
      proxy.addTitle(editionDV.titles);
      proxy.addPublication(editionDV.publicationInfo);
      proxy.addField(docSeries, editionDV.series);
      proxy.addField(docSummary, editionDV.summary);
      return proxy;
   }

   public static WorkSolrProxy createVolume(String workId, Edition edition, Volume volume)
   {
      VolumeDV volumeDV = new VolumeDV(volume);
      StringBuilder volumeId = new StringBuilder(workId)
                              .append(":")
                              .append(editionId)
                              .append(":")
                              .append(volumeDV.id);

      WorkSolrProxy proxy = new WorkSolrProxy();
      proxy.addField(id, volumeId.toString());
      proxy.addField(editionName, edition.getEditionName());
      proxy.addField(volumeNumber, volumeDV.volumeNumber);
      proxy.addAuthors(volumeDV.authors);
      proxy.addTitle(volumeDV.titles);
      proxy.addPublication(volumeDV.publicationInfo);
      proxy.addField(docSeries, volumeDV.series);
      proxy.addField(docSummary, volumeDV.summary);
      return proxy;
   }

   public static WorkSolrProxy updateWork(Work work)
   {
      WorkSolrProxy proxy = new WorkSolrProxy();
      WorkDV workDV = new WorkDV(work);

      proxy.updateField(id, workDV.id, SET);
      proxy.addAuthors(workDV.authors);
      proxy.addTitle(workDV.titles);
      proxy.updateField(docSeries, workDV.series, SET);
      proxy.updateField(docSummary, workDV.summary, SET);
      return proxy;
   }

   public static WorkSolrProxy updateEdition(String workId, Edition edition)
   {
      EditionDV editionDV = new EditionDV(edition);
      StringBuilder editionId = new StringBuilder(workId)
                               .append(":")
                               .append(editionDV.id);

      WorkSolrProxy proxy = new WorkSolrProxy();
      proxy.updateField(id, editionId.toString(), SET);
      proxy.updateField(editionName, editionDV.editionName, SET);
      proxy.addAuthors(editionDV.authors);
      proxy.addTitle(editionDV.titles);
      proxy.addPublication(editionDV.publicationInfo);
      proxy.updateField(docSeries, editionDV.series, SET);
      proxy.updateField(docSummary, editionDV.summary, SET);
      return proxy;
   }

   public static WorkSolrProxy updateVolume(String workId, Edition edition, Volume volume)
   {
      VolumeDV volumeDV = new VolumeDV(volume);
      StringBuilder volumeId = new StringBuilder(workId)
                              .append(":")
                              .append(editionId)
                              .append(":")
                              .append(volumeDV.id);

      WorkSolrProxy proxy = new WorkSolrProxy();
      proxy.updateField(id, volumeId.toString(), SET);
      proxy.updateField(editionName, edition.getEditionName(), SET);
      proxy.updateField(volumeNumber, volumeDV.volumeNumber, SET);
      proxy.addAuthors(volumeDV.authors);
      proxy.addTitle(volumeDV.titles);
      proxy.addPublication(volumeDV.publicationInfo);
      proxy.updateField(docSeries, volumeDV.series, SET);
      proxy.updateField(docSummary, volumeDV.summary, SET);
      return proxy;
   }


   public SolrInputDocument getDocument()
   {
      return document;
   }

   void addField(String fieldName, String fieldValue)
   {
      document.addField(fieldName, fieldValue);
   }


   void updateField(String fieldName, String value, String updateType)
   {
      fieldModifier = new HashMap<>(1);
      fieldModifier.put(updateType, value);
      document.addField(fieldName, fieldModifier);
   }

   void addAuthors(List<AuthorRefDV> authors)
   {
      for (AuthorRefDV author : authors)
      {
         if (author.authorId != null)
            document.addField(authorIds, author.authorId);
         else
            document.addField(authorIds, "");
         document.addField(authorNames, author.name);
         document.addField(authorRoles, author.role);
      }
   }



   void updateAuthors(List<AuthorRefDV> authors, String updateType)
   {
      Set<String> allIds = new HashSet<>();
      Set<String> allNames = new HashSet<>();
      Set<String> allRoles = new HashSet<>();
      fieldModifier = new HashMap<>(1);

      for (AuthorRefDV author : authors)
      {
         if (author.authorId != null)
            allIds.add(author.authorId);
         else
            document.addField(authorIds, "");
         allNames.add(author.name);
         allRoles.add(author.role);
      }

      fieldModifier.put(updateType, allIds);
      document.addField(authorIds, fieldModifier);

      fieldModifier.clear();
      fieldModifier.put(updateType, allNames);
      document.addField(authorNames, fieldModifier);

      fieldModifier.clear();
      fieldModifier.put(updateType, allRoles);
      document.addField(authorRoles, fieldModifier);

   }

   void addTitle(Collection<TitleDV> titlesDV)
   {
      for (TitleDV title : titlesDV)
      {
         document.addField(titleTypes, title.type);
         document.addField(language, title.lg);
         document.addField(titles, title.title);
         document.addField(subtitles, title.subtitle);
      }
   }

   void updateTitle(Collection<TitleDV> titlesDV, String updateType)
   {
      Set<String> allTypes = new HashSet<>();
      Set<String> allLangs = new HashSet<>();
      Set<String> allTitles = new HashSet<>();
      Set<String> allSubTitles = new HashSet<>();
      fieldModifier = new HashMap<>(1);

      for (TitleDV title : titlesDV)
      {
         allTypes.add(title.type);
         allLangs.add(title.lg);
         allTitles.add(title.title);
         allSubTitles.add(title.subtitle);
      }

      fieldModifier.put(updateType, allTypes);
      document.addField(titleTypes, fieldModifier);

      fieldModifier.clear();
      fieldModifier.put(updateType, allLangs);
      document.addField(language, fieldModifier);

      fieldModifier.clear();
      fieldModifier.put(updateType, allTitles);
      document.addField(titles, fieldModifier);

      fieldModifier.clear();
      fieldModifier.put(updateType, allSubTitles);
      document.addField(subtitles, fieldModifier);
   }

   void addPublication(PublicationInfoDV publication)
   {
      if (publication.publisher != null)
         document.addField(publisher, publication.publisher);
      else
         document.addField(publisher, "");
      if (publication.place != null)
         document.addField(pubLocation, publication.place);
      else
         document.addField(pubLocation, "");

      DateDescriptionDV dateDescription = publication.date;
      document.addField(pubDateString, dateDescription.description);

      if (dateDescription.calendar != null)
         document.addField(pubDateValue, convertDate(dateDescription.calendar));
   }

   void updatePublication(PublicationInfoDV publication, String updateType)
   {
      if (publication.publisher != null)
         document.addField(publisher, publication.publisher);
      else
         document.addField(publisher, "");
      if (publication.place != null)
         document.addField(pubLocation, publication.place);
      else
         document.addField(pubLocation, "");

      DateDescriptionDV dateDescription = publication.date;
      document.addField(pubDateString, dateDescription.description);

      if (dateDescription.calendar != null)
         document.addField(pubDateValue, convertDate(dateDescription.calendar));
   }

   private String convertDate(String localDate)
   {
      return localDate + "T00:00:00Z";
   }
}