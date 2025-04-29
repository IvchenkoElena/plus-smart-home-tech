package ru.yandex.practicum.exception;

public class NoCartException extends RuntimeException {
    public NoCartException(String message) {
        super(message);
    }
}
