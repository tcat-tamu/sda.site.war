package edu.tamu.tcat.trc.entries.bib.copy.discovery;

import java.util.Collection;

public interface CopySearchResult
{
   // TODO list the sources searched
   // TODO get results by source
   // TODO get executed query (with any default modifications)
   // TODO serialize and repeat

   Collection<DigitalCopyProxy> asCollection();

   CopySearchResult getNextPage();

   CopySearchResult getPrevPage();
}