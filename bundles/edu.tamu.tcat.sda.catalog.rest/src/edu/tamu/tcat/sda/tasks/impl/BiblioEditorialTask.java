package edu.tamu.tcat.sda.tasks.impl;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.sda.tasks.EditWorkItemCommand;
import edu.tamu.tcat.sda.tasks.EditorialTask;
import edu.tamu.tcat.sda.tasks.PartialWorkItemSet;
import edu.tamu.tcat.sda.tasks.TaskSubmissionMonitor;
import edu.tamu.tcat.sda.tasks.WorkItem;
import edu.tamu.tcat.sda.tasks.WorkItemRepository;
import edu.tamu.tcat.sda.tasks.workflow.Workflow;
import edu.tamu.tcat.sda.tasks.workflow.WorkflowStage;
import edu.tamu.tcat.sda.tasks.workflow.WorkflowStageTransition;
import edu.tamu.tcat.trc.entries.types.biblio.AuthorList;
import edu.tamu.tcat.trc.entries.types.biblio.AuthorReference;
import edu.tamu.tcat.trc.entries.types.biblio.Title;
import edu.tamu.tcat.trc.entries.types.biblio.TitleDefinition;
import edu.tamu.tcat.trc.entries.types.biblio.Work;
import edu.tamu.tcat.trc.repo.IdFactory;
import edu.tamu.tcat.trc.repo.RepositoryException;
import edu.tamu.tcat.trc.repo.RepositorySchema;

public abstract class BiblioEditorialTask implements EditorialTask<Work>
{
   private static final String[] TITLE_PREFERENCE_ORDER = {"short", "canonical", "bibliographic"};

   private final String id;
   private final SqlExecutor sqlExecutor;
   private final IdFactory idFactory;
   private final Executor executor;

   private WorkItemRepository repo;

   public BiblioEditorialTask(String id, SqlExecutor sqlExecutor, IdFactory idFactory, Executor executor)
   {
      this.id = id;
      this.sqlExecutor = sqlExecutor;
      this.idFactory = idFactory;
      this.executor = executor;
   }

   @Override
   public String getId()
   {
      return id;
   }

   protected WorkItemRepository getRepository()
   {
      if (repo == null) {
         String tableName = getTableName();

         Workflow workflow = getWorkflow();
         ModelAdapter modelAdapter = new ModelAdapter(workflow::getStage);

         RepositorySchema schema = getRepositorySchema();

         repo = new WorkItemRepositoryImpl(tableName, sqlExecutor, idFactory, modelAdapter, schema);
      }

      return repo;
   }

   protected abstract RepositorySchema getRepositorySchema();

   protected abstract String getTableName();

   /**
    * @param id The id of the item to edit.
    * @return A command for editing the item.
    */
   public EditWorkItemCommand editItem(String id)
   {
      WorkItemRepository repo = getRepository();
      return repo.editItem(id);
   }

   @Override
   public WorkItem addItem(Work entity) throws IllegalArgumentException
   {
      WorkItemRepository repo = getRepository();
      EditWorkItemCommand command = repo.createItem();
      applyCreate(command, entity);

      try
      {
         String id = command.execute().get();
         return repo.getItem(id);
      }
      catch (InterruptedException | ExecutionException e)
      {
         throw new IllegalStateException("Unable to create work item", e);
      }
      catch (RepositoryException e)
      {
         throw new IllegalStateException("Work item supposedly created, but unable to retrieve it", e);
      }
   }

   @Override
   public void addItems(Supplier<Work> entities, TaskSubmissionMonitor monitor)
   {
      WorkItemRepository repo = getRepository();

      executor.execute(() -> {
         Work entity = entities.get();
         while (entity != null)
         {
            EditWorkItemCommand command = repo.createItem();
            applyCreate(command, entity);

            try
            {
               String id = command.execute().get();
               WorkItem workItem = repo.getItem(id);
               monitor.created(new BasicWorkItemCreationRecord<>(workItem, id));
            }
            catch (ExecutionException | InterruptedException e)
            {
               // TODO: Not sure what to pass in as the first argument: Should it be the WorkItem (that was not created) or the Work?
               monitor.failed(new BasicWorkItemCreationError<>(null, "unable to fetch created work item for the given entity", e));
            }
            catch (RepositoryException e)
            {
               // TODO: Not sure what to pass in as the first argument: Should it be the WorkItem (that was not created) or the Work?
               monitor.failed(new BasicWorkItemCreationError<>(null, "unable to fetch created work item", e));
            }

            // prime next loop cycle
            entity = entities.get();
         }
         monitor.finished();
      });
   }

