package edu.tamu.tcat.sda.rest.export.csv;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import edu.tamu.tcat.trc.entries.common.HistoricalEvent;
import edu.tamu.tcat.trc.entries.types.bio.BiographicalEntry;
import edu.tamu.tcat.trc.entries.types.bio.PersonName;
import edu.tamu.tcat.trc.entries.types.bio.repo.BiographicalEntryRepository;

@Path("/export/authors")
public class BioCsvExportResource
{
   private static final Logger logger = Logger.getLogger(BioCsvExportResource.class.getName());

   private static final List<String> csvHeaders = Arrays.asList(
         "id",
         "Display Name",
         "Family Name",
         "Given Name",
         "Middle Name",
         "Title",
         "Suffix",
         "Birth Place",
         "Birth Date",
         "Birth Date Label",
         "Death Place",
         "Death Date",
         "Death Date Lable",
         "remove");

   private final BiographicalEntryRepository repo;

   public BioCsvExportResource(BiographicalEntryRepository peopleRepository)
   {
      repo = peopleRepository;
   }

   private void doWrite(Writer writer)
   {
      try
      {
         CsvExporter<BiographicalEntry, PersonCsvRecord> exporter =
               new CsvExporter<>(PersonCsvRecord::create, PersonCsvRecord.class);

         writer.write(String.join(", ", csvHeaders));
         writer.write(System.lineSeparator());
         Iterator<BiographicalEntry> iterator = repo.listAll();
         exporter.export(iterator, writer);
         writer.flush();
      }
      catch (IOException ex)
      {
         // NOTE: various clients may abort a connection before the file download completes
         //       resulting in this error.
         logger.log(Level.FINE, "Failed to export author list. Unable to read data from repository.", ex);
         Response resp = Response.serverError()
               .entity("Cannot complete author list export. Likely caused by closed connection.")
               .build();
         throw new WebApplicationException(resp);
      }
      catch (Exception ex)
      {
         logger.log(Level.SEVERE, "Failed to export author list. Unexpected error.", ex);
         Response resp = Response.serverError()
               .entity("Cannot export author list. See server logs for details.")
               .build();
         throw new WebApplicationException(resp);
      }
   }


   @GET
   @Produces("text/csv; charset=UTF-8")
   public Response getBasicAuthorListCSV()
   {
      StreamingOutput stream = os -> {
         Writer writer = new BufferedWriter(new OutputStreamWriter(os));

         //HACK: fork a background thread to do the CSV export using Jackson because
         //      if it is done in the jersey/REST call stack, it will get the wrong
         //      version of jackson via different OSGI classloaders. The fork forces the
         //      jackson classes to be loaded from this class's classloader instead of
         //      Jersey's.
         ExecutorService svc = Executors.newSingleThreadExecutor();
         Future<?> future = svc.submit(() -> {
            doWrite(writer);
            return null;
         });
         try {
            // wait on some rediculous amount, because it's better than infinity
            future.get(5, TimeUnit.MINUTES);
         }
         catch (Exception e)
         {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
         }
         finally
         {
            svc.shutdown();
         }
      };

      Response response = Response.ok(stream).build();
      response.getHeaders().add("Content-Disposition", "inline; filename=\"authors.csv\"");
      return response;
   }

   @JsonPropertyOrder
   public static class PersonCsvRecord
   {
      private static PersonCsvRecord create(BiographicalEntry person)
      {
         PersonCsvRecord record = new PersonCsvRecord();
         record.id = person.getId();
         PersonName canonicalName = person.getCanonicalName();
         if (canonicalName != null)
         {
            record.displayName = canonicalName.getDisplayName();
            record.familyName = canonicalName.getFamilyName();
            record.givenName = canonicalName.getGivenName();
            record.middleName = canonicalName.getMiddleName();
            record.title = canonicalName.getTitle();
            record.suffix = canonicalName.getSuffix();
         }

         HistoricalEvent birth = person.getBirth();
         if (birth != null)
         {
            record.birthPlace = birth.getLocation();
            LocalDate date = birth.getDate().getCalendar();
            if (date != null)
            {
               record.birthDate = DateTimeFormatter.ISO_LOCAL_DATE.format(date);
            }
            record.birthDateLable = birth.getDate().getDescription();
         }

         HistoricalEvent death = person.getDeath();
         if (death != null)
         {
            record.deathPlace = death.getLocation();
            LocalDate date = death.getDate().getCalendar();
            if (date != null)
            {
               record.deathDate = DateTimeFormatter.ISO_LOCAL_DATE.format(date);
            }
            record.deathDateLable = death.getDate().getDescription();
         }

         return record;
      }

      public String id;
      public String displayName;
      public String familyName;
      public String givenName;
      public String middleName;
      public String title;
      public String suffix;

      public String birthPlace;
      public String birthDate;
      public String birthDateLable;

      public String deathPlace;
      public String deathDate;
      public String deathDateLable;

      public boolean remove = false;
   }
}
