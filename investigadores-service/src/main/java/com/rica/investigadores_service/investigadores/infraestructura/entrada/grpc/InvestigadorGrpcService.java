package com.rica.investigadores_service.investigadores.infraestructura.entrada.grpc;

import com.rica.investigadores_service.grpc.InvestigadorGrpcGrpc;
import com.rica.investigadores_service.grpc.InvestigadorRequest;
import com.rica.investigadores_service.grpc.InvestigadorResponse;
import com.rica.investigadores_service.investigadores.aplicacion.RepositorioInvestigadores;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

@Service
public class InvestigadorGrpcService extends InvestigadorGrpcGrpc.InvestigadorGrpcImplBase {

    private final RepositorioInvestigadores repositorio;

    public InvestigadorGrpcService(RepositorioInvestigadores repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public void existeInvestigador(InvestigadorRequest request,
                                   StreamObserver<InvestigadorResponse> responseObserver) {
        boolean existe = repositorio.existeCorreo(request.getCorreoInstitucional());
        responseObserver.onNext(InvestigadorResponse.newBuilder().setExiste(existe).build());
        responseObserver.onCompleted();
    }
}