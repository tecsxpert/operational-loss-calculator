package com.project.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.app.config.JwtAuthFilter;
import com.project.app.dto.LossRequestDTO;
import com.project.app.dto.LossResponseDTO;
import com.project.app.service.LossService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LossController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for simple unit testing
public class LossControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LossService lossService;

    @MockBean
    private JwtAuthFilter jwtAuthFilter; // Mocked because of SecurityConfig

    @Autowired
    private ObjectMapper objectMapper;

    private LossResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        responseDTO = LossResponseDTO.builder()
                .id(1L)
                .title("Test Loss")
                .amount(BigDecimal.valueOf(100.0))
                .incidentDate(LocalDate.now())
                .status("PENDING")
                .reportedByUsername("testuser")
                .build();
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetLossById() throws Exception {
        when(lossService.getLossById(1L)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/losses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Test Loss"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testCreateLoss() throws Exception {
        LossRequestDTO request = new LossRequestDTO();
        request.setTitle("New Loss");
        request.setAmount(BigDecimal.valueOf(500.0));
        request.setIncidentDate(LocalDate.now());

        when(lossService.createLoss(any(LossRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/losses/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Test Loss"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testCreateLoss_ValidationError() throws Exception {
        LossRequestDTO request = new LossRequestDTO();
        // Title and Amount are required but missing

        mockMvc.perform(post("/api/losses/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testUpdateLoss() throws Exception {
        LossRequestDTO request = new LossRequestDTO();
        request.setTitle("Updated Title");
        request.setAmount(BigDecimal.valueOf(200.0));
        request.setIncidentDate(LocalDate.now());

        when(lossService.updateLoss(eq(1L), any(LossRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(put("/api/losses/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteLoss() throws Exception {
        mockMvc.perform(delete("/api/losses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
