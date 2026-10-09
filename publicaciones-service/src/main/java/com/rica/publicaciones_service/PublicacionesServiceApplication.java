package com.rica.publicaciones_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.rica.publicaciones_service.grpc.InvestigadorGrpcGrpc.InvestigadorGrpcBlockingStub;
import org.springframework.grpc.client.ImportGrpcClients;

@SpringBootApplication
@ImportGrpcClients(target = "investigadores", types = InvestigadorGrpcBlockingStub.class)
public class PublicacionesServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PublicacionesServiceApplication.class, args);
	}

}
