package com.rica.consultasservice.consultas;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Segundo consumidor del MISMO tópico, con su propio grupo: cada grupo recibe todos los
 * mensajes por separado. Cuenta las altas de investigadores, ignorando las repetidas.
 */
@Component
public class EstadisticasListener {

    private static final Logger log = LoggerFactory.getLogger(EstadisticasListener.class);

    private final RegistroEventosProcesados eventosProcesados;
    private final ContadorAltas contador;
    private final ObjectMapper objectMapper;

    @Autowired
    public EstadisticasListener(RegistroEventosProcesados eventosProcesados,
                                ContadorAltas contador,
                                ObjectMapper objectMapper) {
        this.eventosProcesados = eventosProcesados;
        this.contador = contador;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "investigadores.registrados", groupId = "consultas-estadisticas")
    public void alRegistrarseInvestigador(ConsumerRecord<String, String> registro) {
        if (!esAltaValida(registro.value())) {
            log.warn("Estadísticas: mensaje no válido en {}-{}@{}, no se cuenta",
                    registro.topic(), registro.partition(), registro.offset());
            return;
        }

        // Identidad del evento = su posición en el log de Kafka (tópico-partición-offset).
        String idEvento = registro.topic() + "-" + registro.partition() + "-" + registro.offset();
        if (!eventosProcesados.primeraVez(idEvento)) {
            log.info("Estadísticas: evento {} ya procesado, se ignora", idEvento);
            return;
        }

        contador.contarAlta();
        log.info("Estadísticas: alta contada ({}). Total: {}", idEvento, contador.total());
    }

    private boolean esAltaValida(String mensaje) {
        if (mensaje == null || mensaje.isBlank()) {
            return false;
        }

        try {
            JsonNode evento = objectMapper.readTree(mensaje);
            JsonNode correo = evento == null ? null : evento.get("correoInstitucional");
            return correo != null && correo.isTextual() && !correo.asText().isBlank();
        } catch (JsonProcessingException e) {
            return false;
        }
    }

}