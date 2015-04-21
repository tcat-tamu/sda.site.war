package edu.tamu.tcat.trc.entries.bib;

import java.net.URI;
import java.util.UUID;

public interface EditCopyReferenceCommand
{
   /**
    * @return A copy of the current state of this copy command.
    */
   CopyReference getCurrentState();

   /**
    * @return The unique identifier for the {@link CopyReference} being edited by this command.
    */
   UUID getId();

   /**
    * @param uri The URI of the associated bibliographic entry. Note that digital copies may be
    *       attached to works, editions or volumes.
    */
   EditCopyReferenceCommand setAssociatedEntry(URI uri);

   /**
    * @param id The unique identifier for the associated digital copy. See
    *       {@link CopyResolverRegistry} for more detail on how to retrieve a reference to
    *       the digital copy.
    */
   EditCopyReferenceCommand setCopyId(String id);  // TODO need info about provider, type, etc.

   /**
    * @param title A title that describes this relationship between the work and the digital copy.
    *       Examples would include 'Black and White', 'High Resolution Color Scan',
    *       'Harvard Copy'. May be an empty string. {@code null} values will be converted to the
    *       empty string.
    */
   EditCopyReferenceCommand setTitle(String title);

   /**
    * @param summary A short description (if desired) that describes interesting features of the
    *       linked copy to aid users in understanding its relevance to their reading. For
    *       example, this might be used to note missing pages, significant annotations or
    *       provenance, or the accuracy of OCR.  May be an empty string. {@code null} values
    *       will be converted to the empty string.
    */
   EditCopyReferenceCommand setSummary(String summary);

   /**
    * @param description A description of the usage rights of this work.  May be an empty
    *       string. {@code null} values will be converted to the empty string.
    */
   EditCopyReferenceCommand setRights(String description);     // TODO use structured model for rights.

   /**
    * Attempts to executed the provided updates.
    */
   void execute();      // TODO need a response handler of some form
}