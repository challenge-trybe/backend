package com.trybe.modulenotification.controller;

import com.trybe.modulenotification.service.EmitterService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/sse-connection")
public class NotificationConsumerController {
    private final EmitterService emitterService;

    public NotificationConsumerController(EmitterService emitterService) {
        this.emitterService = emitterService;
    }

    @GetMapping(produces = "text/event-stream")
    public SseEmitter stream(@RequestParam(name = "uuid") String uuid) {
        return emitterService.addEmitter(uuid);
    }
}