# Evidencia de los pasos 6 y 7

Prueba ejecutada en `localhost` el 2026-11-04. Los registros de prueba se retiraron al terminar y `consultas-service` quedó reiniciado y saludable.

## 1. `/actuator/health`

Los tres endpoints devolvieron HTTP 200 y `"status":"UP"`:

- `investigadores-service` (`:8080`): `db`, `discoveryComposite`, `diskSpace`, `livenessState`, `ping`, `readinessState`, `refreshScope` y `ssl` en `UP`.
- `publicaciones-service` (`:8081`): `discoveryComposite`, `diskSpace`, `livenessState`, `mongo`, `ping`, `readinessState`, `refreshScope` y `ssl` en `UP`.
- `consultas-service` (`:8083`): `discoveryComposite`, `diskSpace`, `livenessState`, `mongo`, `ping`, `readinessState`, `refreshScope` y `ssl` en `UP`.

## 2. Paso 6: Saga de eliminación, correlación y brecha

Se registró un investigador temporal (`id=21`, correo `saga.prueba.fcf6f3d36aff41c79b57720b2d270e29@uptc.edu.co`) con `X-Correlation-Id: paso6-saga-bd24b2d9072a40fc84c8f6a1933ac200`; POST respondió 201 y DELETE respondió 202. La ficha de consultas pasó a `ACTIVO` y, al terminar la Saga, tanto el investigador como su ficha devolvieron 404. La Saga recorrió el caso normal (sin publicaciones asociadas).

Líneas de código:

```text
[publicaciones-service,paso6-saga-bd24b2d9072a40fc84c8f6a1933ac200] Saga: 0 publicaciones de saga.prueba.fcf6f3d36aff41c79b57720b2d270e29@uptc.edu.co marcadas como huérfanas
[investigadores-service,paso6-saga-bd24b2d9072a40fc84c8f6a1933ac200] Saga: publicaciones confirmó para saga.prueba.fcf6f3d36aff41c79b57720b2d270e29@uptc.edu.co. Eliminando investigador.
```

Al reproyectar los tres investigadores del paso 7:

```text
INFO [consultas-service,] Proyección: ficha de prueba.paso7.89b5bd7ac36d.1@uptc.edu.co actualizada (registro)
INFO [consultas-service,] Proyección: prueba.paso7.89b5bd7ac36d.1@uptc.edu.co marcado EN_ELIMINACION
INFO [consultas-service,] Proyección: ficha de prueba.paso7.89b5bd7ac36d.1@uptc.edu.co eliminada
```

El campo MDC aparece vacío (`[consultas-service,]`): `ProyectorFichas` no copia el `correlationId` del evento al MDC.

## 3. Paso 7: métricas HTTP y lag de Kafka

Se generaron cinco GET a `GET /api/investigadores` (HTTP 200) y un GET a `/api/investigadores/999999999` (HTTP 404). Antes de detener `consultas-service`, `GET /actuator/metrics/http.server.requests` en `investigadores-service` fue:

```json
{
  "name": "http.server.requests",
  "baseUnit": "seconds",
  "measurements": [
    { "statistic": "COUNT", "value": 44.0 },
    { "statistic": "TOTAL_TIME", "value": 0.6963284 },
    { "statistic": "MAX", "value": 0.0187287 }
  ]
}
```

La respuesta incluyó tags disponibles para `method` (`GET`, `POST`, `DELETE`), `status`, `outcome` y `uri`. Para `?tag=status:404` se observó `COUNT=6.0`, `TOTAL_TIME=0.0379947` y `MAX=0.0111261` segundos.

El lag del grupo `consultas-service-v2` en `investigadores.registrados` fue `0` (offset 21/21) antes de detener el consumidor, subió a `3` (offset 21/24) tras crear tres investigadores temporales y volvió a `0` (24/24) después del reinicio. Los otros tópicos listados permanecieron sin lag. Los tres investigadores (`id=22, 23, 24`) se registraron con HTTP 201; tras verificarse el catch-up, se eliminaron por Saga y se comprobó que ya no existían ni en el servicio de escritura ni en la proyección.

Al final, `http.server.requests` en `investigadores-service` reportó `COUNT=58.0`, `TOTAL_TIME=0.8115164` y `MAX=0.0148216` segundos; el filtro `status:404` reportó `COUNT=11.0`, `TOTAL_TIME=0.0800762` y `MAX=0.0146753`. Esos contadores son acumulados desde el inicio del servicio, no solo del paso 7. Salud final: los tres servicios en `UP`; lag final `0` en los grupos consultados.

# Notas sobre la Saga de eliminación

1. Si `publicaciones-service` falla permanentemente antes de publicar la confirmación, el investigador queda en `EN_ELIMINACION`: el servicio solo lo borra al consumir `publicaciones.marcadas-huerfanas`, así que no volvería automáticamente a `ACTIVO`. Hoy se registran errores del consumidor y hay métricas genéricas, pero no una alerta específica para este caso; el equipo de operación tendría que detectarlo revisando logs o monitoreo.

2. `publicaciones-service` podría publicar `publicaciones.marcado-fallido` al agotar sus reintentos, y `investigadores-service` consumiría ese evento y devolvería el investigador a `ACTIVO`. Sería idempotente si el manejador solo cambia investigadores que aún están en `EN_ELIMINACION` (idealmente validando también el identificador de la Saga), de modo que eventos duplicados o tardíos no alteren otro estado.

3. Definiría la métrica `sagas_eliminacion_atascadas` como el número de investigadores que permanecen en `EN_ELIMINACION` más de 10 minutos, junto con una alerta cuando el valor sea mayor que cero durante unos minutos. El equipo de operación recibiría la alerta con el identificador/correo y el `correlationId` para investigar los logs de ambos servicios y Kafka.

4. Si se agregaran cuatro pasos más, convendría orquestarla con un coordinador explícito que registre el paso actual, los reintentos y las compensaciones. Con dos pasos la coreografía es simple, pero con seis el flujo implícito entre eventos se vuelve más difícil de seguir, depurar y modificar sin acoplar los consumidores entre sí.
