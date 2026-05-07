package com.project.app.controller;

import com.project.app.dto.LossRequestDTO;
import com.project.app.dto.LossResponseDTO;
import com.project.app.service.LossService;
import com.project.app.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/losses")
@RequiredArgsConstructor
public class LossController {

    private final LossService lossService;

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<LossResponseDTO>>> getAllLosses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<LossResponseDTO> losses = lossService.getAllLosses(page, size);
        return ResponseEntity.ok(ApiResponse.success(losses, "Losses fetched successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<LossResponseDTO>> getLossById(@PathVariable Long id) {
        LossResponseDTO loss = lossService.getLossById(id);
        return ResponseEntity.ok(ApiResponse.success(loss, "Loss fetched successfully"));
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<LossResponseDTO>> createLoss(@Valid @RequestBody LossRequestDTO request) {
        LossResponseDTO createdLoss = lossService.createLoss(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(createdLoss, "Loss created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<LossResponseDTO>> updateLoss(
            @PathVariable Long id, @Valid @RequestBody LossRequestDTO request) {
        LossResponseDTO updatedLoss = lossService.updateLoss(id, request);
        return ResponseEntity.ok(ApiResponse.success(updatedLoss, "Loss updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteLoss(@PathVariable Long id) {
        lossService.deleteLoss(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Loss deleted successfully"));
    }
}
