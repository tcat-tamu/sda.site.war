package edu.tamu.tcat.sda.catalog.psql;

import java.net.URI;
import java.util.Collection;
import java.util.List;

import edu.tamu.tcat.sda.catalog.NoSuchCatalogRecordException;
import edu.tamu.tcat.sda.catalog.works.EditionMutator;
import edu.tamu.tcat.sda.catalog.works.VolumeMutator;
import edu.tamu.tcat.sda.catalog.works.dv.AuthorRefDV;
import edu.tamu.tcat.sda.catalog.works.dv.EditionDV;
import edu.tamu.tcat.sda.catalog.works.dv.PublicationInfoDV;
import edu.tamu.tcat.sda.catalog.works.dv.TitleDV;
import edu.tamu.tcat.sda.catalog.works.dv.VolumeDV;

public class EditionMutatorImpl implements EditionMutator
{
   private final EditionDV edition;


   public EditionMutatorImpl(EditionDV edition)
   {
      this.edition = edition;
   }


   @Override
   public void setAll(EditionDV edition)
   {
      setAuthors(edition.authors);
      setTitles(edition.titles);
      setOtherAuthors(edition.otherAuthors);
      setEdition(edition.edition);
      setPublicationInfo(edition.publicationInfo);
      setSummary(edition.summary);
      setSeries(edition.series);
      setImages(edition.images);
      setTags(edition.tags);
      setNotes(edition.notes);

      for (VolumeDV volume : edition.volumes) {
         VolumeMutator mutator;

         try {
            mutator = (null == volume.id) ? createVolume() : editVolume(volume.id);
         }
         catch (NoSuchCatalogRecordException e) {
            // TODO: Log warning message
            mutator = createVolume();
         }

         mutator.setAll(volume);
      }
   }

   @Override
   public void setAuthors(List<AuthorRefDV> authors)
   {
      edition.authors = authors;
   }

   @Override
   public void setTitles(List<TitleDV> titles)
   {
      edition.titles = titles;
   }

   @Override
   public void setOtherAuthors(List<AuthorRefDV> otherAuthors)
   {
      edition.otherAuthors = otherAuthors;
   }

   @Override
   public void setEdition(String edition)
   {
      // HACK: this isn't confusing at all... </sarcasm>
      this.edition.edition = edition;
   }

   @Override
   public void setPublicationInfo(PublicationInfoDV pubInfo)
   {
      edition.publicationInfo = pubInfo;
   }

   @Override
   public void setSummary(String summary)
   {
      edition.summary = summary;
   }

   @Override
   public void setSeries(String series)
   {
      edition.series = series;
   }

   @Override
   public void setImages(List<URI> images)
   {
      edition.images = images;
   }

   @Override
   public void setTags(Collection<String> tags)
   {
      edition.tags = tags;
   }

   @Override
   public void setNotes(Collection<String> notes)
   {
      edition.notes = notes;
   }

   @Override
   public VolumeMutator createVolume()
   {
      VolumeDV volume = new VolumeDV();
      edition.volumes.add(volume);
      return new VolumeMutatorImpl(volume);
   }

   @Override
   public VolumeMutator editVolume(String id) throws NoSuchCatalogRecordException
   {
      for (VolumeDV volume : edition.volumes) {
         if (volume.id.equals(id)) {
            return new VolumeMutatorImpl(volume);
         }
      }
      throw new NoSuchCatalogRecordException("Unable to find volume with id [" + id + "].");
   }

}