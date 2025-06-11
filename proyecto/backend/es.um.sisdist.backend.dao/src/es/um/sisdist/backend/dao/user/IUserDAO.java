package es.um.sisdist.backend.dao.user;

import java.util.Optional;

import es.um.sisdist.backend.dao.models.User;

public interface IUserDAO
{
    public Optional<User> getUserById(String id);

    public Optional<User> getUserByEmail(String id);

    void createUser(User user);

    void updateVisits(String id, int visits);

    void updateToken(String id, String token);

    void deleteUser(String id); // opcional
}
