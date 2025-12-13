package ru.yandex.practicum.filmorate.error;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class ValidationErrorResponse {

    private String error;
    private List<String> details;

    @JsonCreator
    public ValidationErrorResponse(@JsonProperty("error") String error,
                                   @JsonProperty("details") List<String> details) {
        this.error = error;
        this.details = details;
    }
}
