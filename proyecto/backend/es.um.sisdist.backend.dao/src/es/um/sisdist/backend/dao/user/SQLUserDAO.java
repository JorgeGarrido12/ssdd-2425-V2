/**
 *
 */
package es.um.sisdist.backend.dao.user;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import com.fasterxml.jackson.databind.ObjectMapper;


import es.um.sisdist.backend.dao.models.Dialogue;
import es.um.sisdist.backend.dao.models.DialogueEstados;
import es.um.sisdist.backend.dao.models.Prompt;
import es.um.sisdist.backend.dao.models.UsageStats;
import es.um.sisdist.backend.dao.models.User;
import es.um.sisdist.backend.dao.utils.Lazy;

/**
 * @author dsevilla
 *
 */
public class SQLUserDAO implements IUserDAO
{
    Supplier<Connection> conn;

    public SQLUserDAO()
    {
    	conn = Lazy.lazily(() -> 
    	{
    		try
    		{
    			Class.forName("com.mysql.cj.jdbc.Driver").getConstructor().newInstance();

    			// Si el nombre del host se pasa por environment, se usa aquí.
    			// Si no, se usa localhost. Esto permite configurarlo de forma
    			// sencilla para cuando se ejecute en el contenedor, y a la vez
    			// se pueden hacer pruebas locales
    			String sqlServerName = Optional.ofNullable(System.getenv("SQL_SERVER")).orElse("localhost");
    			String dbName = Optional.ofNullable(System.getenv("DB_NAME")).orElse("ssdd");
    			return DriverManager.getConnection(
                    "jdbc:mysql://" + sqlServerName + "/" + dbName + "?user=root&password=root");
    		} catch (Exception e)
    		{
    			// TODO Auto-generated catch block
    			e.printStackTrace();
            
    			return null;
    		}
    	});
    }

