package com.corunet.server;

import java.io.IOException;

import com.corunet.service.GreeterGrpc.GreeterImplBase;
import com.corunet.service.Service.HelloReply;
import com.corunet.service.Service.HelloRequest;

import io.grpc.Server;
import io.grpc.ServerBuilder;

public class GrpcServer {

	private Server server;

	static int port = 50051;

	private void start() throws IOException {
		/* The port on which the server should run */
		server = ServerBuilder.forPort(port).addService(new GreeterImpl()).build().start();
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				// Use stderr here since the logger may have been reset by its JVM shutdown
				// hook.
				System.err.println("*** shutting down gRPC server since JVM is shutting down");
				GrpcServer.this.stop();
				System.err.println("*** server shut down");
			}
		});
	}

	private void stop() {
		if (server != null) {
			server.shutdown();
		}
	}

	/**
	 * Await termination on the main thread since the grpc library uses daemon
	 * threads.
	 */
	private void blockUntilShutdown() throws InterruptedException {
		if (server != null) {
			server.awaitTermination();
		}
	}

	/**
	 * Main launches the server from the command line.
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		final GrpcServer server = new GrpcServer();
		server.start();
		System.out.println("Server started, listening on " + port);
		server.blockUntilShutdown();
	}

	public static class GreeterImpl extends GreeterImplBase {
		@Override
		public void sayHello(HelloRequest request, io.grpc.stub.StreamObserver<HelloReply> responseObserver) {
			System.out.println("Name in received message: " + request.getName());
			HelloReply reply = HelloReply.newBuilder().setMessage("Response").build();
			responseObserver.onNext(reply);
			responseObserver.onCompleted();
		}
	}
}