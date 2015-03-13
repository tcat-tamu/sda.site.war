package edu.tamu.tcat.trc.entries.bib.copy.discovery;

import edu.tamu.tcat.trc.entries.bib.copy.CopyResolverStrategy;


/**
 *  Defines simple metadata for uniquely identifying a digital copy and presenting basic
 *  information about that copy to aid in the discovery process.
 *
 */
public interface DigitalCopyProxy
{
   /**
    * @return An identifier that uniquely identifies this copy within the scope of a particular
    *       {@link CopyResolverStrategy}. Note that this identifier should be in a format that
    *       can be recognized and parsed only by the the {@link CopyResolverStrategy} that
    *       created this proxy.
    */
   String getIdentifier();

   /**
    * @return A short title for this copy suitable for display. This will be used to assist
    *       end users in assessing the relevance of this proxy for their particular needs.
    */
   String getTitle();

   /**
    * @return A short description of the copy.
    */
   String getDescription();

   /**
    * @return The service that hosts and/or provides access to the digital copy of this work,
    *       such as Google Books, HathiTrust, Internet Archive of Local. Suitable for display.
    */
   String getCopyProvider();

   /**
    * @return Basic information about the source of the digital copy such as University of
    *       Michigan. Intended for user display to aid discovery of relevant copies rather that
    *       to provide detailed metadata about the location of the source object or the
    *       digitization process.
    */
   String getSourceSummary();

   /**
    * @return A string based representation of the publication date.
    */
   String getPublicationDate();

   /**
    * @return The rights to access a digital content, defined by the service provider.
    */
   String getRights();
}
