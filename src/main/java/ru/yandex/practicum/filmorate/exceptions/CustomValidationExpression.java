package ru.yandex.practicum.filmorate.exceptions;

public class CustomValidationExpression extends RuntimeException {

    public CustomValidationExpression(String message) {
        super(message);
    }
}
