package com.trybe.modulenotification.service;

import com.trybe.modulenotification.repository.EmitterRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;

@Service
public class EmitterService {
    public final static long DEFAULT_TIMEOUT = 60L * 60L * 1000L;

    private final EmitterRepository emitterRepository;

    public EmitterService(EmitterRepository emitterRepository) {
        this.emitterRepository = emitterRepository;
    }

    public SseEmitter addEmitter(String uuid) {
        SseEmitter emitter = emitterRepository.save(uuid, new SseEmitter(DEFAULT_TIMEOUT));

        emitter.onCompletion(() -> {
            emitterRepository.deleteById(uuid);
        });
        emitter.onTimeout(() -> {
            emitterRepository.deleteById(uuid);
        });

        sendToClient(uuid, emitter, "connected");

        return emitter;
    }

    public SseEmitter getEmitter(String uuid) {
        return emitterRepository.findById(uuid);
    }

    public void sendToClient(String emitterId, SseEmitter emitter, Object data) {
        try {
            emitter.send(SseEmitter.event()
                    .id(emitterId)
                    .data(data)
            );
        } catch (IOException e) {
            emitter.completeWithError(e);
            emitterRepository.deleteById(emitterId);
        }
    }

    @Scheduled(fixedRate = 3 * 60 * 1000)
    public void sendHeartbeat() {
        Map<String, SseEmitter> emitters = emitterRepository.findAll();

        emitters.forEach((uuid, emitter) -> {
            sendToClient(uuid, emitter, "heartbeat");
        });
    }
}