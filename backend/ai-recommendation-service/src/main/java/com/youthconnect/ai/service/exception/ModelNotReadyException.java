package com.youthconnect.ai.service.exception;

/**
 * Exception thrown when ML model is not ready or loaded
 *
 * @author Douglas Kings Kato
 */
public class ModelNotReadyException extends AIRecommendationException {

    public ModelNotReadyException() {
        super("MODEL_NOT_READY", "ML model is not ready or properly loaded");
    }

    public ModelNotReadyException(String message) {
        super("MODEL_NOT_READY", message);
    }
}