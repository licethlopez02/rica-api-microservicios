package com.rica.investigadores_service.investigadores.infraestructura.entrada.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/status")
public class StatusController {

    @GetMapping
    public String status() {
        return "OK - Investigadores Service running";
    }
}