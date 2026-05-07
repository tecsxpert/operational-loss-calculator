package com.project.app.service;

import com.project.app.dto.LossRequestDTO;
import com.project.app.dto.LossResponseDTO;
import com.project.app.entity.Loss;
import com.project.app.entity.User;
import com.project.app.exception.CustomException;
import com.project.app.repository.LossRepository;
import com.project.app.repository.UserRepository;
import com.project.app.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class LossService {

    private final LossRepository lossRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    
    @Value("${app.ai.service-url}")
    private String aiServiceUrl;

    @Cacheable(value = "losses", key = "#page + '-' + #size")
    public Page<LossResponseDTO> getAllLosses(int page, int size) {
        return lossRepository.findByDeletedFalse(PageRequest.of(page, size))
                .map(this::mapToResponseDTO);
    }

    @Cacheable(value = "loss", key = "#id")
    public LossResponseDTO getLossById(Long id) {
        Loss loss = lossRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new CustomException("Loss not found with id: " + id, HttpStatus.NOT_FOUND));
        return mapToResponseDTO(loss);
    }

    @CacheEvict(value = {"losses", "loss"}, allEntries = true)
    public LossResponseDTO createLoss(LossRequestDTO request) {
        User currentUser = getCurrentUser();

        Loss loss = Loss.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .amount(request.getAmount())
                .incidentDate(request.getIncidentDate())
                .status(Constants.STATUS_PENDING)
                .reportedBy(currentUser)
                .build();

        Loss savedLoss = lossRepository.save(loss);
        
        // Trigger AI analysis asynchronously (simplified here, but could use @Async)
        analyzeLossWithAI(savedLoss);
        
        // Send email notification
        sendNotification(currentUser.getEmail(), "Loss Reported", "Your loss report '" + savedLoss.getTitle() + "' has been submitted successfully.");

        return mapToResponseDTO(savedLoss);
    }

    @CacheEvict(value = {"losses", "loss"}, allEntries = true)
    public LossResponseDTO updateLoss(Long id, LossRequestDTO request) {
        Loss loss = lossRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new CustomException("Loss not found with id: " + id, HttpStatus.NOT_FOUND));

        loss.setTitle(request.getTitle());
        loss.setDescription(request.getDescription());
        loss.setAmount(request.getAmount());
        loss.setIncidentDate(request.getIncidentDate());

        return mapToResponseDTO(lossRepository.save(loss));
    }

    @CacheEvict(value = {"losses", "loss"}, allEntries = true)
    public void deleteLoss(Long id) {
        Loss loss = lossRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new CustomException("Loss not found with id: " + id, HttpStatus.NOT_FOUND));
        
        loss.setDeleted(true);
        lossRepository.save(loss);
    }

    private void analyzeLossWithAI(Loss loss) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            // Placeholder for AI Service Call
            // ResponseEntity<String> response = restTemplate.postForEntity(aiServiceUrl, requestPayload, String.class);
            // loss.setAiAnalysisResult(response.getBody());
            
            // Mock response
            loss.setAiAnalysisResult("{\"risk_score\": 7.5, \"category\": \"Fraud\"}");
            loss.setStatus(Constants.STATUS_ANALYZED);
            lossRepository.save(loss);
        } catch (Exception e) {
            log.error("Error analyzing loss with AI: ", e);
        }
    }

    private void sendNotification(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Error sending email: ", e);
        }
    }

    @Scheduled(cron = "0 0 0 * * ?") // Every day at midnight
    public void scheduledLossAnalysis() {
        log.info("Running scheduled task: Checking for pending losses...");
        // Logic to batch process pending losses
    }

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            return userRepository.findByUsername(username)
                    .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));
        }
        throw new CustomException("Unauthorized", HttpStatus.UNAUTHORIZED);
    }

    private LossResponseDTO mapToResponseDTO(Loss loss) {
        return LossResponseDTO.builder()
                .id(loss.getId())
                .title(loss.getTitle())
                .description(loss.getDescription())
                .amount(loss.getAmount())
                .incidentDate(loss.getIncidentDate())
                .status(loss.getStatus())
                .aiAnalysisResult(loss.getAiAnalysisResult())
                .reportedByUsername(loss.getReportedBy().getUsername())
                .createdAt(loss.getCreatedAt())
                .updatedAt(loss.getUpdatedAt())
                .build();
    }
}