   @Override
   public WorkItem getItem(String id)
   {
      WorkItemRepository repo = getRepository();
      return repo.getItem(id);
   }

   @Override
   public PartialWorkItemSet getItems(WorkflowStage stage, int start, int ct)
   {
      WorkItemRepository repo = getRepository();
      Iterable<WorkItem> iterable = () -> repo.getAllItems();

      // HACK: highly inefficient. Need better support from backing repo for paged data queries
      // TODO refactor
      String id = stage.getId();
      List<WorkItem> items = StreamSupport.stream(iterable.spliterator(), false)
                                    .filter(workItem -> id.equals(workItem.getStage().getId()))
                                    .collect(Collectors.toList());

      return new PwisImpl(stage, items, start, ct);
   }

   @Override
   public WorkItem transition(WorkItem item, WorkflowStageTransition transition)
   {
      checkValidity(item, transition);

      // update item stage in repository
      String itemId = item.getId();
      EditWorkItemCommand command = editItem(itemId);

      command.setStage(transition.getTarget());

      // updatedId should equal itemId, but the code below mainly serves to synchronize the update process and as a sanity check.
      try
      {
         String updatedId = command.execute().get();
         return repo.getItem(updatedId);
      }
      catch (InterruptedException | ExecutionException e)
      {
         String message = MessageFormat.format("Unable to transition item {0} to stage {1}.", itemId, transition.getTarget().getId());
         throw new IllegalStateException(message, e);
      }
      catch (RepositoryException e)
      {
         String message = MessageFormat.format("Unable to fetch updated item {0} from repo.", itemId);
         throw new IllegalStateException(message, e);
      }
   }

   /**
    * @param item
    * @param transition
    * @throws IllegalArgumentException if the transition is not valid for the given item
    */
   protected void checkValidity(WorkItem item, WorkflowStageTransition transition)
   {
      // extract info about transition and target stage to prevent redundancy.
      String transitionId = transition.getId();
      WorkflowStage targetStage = transition.getTarget();
      String targetStageId = targetStage.getId();

      // verify that transition is valid from current stage
      WorkflowStage currentStage = item.getStage();
      List<WorkflowStageTransition> validTransitions = currentStage.getTransitions();
      boolean isValidTransition = validTransitions.stream()
            .anyMatch(t -> t.getId().equals(transitionId) && t.getTarget().getId().equals(targetStageId));

      if (!isValidTransition) {
         String message = MessageFormat.format("Invalid transition {0}.", transition.getId());
         throw new IllegalArgumentException(message);
      }
   }

   /**
    * Copies the necessary data to create a new WorkItem
    *
    * @param command
    * @param entity
    * @return The command
    */
   private EditWorkItemCommand applyCreate(EditWorkItemCommand command, Work entity)
   {
      command.setEntityRef("work", entity.getId());
      command.setLabel(getLabel(entity));
      command.setDescription("");

      Workflow workflow = getWorkflow();
      WorkflowStage initial = workflow.getInitialStage();
      command.setStage(initial);

      return command;
   }

   /**
    * Formats an HTML label for a work to be used as the work item label.
    *
    * If an author cannot be found, the placeholder "[Author Unavailable]" will be used instead.
    * If a title cannot be found, the placeholder "[Title Unavailable]" will be used instead.
    *
    * @param entity
    * @return
    */
   private static String getLabel(Work entity)
   {
      String authorLabel = getAuthorLabel(entity);
      String titleLabel = getTitleLabel(entity);

      return MessageFormat.format(
            "<span class=\"author\">{0}</span>, <span class=\"title\">{1}</span>",
            authorLabel == null ? "[Author Unavailable]" : authorLabel,
            titleLabel == null ? "[Title Unavailable]" : titleLabel
      );
   }

