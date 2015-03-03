package edu.tamu.tcat.sda.catalog.psql.impl;

import edu.tamu.tcat.catalogentries.events.DateDescription;
import edu.tamu.tcat.catalogentries.events.dv.DateDescriptionDV;
import edu.tamu.tcat.catalogentries.works.PublicationInfo;
import edu.tamu.tcat.catalogentries.works.dv.PublicationInfoDV;

public class PublicationImpl implements PublicationInfo
{
   private final String place;
   private final String publisher;
   private final DateDescription date;

   public PublicationImpl(PublicationInfoDV pubInfo)
   {
      this.place = pubInfo.place;
      this.publisher = pubInfo.publisher;
      this.date = DateDescriptionDV.convert(pubInfo.date);
   }

   @Override
   public String getLocation()
   {
      return place;
   }

   @Override
   public String getPublisher()
   {
      return publisher;
   }

   @Override
   public DateDescription getPublicationDate()
   {
      return date;
   }

}
