package com.trybe.modulenotification.repository;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class EmitterRepository {
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter save(String emitterId, SseEmitter emitter) {
        emitters.put(emitterId, emitter);
        return emitter;
    }

    public SseEmitter findById(String emitterId) {
        return emitters.get(emitterId);
    }

    public Map<String, SseEmitter> findAll() {
        return new HashMap<>(emitters);
    }

    public void deleteById(String emitterId) {
        emitters.remove(emitterId);
    }
}