package es.um.sisdist.backend.dao.logs;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import es.um.sisdist.backend.dao.models.LogDTO;
import es.um.sisdist.backend.dao.utils.Lazy;


public class SQLLogsDAO implements ILogsDAO
{
    Supplier<Connection> conn;

    public SQLLogsDAO()
    {
        conn = Lazy.lazily(() -> {
            try
            {
                Class.forName("com.mysql.cj.jdbc.Driver").getConstructor().newInstance();

                String sqlServerName = Optional.ofNullable(System.getenv("SQL_SERVER")).orElse("localhost");
                String dbName = Optional.ofNullable(System.getenv("DB_NAME")).orElse("ssdd");

                return DriverManager.getConnection(
                    "jdbc:mysql://" + sqlServerName + "/" + dbName + "?user=root&password=root");
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return null;
            }
        });
    }


    @Override
    public void saveLog(String userId, String dialogueId, String dialogueJson, long timestamp)
    {
        try
        {
            PreparedStatement stm = conn.get().prepareStatement(
                "INSERT INTO logs (user_id, dialogue_id, dialogue_json, timestamp) VALUES (?, ?, ?, ?)"
            );
            stm.setString(1, userId);
            stm.setString(2, dialogueId);
            stm.setString(3, dialogueJson);
            stm.setLong(4, timestamp);

            stm.executeUpdate();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public List<LogDTO> getLogsForUser(String userId)
    {
        List<LogDTO> logs = new ArrayList<>();
        try
        {
            PreparedStatement stm = conn.get().prepareStatement(
                "SELECT dialogue_id, dialogue_json, timestamp FROM logs WHERE user_id = ? ORDER BY timestamp DESC"
            );
            stm.setString(1, userId);

            ResultSet rs = stm.executeQuery();

            while (rs.next())
            {
                LogDTO log = new LogDTO();
                log.setDialogueId(rs.getString("dialogue_id"));
                log.setDialogueJson(rs.getString("dialogue_json"));
                log.setTimestamp(rs.getLong("timestamp"));

                logs.add(log);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return logs;
    }

    @Override
    public boolean deleteLog(String userId, String dialogueId)
    {
        try
        {
            PreparedStatement stm = conn.get().prepareStatement(
                "DELETE FROM logs WHERE user_id = ? AND dialogue_id = ?"
            );
            stm.setString(1, userId);
            stm.setString(2, dialogueId);

            int rows = stm.executeUpdate();
            return rows > 0;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return false;
        }
    }
}
