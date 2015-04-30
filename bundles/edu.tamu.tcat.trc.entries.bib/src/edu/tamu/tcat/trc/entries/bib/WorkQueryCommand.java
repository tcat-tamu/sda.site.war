package edu.tamu.tcat.trc.entries.bib;

import java.time.Year;
import java.util.List;

import edu.tamu.tcat.trc.entries.bib.dto.WorkInfo;


/**
 * Command for use in querying the underlying search service
 */
public interface WorkQueryCommand
{

   // TODO probably should use a builder pattern.
   // TODO should be able to serialize a query

   /**
    * Supply a free-text, keyword query to be executed. In general, the supplied query should
    * be executed against a wide range of fields (e.g., author, title, abstract, publisher, etc.)
    * with different fields being assigned different levels of boosting (per-field weights).
    * The specific fields to be searched and the relative weights associated with different
    * fields is a
    *
    * @param q
    * @return
    */
   WorkQueryCommand setQuery(String q);

   /**
    * @param q The value to search for in the title.
    * @return
    */
   WorkQueryCommand setTitleQuery(String q);

   /**
    * Set the name of the author to search for. A best effort will be made to match books whose
    * authors correspond to this name, either specifically within the bibliographic table or
    * within the affiliated person record.
    *
    * @param authorName
    * @return
    */
   WorkQueryCommand setAuthorName(String authorName);

   /**
    * Filter results based on the supplied list of author ids. Only entries that
    * specifically match the supplied authors will be returned.
    *
    * @param ids
    * @return
    */
   WorkQueryCommand filterByAuthor(String... ids);

   /**
    * Filter results to return only those entries that are published between the supplied dates.
    *
    * @param after The lower bound of the date filter. If {@code null} indicates that no
    *       lower bound should be enforced.
    * @param before The upper bound of the date filter. If {@code null} indicates that no upper
    *       bound should be enforced.
    * @return
    */
   WorkQueryCommand filterByDate(Year after, Year before);

   /**
    * Filter results to a specific geographical location.
    *
    * @param location
    * @return
    */
   WorkQueryCommand filterByLocation(String location);

   /**
    * Sets the index of the first result to be returned. Useful in conjunction with
    * {@link WorkQueryCommand#setMaxResults(int) } to support result paging. Note that
    * implementations are <em>strongly</em> encouraged to make a best-effort attempt to
    * preserve result order across multiple invocations of the same query.  In general, this
    * is a challenging problem in the face of updates to the underlying index and implementations
    * are not required to guarantee result order consistency of result order across multiple
    * calls.
    *
    * @param start
    * @return
    */
   WorkQueryCommand setStartIndex(int start);

   /**
    * Specify the maximum number of results to be returned. Implementations may return fewer
    * results but must not return more.
    *
    * @param ct
    * @return
    */
   WorkQueryCommand setMaxResults(int ct);

   List<WorkInfo> getResults();
}
