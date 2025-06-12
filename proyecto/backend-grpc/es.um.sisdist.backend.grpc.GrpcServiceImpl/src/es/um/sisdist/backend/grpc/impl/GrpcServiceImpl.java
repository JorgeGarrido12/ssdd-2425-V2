package es.um.sisdist.backend.grpc.impl;

import java.util.logging.Logger;

import es.um.sisdist.backend.grpc.GrpcServiceGrpc;
import es.um.sisdist.backend.grpc.PingRequest;
import es.um.sisdist.backend.grpc.PingResponse;
import io.grpc.stub.StreamObserver;
import es.um.sisdist.backend.grpc.PromptRequest;
import es.um.sisdist.backend.grpc.PromptResponse;

class GrpcServiceImpl extends GrpcServiceGrpc.GrpcServiceImplBase {
	private Logger logger;

	public GrpcServiceImpl(Logger logger) {
		super();
		this.logger = logger;
	}

	@Override
	public void ping(PingRequest request, StreamObserver<PingResponse> responseObserver) {
		logger.info("Recived PING request, value = " + request.getV());
		responseObserver.onNext(PingResponse.newBuilder().setV(request.getV()).build());
		responseObserver.onCompleted();
	}

	@Override
	public void askPrompt(PromptRequest request, StreamObserver<PromptResponse> responseObserver) {
		logger.info("Received AskPrompt, prompt = " + request.getPrompt());

		// Añade aquí el retardo para simular tiempo de respuesta
		try {
			Thread.sleep(4000); // 4 segundos de espera
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// Aquí puedes simular una respuesta de momento
		String simulatedAnswer = "Simulated answer to: " + request.getPrompt();

		responseObserver.onNext(PromptResponse.newBuilder().setAnswer(simulatedAnswer).build());
		responseObserver.onCompleted();
	}

	/*
	 * @Override
	 * public void storeImage(ImageData request, StreamObserver<Empty>
	 * responseObserver)
	 * {
	 * logger.info("Add image " + request.getId());
	 * imageMap.put(request.getId(),request);
	 * responseObserver.onNext(Empty.newBuilder().build());
	 * responseObserver.onCompleted();
	 * }
	 * 
	 * @Override
	 * public StreamObserver<ImageData> storeImages(StreamObserver<Empty>
	 * responseObserver)
	 * {
	 * // La respuesta, sólo un objeto Empty
	 * responseObserver.onNext(Empty.newBuilder().build());
	 * 
	 * // Se retorna un objeto que, al ser llamado en onNext() con cada
	 * // elemento enviado por el cliente, reacciona correctamente
	 * return new StreamObserver<ImageData>() {
	 * 
	 * @Override
	 * public void onCompleted() {
	 * // Terminar la respuesta.
	 * responseObserver.onCompleted();
	 * }
	 * 
	 * @Override
	 * public void onError(Throwable arg0) {
	 * }
	 * 
	 * @Override
	 * public void onNext(ImageData imagedata)
	 * {
	 * logger.info("Add image (multiple) " + imagedata.getId());
	 * imageMap.put(imagedata.getId(), imagedata);
	 * }
	 * };
	 * }
	 * 
	 * @Override
	 * public void obtainImage(ImageSpec request, StreamObserver<ImageData>
	 * responseObserver) {
	 * // TODO Auto-generated method stub
	 * super.obtainImage(request, responseObserver);
	 * }
	 * 
	 * @Override
	 * public StreamObserver<ImageSpec> obtainCollage(StreamObserver<ImageData>
	 * responseObserver) {
	 * // TODO Auto-generated method stub
	 * return super.obtainCollage(responseObserver);
	 * }
	 */
}