    @Override
    public Optional<User> getUserById(String id)
    {
        PreparedStatement stm;
        try
        {
            stm = conn.get().prepareStatement("SELECT * from users WHERE id = ?");
            stm.setString(1, id);
            ResultSet result = stm.executeQuery();
            if (result.next())
                return createUser(result);
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
        return Optional.empty();
    }


    @Override
    public Optional<User> getUserByEmail(String id)
    {
        PreparedStatement stm;
        try
        {
            stm = conn.get().prepareStatement("SELECT * from users WHERE email = ?");
            stm.setString(1, id);
            ResultSet result = stm.executeQuery();
            if (result.next())
                return createUser(result);
        } catch (SQLException e)
        {
            // Fallthrough
        }
        return Optional.empty();
    }

    private Optional<User> createUser(ResultSet result)
    {
        try
        {
            return Optional.of(new User(result.getString(1), // id
                    result.getString(2), // email
                    result.getString(3), // pwhash
                    result.getString(4), // name
                    result.getString(5), // token
                    result.getInt(6))); // visits
        } catch (SQLException e)
        {
            return Optional.empty();
        }
    }

    @Override
    public void createUser(User user)
    {
        PreparedStatement stm;
        try
        {
            // Calcular el token MD5 antes de insertar
            String token = calculateMD5Token(user);
            user.setToken(token);

            // Hashear la password que viene del User
            String passwordHash = calculateMD5(user.getPassword_hash());
            user.setPassword_hash(passwordHash);

            // Insertar en users
            stm = conn.get().prepareStatement("INSERT INTO users (id, email, password_hash, name, token, visits) VALUES (?, ?, ?, ?, ?, ?)");
            stm.setString(1, user.getId());
            stm.setString(2, user.getEmail());
            stm.setString(3, user.getPassword_hash());
            stm.setString(4, user.getName());
            stm.setString(5, user.getToken());
            stm.setInt(6, user.getVisits());

            stm.executeUpdate();

            // Insertar también en usage_stats
            PreparedStatement stmStats = conn.get().prepareStatement(
                "INSERT INTO usage_stats (user_id, total_prompts, total_conversations, last_access_timestamp) VALUES (?, ?, ?, ?)"
            );
            stmStats.setString(1, user.getId());
            stmStats.setInt(2, 0);
            stmStats.setInt(3, 0);
            stmStats.setLong(4, System.currentTimeMillis());

            stmStats.executeUpdate();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
    }


    private String calculateMD5(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(input.getBytes());
        byte[] digest = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }   


    @Override
    public void updateVisits(String id, int visits)
    {
        PreparedStatement stm;
        try
        {
            stm = conn.get().prepareStatement("UPDATE users SET visits = ? WHERE id = ?");
            stm.setInt(1, visits);
            stm.setString(2, id);

            stm.executeUpdate();
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void updateToken(String id, String token)
    {
        PreparedStatement stm;
        try
        {
            stm = conn.get().prepareStatement("UPDATE users SET token = ? WHERE id = ?");
            stm.setString(1, token);
            stm.setString(2, id);

            stm.executeUpdate();
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }



    @Override
    public void deleteUser(String id)
    {
        PreparedStatement stm;
        try
        {
            stm = conn.get().prepareStatement("DELETE FROM users WHERE id = ?");
            stm.setString(1, id);

            stm.executeUpdate();
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public boolean addVisits(String userId)
    {
        PreparedStatement stm;
        try
        {
            // Incrementar en 1 las visitas
            stm = conn.get().prepareStatement(
                "UPDATE users SET visits = visits + 1 WHERE id = ?"
            );
            stm.setString(1, userId);

            int rows = stm.executeUpdate();
            return rows > 0;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return false;
        }
    }


    private String calculateMD5Token(User user) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        String dataToHash = user.getId() + user.getEmail() + user.getPassword_hash();
        md.update(dataToHash.getBytes());
        byte[] digest = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }


    //METODOS PARA DIALOGOS Y ESTADISTICAS
    @Override
    public boolean createDialogue(String userId, Dialogue dialogue)
    {
        PreparedStatement stm;
        try
        {
            stm = conn.get().prepareStatement(
                "INSERT INTO conversations (dialogue_id, user_id, status, next_url, end_url, created_at) VALUES (?, ?, ?, ?, ?, ?)"
            );
            stm.setString(1, dialogue.getDialogueId());
            stm.setString(2, userId);
            stm.setString(3, dialogue.getStatus().toString());
            stm.setString(4, dialogue.getNextUrl());
            stm.setString(5, dialogue.getEndUrl());
            stm.setLong(6, System.currentTimeMillis());

            stm.executeUpdate();
            return true;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateDialogue(String userId, String dialogueId, Dialogue dialogue)
    {
        PreparedStatement stm;
        try
        {
            stm = conn.get().prepareStatement(
                "UPDATE conversations SET status = ?, next_url = ?, end_url = ? WHERE dialogue_id = ? AND user_id = ?"
            );
            stm.setString(1, dialogue.getStatus().toString());
            stm.setString(2, dialogue.getNextUrl());
            stm.setString(3, dialogue.getEndUrl());
            stm.setString(4, dialogueId);
            stm.setString(5, userId);

            int rows = stm.executeUpdate();
            return rows > 0;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean addPrompt(String userId, String dialogueId, String nextUrl, Prompt prompt)
    {
        PreparedStatement stm;
        try
        {
            // Insert the prompt
            stm = conn.get().prepareStatement(
                "INSERT INTO prompts (dialogue_id, prompt, answer, timestamp) VALUES (?, ?, ?, ?)"
            );
            stm.setString(1, dialogueId);
            stm.setString(2, prompt.getPrompt());
            stm.setString(3, prompt.getAnswer());
            stm.setLong(4, prompt.getTimestamp());

            stm.executeUpdate();

            // Update nextUrl and set status to BUSY
            PreparedStatement stm2 = conn.get().prepareStatement(
                "UPDATE conversations SET next_url = ?, status = ? WHERE dialogue_id = ? AND user_id = ?"
            );
            stm2.setString(1, nextUrl);
            stm2.setString(2, DialogueEstados.BUSY.toString());
            stm2.setString(3, dialogueId);
            stm2.setString(4, userId);

            stm2.executeUpdate();

            return true;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean addPromptRespuesta(String userId, String dialogueId, Prompt prompt)
    {
        PreparedStatement stm;
        try
        {
            // Update the answer of the prompt
            stm = conn.get().prepareStatement(
                "UPDATE prompts SET answer = ? WHERE dialogue_id = ? AND timestamp = ?"
            );
            stm.setString(1, prompt.getAnswer());
            stm.setString(2, dialogueId);
            stm.setLong(3, prompt.getTimestamp());

            int rows = stm.executeUpdate();

            if (rows > 0)
            {
                // Set status back to READY
                PreparedStatement stm2 = conn.get().prepareStatement(
                    "UPDATE conversations SET status = ? WHERE dialogue_id = ? AND user_id = ?"
                );
                stm2.setString(1, DialogueEstados.READY.toString());
                stm2.setString(2, dialogueId);
                stm2.setString(3, userId);

                stm2.executeUpdate();

                return true;
            }
            else
            {
                return false;
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return false;
        }
    }



    @Override
    public boolean updateDialogueEstado(String userId, String dialogueId, DialogueEstados status)
    {
        PreparedStatement stm;
        try
        {
            stm = conn.get().prepareStatement(
                "UPDATE conversations SET status = ? WHERE dialogue_id = ? AND user_id = ?"
            );
            stm.setString(1, status.toString());
            stm.setString(2, dialogueId);
            stm.setString(3, userId);

            int rows = stm.executeUpdate();

            // Si se actualizó y el nuevo estado es FINISHED → guardamos el log
            if (rows > 0 && status == DialogueEstados.FINISHED)
            {
                Dialogue dialogue = getDialogue(userId, dialogueId);

                // Convertir a JSON
                ObjectMapper objectMapper = new ObjectMapper();
                String dialogueJson = objectMapper.writeValueAsString(dialogue);

                // Guardar en logs
                PreparedStatement stmLog = conn.get().prepareStatement(
                    "INSERT INTO logs (user_id, dialogue_id, dialogue_json, timestamp) VALUES (?, ?, ?, ?)"
                );
                stmLog.setString(1, userId);
                stmLog.setString(2, dialogueId);
                stmLog.setString(3, dialogueJson);
                stmLog.setLong(4, System.currentTimeMillis());

                stmLog.executeUpdate();
            }

            return rows > 0;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }



    @Override
    public Dialogue getDialogue(String userId, String dialogueId)
    {
        Dialogue dialogue = null;
        try
        {
            // First, get conversation
            PreparedStatement stm = conn.get().prepareStatement(
                "SELECT dialogue_id, status, next_url, end_url FROM conversations WHERE dialogue_id = ? AND user_id = ?"
            );
            stm.setString(1, dialogueId);
            stm.setString(2, userId);

            ResultSet rs = stm.executeQuery();

            if (rs.next())
            {
                dialogue = new Dialogue();
                dialogue.setDialogueId(rs.getString("dialogue_id"));
                dialogue.setStatus(DialogueEstados.valueOf(rs.getString("status")));
                dialogue.setNextUrl(rs.getString("next_url"));
                dialogue.setEndUrl(rs.getString("end_url"));
            }
            else
            {
                return null;
            }

            // Then, get prompts
            PreparedStatement stm2 = conn.get().prepareStatement(
                "SELECT prompt, answer, timestamp FROM prompts WHERE dialogue_id = ? ORDER BY timestamp ASC"
            );
            stm2.setString(1, dialogueId);

            ResultSet rs2 = stm2.executeQuery();

            List<Prompt> prompts = new ArrayList<>();
            while (rs2.next())
            {
                Prompt p = new Prompt();
                p.setPrompt(rs2.getString("prompt"));
                p.setAnswer(rs2.getString("answer"));
                p.setTimestamp(rs2.getLong("timestamp"));

                prompts.add(p);
            }

            dialogue.setDialogue(prompts);

            return dialogue;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return null;
        }
    }


    @Override
    public UsageStats getUsageStats(String userId)
    {
        UsageStats stats = new UsageStats();
        try
        {
            PreparedStatement stm = conn.get().prepareStatement(
                "SELECT total_prompts, total_conversations, last_access_timestamp FROM usage_stats WHERE user_id = ?"
            );
            stm.setString(1, userId);

            ResultSet rs = stm.executeQuery();

            if (rs.next())
            {
                stats.setTotalPrompts(rs.getInt("total_prompts"));
                stats.setTotalConversations(rs.getInt("total_conversations"));
                stats.setLastAccessTimestamp(rs.getLong("last_access_timestamp"));
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return stats;
    }


    @Override
    public List<String> getDialogueIdsByUserId(String userId)
    {
        List<String> ids = new ArrayList<>();
        try
        {
            PreparedStatement stm = conn.get().prepareStatement(
                "SELECT dialogue_id FROM conversations WHERE user_id = ?"
            );
            stm.setString(1, userId);

            ResultSet rs = stm.executeQuery();

            while (rs.next())
            {
                ids.add(rs.getString("dialogue_id"));
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return ids;
    }

    //Metodos para actualizar las estadísticas de uso
    @Override
    public boolean incrementTotalConversations(String userId)
    {
        try {
            PreparedStatement stm = conn.get().prepareStatement(
                "UPDATE usage_stats SET total_conversations = total_conversations + 1 WHERE user_id = ?"
            );
            stm.setString(1, userId);
            int rows = stm.executeUpdate();
            return rows > 0;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean incrementTotalPrompts(String userId)
    {
        try {
            PreparedStatement stm = conn.get().prepareStatement(
                "UPDATE usage_stats SET total_prompts = total_prompts + 1 WHERE user_id = ?"
            );
            stm.setString(1, userId);
            int rows = stm.executeUpdate();
            return rows > 0;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateLastAccessTimestamp(String userId, long timestamp)
    {
        try {
            PreparedStatement stm = conn.get().prepareStatement(
                "UPDATE usage_stats SET last_access_timestamp = ? WHERE user_id = ?"
            );
            stm.setLong(1, timestamp);
            stm.setString(2, userId);
            int rows = stm.executeUpdate();
            return rows > 0;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


}
