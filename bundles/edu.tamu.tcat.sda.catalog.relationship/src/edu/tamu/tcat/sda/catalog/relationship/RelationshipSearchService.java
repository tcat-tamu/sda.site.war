package edu.tamu.tcat.sda.catalog.relationship;

import java.net.URI;

public interface RelationshipSearchService
{
   /**
    * Retrieves all relationships associated with a particular catalog entry.
    *
    * @param entry
    * @return
    * FIXME belongs in search
    */
   Iterable<Relationship> findRelationshipsFor(URI entry);

   Iterable<Relationship> findRelationshipsBy(URI creator);
}