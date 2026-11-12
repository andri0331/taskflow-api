package com.taskflow.controller;

import com.taskflow.entity.Task;
import com.taskflow.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TaskControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @Test
    void createsTaskWithAllowedFields() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Write documentation",
                                  "description": "Document the API",
                                  "priority": "HIGH",
                                  "dueDate": "2026-11-20",
                                  "completed": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Write documentation"))
                .andExpect(jsonPath("$.description").value("Document the API"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.dueDate").value("2026-11-20"))
                .andExpect(jsonPath("$.completed").value(true));
    }

    @Test
    void defaultsCompletedToFalseWhenOmitted() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "New task"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.completed").value(false));
    }

    @ParameterizedTest
    @ValueSource(strings = {"id", "owner", "roles"})
    void rejectsUnapprovedFieldsWithoutChangingExistingTasks(String field) throws Exception {
        Task existing = new Task();
        existing.setTitle("Original task");
        existing = taskRepository.saveAndFlush(existing);
        Long existingId = existing.getId();
        long originalCount = taskRepository.count();

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Overwritten task", "%s": %d}
                                """.formatted(field, existingId)))
                .andExpect(status().isBadRequest());

        assertThat(taskRepository.count()).isEqualTo(originalCount);
        assertThat(taskRepository.findById(existingId).orElseThrow().getTitle())
                .isEqualTo("Original task");
    }
}
