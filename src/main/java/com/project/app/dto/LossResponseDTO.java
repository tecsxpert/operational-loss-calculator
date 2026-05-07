package com.project.app.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class LossResponseDTO {
    private Long id;
    private String title;
    private String description;
    private BigDecimal amount;
    private LocalDate incidentDate;
    private String status;
    private String aiAnalysisResult;
    private String reportedByUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