   /**
    * Finds the first available author with a last name and returns that author's last name.
    *
    * @param entity
    * @return The first non-empty author's last name or <code>null</code> if one cannot be found.
    */
   private static String getAuthorLabel(Work entity)
   {
      if (entity != null)
      {
         AuthorList authors = entity.getAuthors();
         if (authors != null && authors.size() > 0)
         {
            // find first available author's last name
            for (AuthorReference author : authors)
            {
               if (author == null)
               {
                  continue;
               }

               String authorLastName = author.getLastName();
               if (authorLastName == null || authorLastName.trim().isEmpty())
               {
                  continue;
               }

               return authorLastName.trim();
            }
         }
      }

      return null;
   }

   /**
    * Finds the first available title in preference order (see TITLE_PREFERENCE_ORDER constant).
    *
    * @param entity
    * @return The first non-empty full title or <code>null</code> if one cannot be found.
    */
   private static String getTitleLabel(Work entity)
   {
      if (entity != null)
      {
         TitleDefinition titleDefinition = entity.getTitle();
         if (titleDefinition != null)
         {
            // find the first available title in preferred type order
            for (String type : TITLE_PREFERENCE_ORDER)
            {
               Title title = titleDefinition.get(type);
               String fullTitle = extractFullTitle(title);
               if (fullTitle != null)
               {
                  return fullTitle;
               }
            }

            // no preferred titles available; just get any title
            Set<Title> titles = titleDefinition.get();
            if (titles != null)
            {
               for (Title title : titles)
               {
                  String fullTitle = extractFullTitle(title);
                  if (fullTitle != null)
                  {
                     return fullTitle;
                  }
               }
            }
         }
      }

      return null;
   }

   /**
    * Extracts and formats the full title of a given Title object.
    *
    * @param title
    * @return A string representing the full title or <code>null</code> if one cannot be extracted.
    */
   private static String extractFullTitle(Title title)
   {
      if (title == null)
      {
         return null;
      }

      String fullTitle = title.getFullTitle();
      if (fullTitle == null || fullTitle.trim().isEmpty())
      {
         return null;
      }

      return fullTitle.trim();
   }

   private static class PwisImpl implements PartialWorkItemSet
   {
      private WorkflowStage stage;
      private List<WorkItem> items;
      private int start;
      private int size;

      public PwisImpl(WorkflowStage stage, List<WorkItem> items, int start, int sz)
      {
         this.stage = stage;
         this.items = items;
         this.start = start;
         this.size  = sz;
      }

      @Override
      public int getTotalMatched()
      {
         return items.size();
      }


      @Override
      public int getStart()
      {
         return start;
      }

      @Override
      public int getLimit()
      {
         return size;
      }

      @Override
      public List<WorkItem> getItems()
      {
         int end = Math.min(items.size(), start + size);
         return Collections.unmodifiableList(items.subList(start, end));
      }

      @Override
      public PartialWorkItemSet getNext()
      {
         return new PwisImpl(stage, items, start + size, size);
      }
   }

   private static class BasicWorkItemCreationError<X> implements TaskSubmissionMonitor.WorkItemCreationError<X>
   {
      private final X item;
      private final String message;
      private final Exception exception;

      public BasicWorkItemCreationError(X item, String message, Exception exception)
      {
         this.item = item;
         this.message = message;
         this.exception = exception;
      }

      @Override
      public X getEntity()
      {
         return item;
      }

      @Override
      public String getMessage()
      {
         return message;
      }

      @Override
      public Exception getException()
      {
         return exception;
      }
   }

   private static class BasicWorkItemCreationRecord<X> implements TaskSubmissionMonitor.WorkItemCreationRecord<X>
   {
      private final X item;
      private final String id;

      public BasicWorkItemCreationRecord(X item, String id)
      {
         this.item = item;
         this.id = id;
      }

      @Override
      public X getEntity()
      {
         return item;
      }

      @Override
      public String getWorkItemId()
      {
         return id;
      }
   }
}