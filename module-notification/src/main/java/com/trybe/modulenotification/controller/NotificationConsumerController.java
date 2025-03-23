package com.trybe.modulenotification.controller;

import com.trybe.modulenotification.service.EmitterService;
import org.springframework.http.MediaType;
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

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestParam(name = "uuid") String uuid) {
        return emitterService.addEmitter(uuid);
    }
}