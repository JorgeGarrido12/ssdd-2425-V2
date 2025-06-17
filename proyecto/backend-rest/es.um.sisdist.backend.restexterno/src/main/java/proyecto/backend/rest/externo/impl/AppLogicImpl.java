/**
 *
 */
package proyecto.backend.rest.externo.impl;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import es.um.sisdist.backend.grpc.PromptRequest;
import es.um.sisdist.backend.grpc.PromptResponse;

import es.um.sisdist.backend.grpc.GrpcServiceGrpc;
import es.um.sisdist.backend.grpc.PingRequest;
import es.um.sisdist.backend.dao.DAOFactoryImpl;
import es.um.sisdist.backend.dao.IDAOFactory;
import es.um.sisdist.backend.dao.logs.ILogsDAO;
import es.um.sisdist.backend.dao.models.Dialogue;
import es.um.sisdist.backend.dao.models.DialogueEstados;
import es.um.sisdist.backend.dao.models.LogDTO;
import es.um.sisdist.backend.dao.models.Prompt;
import es.um.sisdist.backend.dao.models.UsageStats;
import es.um.sisdist.backend.dao.models.User;
import es.um.sisdist.backend.dao.models.utils.UserUtils;
import es.um.sisdist.backend.dao.user.IUserDAO;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

/**
 * @author dsevilla
 *
 */
public class AppLogicImpl {
    IDAOFactory daoFactory;
    IUserDAO dao;
    ILogsDAO logsDAO;


    private static final Logger logger = Logger.getLogger(AppLogicImpl.class.getName());

    private final ManagedChannel channel;
    private final GrpcServiceGrpc.GrpcServiceBlockingStub blockingStub;
    // private final GrpcServiceGrpc.GrpcServiceStub asyncStub;

    static AppLogicImpl instance = new AppLogicImpl();

    private AppLogicImpl() {
        daoFactory = new DAOFactoryImpl();
        Optional<String> backend = Optional.ofNullable(System.getenv("DB_BACKEND"));

        if (backend.isPresent() && backend.get().equals("mongo")) {
            dao = daoFactory.createMongoUserDAO();
            logsDAO = daoFactory.createSQLLogsDAO(); // Usamos también el logsDAO en mongo (aunque no haya DAO mongo de logs, puedes ponerlo a null si quieres)
        } else {
            dao = daoFactory.createSQLUserDAO();
            logsDAO = daoFactory.createSQLLogsDAO();
        }

        var grpcServerName = Optional.ofNullable(System.getenv("GRPC_SERVER"));
        var grpcServerPort = Optional.ofNullable(System.getenv("GRPC_SERVER_PORT"));

        channel = ManagedChannelBuilder
                .forAddress(grpcServerName.orElse("localhost"), Integer.parseInt(grpcServerPort.orElse("50051")))
                .usePlaintext().build();
        blockingStub = GrpcServiceGrpc.newBlockingStub(channel);
    }


    public static AppLogicImpl getInstance() {
        return instance;
    }

    public Optional<User> getUserByEmail(String userId) {
        Optional<User> u = dao.getUserByEmail(userId);
        return u;
    }

    public Optional<User> getUserById(String userId) {
        return dao.getUserById(userId);
    }

    public boolean ping(int v) {
        logger.info("Issuing ping, value: " + v);

        // Test de grpc, puede hacerse con la BD
        var msg = PingRequest.newBuilder().setV(v).build();
        var response = blockingStub.ping(msg);

        return response.getV() == v;
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

    // El frontend, a través del formulario de login,
    // envía el usuario y pass, que se convierte a un DTO. De ahí
    // obtenemos la consulta a la base de datos, que nos retornará,
    // si procede,
    public Optional<User> checkLogin(String email, String password) {
        Optional<User> userOpt = dao.getUserByEmail(email);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            try {
                String passwordHash = calculateMD5(password);

                if (user.getPassword_hash().equals(passwordHash))
                {
                    dao.addVisits(user.getId());
                    dao.updateLastAccessTimestamp(user.getId(), System.currentTimeMillis());
                    return userOpt;
                } else
                    return Optional.empty();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }

    // Metodos para dialogos y demas

    public UsageStats getUsageStats(String userId) {
        return dao.getUsageStats(userId);
    }

    public List<Dialogue> getAllDialoguesForUser(String userId) {
        List<String> dialogueIds = dao.getDialogueIdsByUserId(userId);
        List<Dialogue> dialogues = new ArrayList<>();

        for (String dialogueId : dialogueIds) {
            Dialogue d = dao.getDialogue(userId, dialogueId);
            if (d != null)
                dialogues.add(d);
        }

        return dialogues;
    }

    public Dialogue getDialogue(String userId, String dialogueId) {
        return dao.getDialogue(userId, dialogueId);
    }

    public boolean createDialogue(String userId, Dialogue dialogue) {
        boolean success = dao.createDialogue(userId, dialogue);

        if (success) {
            dao.incrementTotalConversations(userId);
        }

        return success;
    }


    public boolean addPrompt(String userId, String dialogueId, String nextUrl, Prompt prompt) {
        // Paso 1 → Añadir el prompt en la BD → status pasa a BUSY
        boolean success = dao.addPrompt(userId, dialogueId, nextUrl, prompt);

        if (success) {
            // Actualizar estadísticas
            dao.incrementTotalPrompts(userId);

            // Paso 2 → Llamar a gRPC → sin esperar respuesta
            PromptRequest grpcRequest = PromptRequest.newBuilder()
                .setUserId(userId)
                .setDialogueId(dialogueId)
                .setPrompt(prompt.getPrompt())
                .setTimestamp(Long.toString(prompt.getTimestamp()))
                .build();

            blockingStub.askPrompt(grpcRequest);

            logger.info("Prompt sent to gRPC. Response will be processed asynchronously.");
        } else {
            logger.warning("Failed to add prompt to dialogue.");
        }

        return success;
    }



    public boolean addPromptRespuesta(String userId, String dialogueId, Prompt prompt) {
        return dao.addPromptRespuesta(userId, dialogueId, prompt);
    }

    public boolean updateDialogueEstado(String userId, String dialogueId, DialogueEstados status) {
        return dao.updateDialogueEstado(userId, dialogueId, status);
    }

    public boolean createUser(User user) {
        return dao.createUser(user);
    }




    //LOGS AÑADIDOS
    public List<LogDTO> getLogsForUser(String userId)
    {
        return logsDAO.getLogsForUser(userId);
    }

    public boolean deleteLog(String userId, String dialogueId)
    {
        return logsDAO.deleteLog(userId, dialogueId);
    }


    //YA NO SE USA PORQUE NO SIMULAMOS LA RESPUESTA DEL LLAMACHAT
    /*public String callExternalService(Prompt p) {
        logger.info("Calling external gRPC service with prompt: " + p.getPrompt());

        PromptRequest request = PromptRequest.newBuilder().setPrompt(p.getPrompt()).build();
        PromptResponse response = blockingStub.askPrompt(request);

        logger.info("Received answer from gRPC: " + response.getAnswer());
        return response.getAnswer();
    }*/

}
