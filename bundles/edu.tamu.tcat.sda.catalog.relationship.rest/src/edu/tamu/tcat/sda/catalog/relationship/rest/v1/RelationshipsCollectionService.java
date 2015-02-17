package edu.tamu.tcat.sda.catalog.relationship.rest.v1;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.google.common.base.Joiner;

import edu.tamu.tcat.sda.catalog.relationship.AnchorSet;
import edu.tamu.tcat.sda.catalog.relationship.EditRelationshipCommand;
import edu.tamu.tcat.sda.catalog.relationship.Relationship;
import edu.tamu.tcat.sda.catalog.relationship.RelationshipRepository;
import edu.tamu.tcat.sda.catalog.relationship.RelationshipSearchService;
import edu.tamu.tcat.sda.catalog.relationship.model.RelationshipDV;
import edu.tamu.tcat.sda.catalog.relationship.rest.v1.model.RelationshipId;

@Path("/relationships")
public class RelationshipsCollectionService
{
   private static final Logger logger = Logger.getLogger(RelationshipsCollectionService.class.getName());

   private RelationshipRepository repo;
   private RelationshipSearchService service;

   public enum RelnDirection
   {
      from, to, any;
   }

   public void setRepository(RelationshipRepository repo)
   {
      this.repo = repo;
   }

   public void setRelationshipService(RelationshipSearchService service)
   {
      this.service = service;

   }

   public void activate()
   {
   }

   public void dispose()
   {
   }

   // /relationships?entity=<uri>      return all entities related to the supplied entity
   // /relationships?entity=<uri>[&type=<type_id>][&direction=from|to|any]
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public List<RelationshipDV> getRelationships(
         @QueryParam(value="entity") URI entity,
         @QueryParam(value="type") String type,
         @QueryParam(value="direction") String d)
   {
      RelnDirection direction = parseDirection(d);

      // TODO - This will be taken care of on jira ticket https://issues.citd.tamu.edu/browse/RI-6
      List<RelationshipDV> relnDV = new ArrayList<>();
      Iterable<Relationship> foundRelns = service.findRelationshipsFor(entity);
      for (Relationship reln : foundRelns)
      {
         // HACK: querying by type should be part of the RelationshipSearchService API
         if (type != null && !reln.getType().getIdentifier().equals(type)) {
            continue;
         }

         // HACK: querying by direction should be part of the RelationshipSearchService API
         if (compareDirection(entity, reln, direction)) {
            relnDV.add(RelationshipDV.create(reln));
         }
      }
      return relnDV;
   }

   /**
    * Compares the direction of a relationship relative to an entity URI against a given direction.
    *
    * @param source reference entity
    * @param reln relationship contain
    * @param compareTo direction against which to compare
    * @return whether the direction of the relationship matches the {@code compareTo} parameter
    */
   private boolean compareDirection(URI entity, Relationship reln, RelnDirection compareTo)
   {
      if (compareTo == RelnDirection.any) {
         return true;
      }

      AnchorSet anchorSet;
      if (compareTo == RelnDirection.to) {
         anchorSet = reln.getTargetEntities();
      } else if (compareTo == RelnDirection.from) {
         anchorSet = reln.getRelatedEntities();
      } else {
         throw new IllegalStateException("Unexpected value for relation direction: " + compareTo.toString());
      }

      Set<URI> uris = anchorSet.getAnchors().parallelStream()
         .flatMap(a -> a.getEntryIds().parallelStream())
         .collect(Collectors.toSet());

      return uris.contains(entity);
   }

   private RelnDirection parseDirection(String d)
   {
      if (d == null)
         return RelnDirection.any;

      try
      {
         return RelnDirection.valueOf(d.toLowerCase());
      }
      catch (IllegalArgumentException iea)
      {
         Joiner joiner = Joiner.on(", ");
         throw new BadRequestException("Invalid value for query parameter 'direction' [" + d + "]. Must be one of the following: " + joiner.join(RelnDirection.values()));
      }
   }

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   public RelationshipId createRelationship(RelationshipDV relationship)
   {
      RelationshipId results = new RelationshipId();
      EditRelationshipCommand createCommand;
      try
      {
         createCommand = repo.create();
         createCommand.setAll(relationship);

         RelationshipId result = new RelationshipId();
         result.id = createCommand.execute().get();
      }
      catch (Exception e)
      {
         logger.severe("An error occured during the creating relationship process. Exception: " + e);
      }

      return results;
   }
}
