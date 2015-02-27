package edu.tamu.tcat.sda.catalog.copies;

import java.util.Collection;

import edu.tamu.tcat.hathitrust.Record;
import edu.tamu.tcat.sda.catalog.works.Edition;
import edu.tamu.tcat.sda.catalog.works.Volume;
import edu.tamu.tcat.sda.catalog.works.Work;
import edu.tamu.tcat.sda.catalog.works.dv.EditionDV;
import edu.tamu.tcat.sda.catalog.works.dv.VolumeDV;
import edu.tamu.tcat.sda.catalog.works.dv.WorkDV;

/**
 *  This class provides the abilitiy to link a {@link DigitalContentProvider} and their digital content to a
 *  {@link Work},{@link Volume} or {@link Edition}.
 */
public interface DigitalCopyLinkRepository
{
    void linkWork(WorkDV work, Collection<Record> records);
    void linkVolume(VolumeDV work, Collection<Record> records);
    void linkEdition(EditionDV work, Collection<Record> records);
}
