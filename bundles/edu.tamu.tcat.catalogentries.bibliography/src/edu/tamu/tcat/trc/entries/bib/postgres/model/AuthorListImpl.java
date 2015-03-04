package edu.tamu.tcat.trc.entries.bib.postgres.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.tamu.tcat.trc.entries.bib.AuthorList;
import edu.tamu.tcat.trc.entries.bib.AuthorReference;
import edu.tamu.tcat.trc.entries.bib.dto.AuthorRefDV;

public class AuthorListImpl implements AuthorList
{
   private List<AuthorReference> authRef;

   public AuthorListImpl(List<AuthorRefDV> authList)
   {
      authRef = new ArrayList<AuthorReference>();
      if (authList != null)
      {
         for (AuthorRefDV ref : authList)
         {
            authRef.add(new AuthorReferenceImpl(ref));
         }
      }
   }

   @Override
   public Iterator<AuthorReference> iterator()
   {
      return authRef.iterator();
   }

   @Override
   public AuthorReference get(int ix) throws IndexOutOfBoundsException
   {
      return authRef.get(ix);
   }

   @Override
   public int size()
   {
      return authRef.size();
   }

}
