package edu.tamu.tcat.sda.catalog.psql.impl;

import edu.tamu.tcat.sda.catalog.works.Title;
import edu.tamu.tcat.sda.catalog.works.dv.TitleDV;

public class TitleImpl implements Title
{
   private final TitleDV title;
   
   public TitleImpl(TitleDV titleDV)
   {
      this.title = titleDV;
   }
   @Override
   public String getTitle()
   {
      return title.title;
   }

   @Override
   public String getSubTitle()
   {
      return title.subtitle;
   }

   @Override
   public String getFullTitle()
   {
      return title.title + " " + title.subtitle;
   }

}
