package es.um.sisdist.backend.dao.logs;

import java.util.List;

import es.um.sisdist.backend.dao.models.LogDTO;


public interface ILogsDAO
{
    void saveLog(String userId, String dialogueId, String dialogueJson, long timestamp);
    List<LogDTO> getLogsForUser(String userId);
    boolean deleteLog(String userId, String dialogueId);
}
