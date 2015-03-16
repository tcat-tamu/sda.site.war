package edu.tamu.tcat.trc.entries.bib.copy.hathitrust;

import edu.tamu.tcat.trc.entries.bib.copy.discovery.DigitalCopyProxy;

public class HTCopyProxy implements DigitalCopyProxy
{
   public String ident;
   public String title;
   public String description;
   public String copyProvider = "HathiTrust";
   public String sourceSummary;
   public String rights;
   public String publicationDate;

   public HTCopyProxy()
   {
      // TODO Auto-generated constructor stub
   }

   @Override
   public String getIdentifier()
   {
      return this.ident;
   }

   @Override
   public String getTitle()
   {
      return this.title;
   }

   @Override
   public String getDescription()
   {
      return this.description;
   }

   @Override
   public String getCopyProvider()
   {
      return this.copyProvider;
   }

   @Override
   public String getSourceSummary()
   {
      return this.sourceSummary;
   }

   @Override
   public String getRights()
   {
      return this.rights;
   }

   @Override
   public String getPublicationDate()
   {
      return this.publicationDate;
   }

}
