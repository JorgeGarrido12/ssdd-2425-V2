package proyecto.backend.rest.externo.service;



import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import es.um.sisdist.backend.grpc.GrpcServiceGrpc;
import es.um.sisdist.backend.grpc.PromptRequest;
import es.um.sisdist.backend.grpc.PromptResponse;

public class GrpcDialogueService {

    private final ManagedChannel channel;
    private final GrpcServiceGrpc.GrpcServiceBlockingStub grpcStub;

    public GrpcDialogueService() {
        this.channel = ManagedChannelBuilder.forAddress("proyecto-backend-grpc", 50051) 
                                            .usePlaintext()
                                            .build();
        this.grpcStub = GrpcServiceGrpc.newBlockingStub(channel);
    }

    public boolean sendPrompt(String userId, String dialogueId, String prompt, String timestamp) {
        PromptRequest request = PromptRequest.newBuilder()
            .setUserId(userId)
            .setDialogueId(dialogueId)
            .setPrompt(prompt)
            .setTimestamp(timestamp)
            .build();

        PromptResponse response = grpcStub.askPrompt(request);

        return response.getSuccess();
    }

    public void shutdown() {
        channel.shutdown();
    }
}
