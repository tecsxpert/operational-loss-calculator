package com.project.app.service;

import com.project.app.dto.LossRequestDTO;
import com.project.app.dto.LossResponseDTO;
import com.project.app.entity.Loss;
import com.project.app.entity.User;
import com.project.app.repository.LossRepository;
import com.project.app.repository.UserRepository;
import com.project.app.util.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LossServiceTest {

    @Mock
    private LossRepository lossRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private LossService lossService;

    private User testUser;
    private Loss testLoss;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .role(Constants.ROLE_USER)
                .build();

        testLoss = Loss.builder()
                .id(1L)
                .title("Test Loss")
                .description("Test Description")
                .amount(BigDecimal.valueOf(100.0))
                .incidentDate(LocalDate.now())
                .status(Constants.STATUS_PENDING)
                .reportedBy(testUser)
                .deleted(false)
                .build();

        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);

        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getPrincipal()).thenReturn(userDetails);
        lenient().when(userDetails.getUsername()).thenReturn("testuser");
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testGetLossById_Success() {
        when(lossRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(testLoss));

        LossResponseDTO result = lossService.getLossById(1L);

        assertNotNull(result);
        assertEquals("Test Loss", result.getTitle());
        verify(lossRepository, times(1)).findByIdAndDeletedFalse(1L);
    }

    @Test
    void testGetLossById_NotFound() {
        when(lossRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> lossService.getLossById(1L));
        verify(lossRepository, times(1)).findByIdAndDeletedFalse(1L);
    }

    @Test
    void testCreateLoss_Success() {
        LossRequestDTO request = new LossRequestDTO();
        request.setTitle("New Loss");
        request.setDescription("New Desc");
        request.setAmount(BigDecimal.valueOf(500.0));
        request.setIncidentDate(LocalDate.now());

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(lossRepository.save(any(Loss.class))).thenReturn(testLoss);

        LossResponseDTO result = lossService.createLoss(request);

        assertNotNull(result);
        assertEquals("Test Loss", result.getTitle()); // Returns mock object data
        verify(lossRepository, times(2)).save(any(Loss.class)); // Saved once initially, once after AI analysis
    }

    @Test
    void testUpdateLoss_Success() {
        LossRequestDTO request = new LossRequestDTO();
        request.setTitle("Updated Title");
        request.setAmount(BigDecimal.valueOf(200.0));
        request.setIncidentDate(LocalDate.now());

        when(lossRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(testLoss));
        when(lossRepository.save(any(Loss.class))).thenReturn(testLoss);

        LossResponseDTO result = lossService.updateLoss(1L, request);

        assertNotNull(result);
        assertEquals("Updated Title", testLoss.getTitle());
        verify(lossRepository, times(1)).save(testLoss);
    }

    @Test
    void testDeleteLoss_Success() {
        when(lossRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(testLoss));

        lossService.deleteLoss(1L);

        assertTrue(testLoss.isDeleted());
        verify(lossRepository, times(1)).save(testLoss);
    }
}
