# Notas sobre la Saga de eliminación

1. Si `publicaciones-service` falla permanentemente antes de publicar la confirmación, el investigador queda en `EN_ELIMINACION`: el servicio solo lo borra al consumir `publicaciones.marcadas-huerfanas`, así que no volvería automáticamente a `ACTIVO`. Hoy se registran errores del consumidor y hay métricas genéricas, pero no una alerta específica para este caso; el equipo de operación tendría que detectarlo revisando logs o monitoreo.

2. `publicaciones-service` podría publicar `publicaciones.marcado-fallido` al agotar sus reintentos, y `investigadores-service` consumiría ese evento y devolvería el investigador a `ACTIVO`. Sería idempotente si el manejador solo cambia investigadores que aún están en `EN_ELIMINACION` (idealmente validando también el identificador de la Saga), de modo que eventos duplicados o tardíos no alteren otro estado.

3. Definiría la métrica `sagas_eliminacion_atascadas` como el número de investigadores que permanecen en `EN_ELIMINACION` más de 10 minutos, junto con una alerta cuando el valor sea mayor que cero durante unos minutos. El equipo de operación recibiría la alerta con el identificador/correo y el `correlationId` para investigar los logs de ambos servicios y Kafka.

4. Si se agregaran cuatro pasos más, convendría orquestarla con un coordinador explícito que registre el paso actual, los reintentos y las compensaciones. Con dos pasos la coreografía es simple, pero con seis el flujo implícito entre eventos se vuelve más difícil de seguir, depurar y modificar sin acoplar los consumidores entre sí.
