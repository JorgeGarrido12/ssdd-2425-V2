/**
 *
 */
package es.um.sisdist.backend.dao;

import java.util.function.Supplier;

import es.um.sisdist.backend.dao.logs.ILogsDAO;
import es.um.sisdist.backend.dao.logs.SQLLogsDAO;
import es.um.sisdist.backend.dao.user.IUserDAO;
import es.um.sisdist.backend.dao.user.MongoUserDAO;
import es.um.sisdist.backend.dao.user.SQLUserDAO;

/**
 * @author dsevilla
 *
 */
public class DAOFactoryImpl implements IDAOFactory
{


    @Override
    public IUserDAO createSQLUserDAO()
    {
        return new SQLUserDAO();
    }

    @Override
    public IUserDAO createMongoUserDAO()
    {
        return null;
    }

    public ILogsDAO createSQLLogsDAO()
    {
        return new SQLLogsDAO(); // igual que haces con SQLUserDAO
    }

}
