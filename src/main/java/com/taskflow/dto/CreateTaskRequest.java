package com.taskflow.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class CreateTaskRequest {

    private String title;

    private String description;

    private String priority;

    private LocalDate dueDate;

    private Boolean completed = false;

    @JsonAnySetter
    public void rejectUnknownField(String name, Object value) {
        throw new IllegalArgumentException("Unknown task field: " + name);
    }
}
