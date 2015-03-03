package edu.tamu.tcat.sda.catalog.psql.tasks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.postgresql.util.PGobject;

import edu.tamu.tcat.catalogentries.works.dv.WorkDV;
import edu.tamu.tcat.db.exec.sql.SqlExecutor;
import edu.tamu.tcat.oss.json.JsonException;
import edu.tamu.tcat.oss.json.JsonMapper;
import edu.tamu.tcat.sda.catalog.psql.ExecutionFailedException;

public final class PsqlCreateWorkTask implements SqlExecutor.ExecutorTask<String>
{
   private final static String insertSql = "INSERT INTO works (id, work) VALUES(?, ?)";

   private final WorkDV work;
   private final JsonMapper jsonMapper;

   public PsqlCreateWorkTask(WorkDV work, JsonMapper jsonMapper)
   {
      // TODO convert to form where these can be configured using plugins/task provider, etc.
      this.work = work;
      this.jsonMapper = jsonMapper;
   }

   private String getJson()
   {
      try
      {
         return jsonMapper.asString(work);
      }
      catch (JsonException jpe)
      {
         throw new IllegalArgumentException("Failed to serialize the supplied work [" + work + "]", jpe);
      }
   }

   @Override
   public String execute(Connection conn) throws SQLException, ExecutionFailedException
   {
      try (PreparedStatement ps = conn.prepareStatement(insertSql))
      {
         PGobject jsonObject = new PGobject();
         jsonObject.setType("json");
         jsonObject.setValue(getJson());

         ps.setLong(1, Long.parseLong(work.id));
         ps.setObject(2, jsonObject);

         int ct = ps.executeUpdate();
         if (ct != 1)
            throw new ExecutionFailedException("Failed to create work. Unexpected number of rows updates [" + ct + "]");
      }
      catch (SQLException e)
      {
         throw new IllegalStateException("Failed to create work: [" + work + "]", e);
      }

      return work.id;
   }
}