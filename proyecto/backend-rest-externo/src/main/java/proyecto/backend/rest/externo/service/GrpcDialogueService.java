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
        this.channel = ManagedChannelBuilder.forAddress("backend-grpc", 50051) 
                                            .usePlaintext()
                                            .build();
        this.grpcStub = GrpcServiceGrpc.newBlockingStub(channel);
    }

    public boolean sendPrompt(String userId, String dialogueId, String prompt, String timestamp) {
        ManagedChannel tempChannel = ManagedChannelBuilder.forAddress("backend-grpc", 50051)
                                                .usePlaintext()
                                                .build();
        GrpcServiceGrpc.GrpcServiceBlockingStub tempStub = GrpcServiceGrpc.newBlockingStub(tempChannel);

        PromptRequest request = PromptRequest.newBuilder()
            .setUserId(userId)
            .setDialogueId(dialogueId)
            .setPrompt(prompt)
            .setTimestamp(timestamp)
            .build();

        PromptResponse response = tempStub.askPrompt(request);

        tempChannel.shutdownNow();  // <- así no quedan canales vivos en memoria

        return response.getSuccess();
    }


    public void shutdown() {
        channel.shutdown();
    }
}
