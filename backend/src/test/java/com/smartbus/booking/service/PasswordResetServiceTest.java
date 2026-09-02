package com.smartbus.booking.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.smartbus.booking.entity.User;
import com.smartbus.booking.repository.UserRepository;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.util.Optional;
import java.util.Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock private JavaMailSender mailSender;
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;

    private PasswordResetService passwordResetService;

    @BeforeEach
    void setUp() {
        passwordResetService = new PasswordResetService(mailSender, userRepository, passwordEncoder);
    }

    @Test
    void sendsCodeAndResetsPasswordForRegisteredEmail() {
        User user = User.builder()
                .id(7L)
                .email("customer@example.com")
                .phone("0900000000")
                .fullName("Khách hàng")
                .password("old-password")
                .build();
        when(userRepository.findByEmailIgnoreCase("customer@example.com")).thenReturn(Optional.of(user));
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(mailSender.createMimeMessage())
                .thenReturn(new MimeMessage(Session.getInstance(new Properties())));
        when(passwordEncoder.encode(anyString()))
                .thenAnswer(invocation -> "encoded:" + invocation.getArgument(0, String.class));
        when(passwordEncoder.matches(anyString(), anyString()))
                .thenAnswer(invocation -> ("encoded:" + invocation.getArgument(0, String.class))
                        .equals(invocation.getArgument(1, String.class)));

        passwordResetService.sendCode(" CUSTOMER@example.com ");

        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
        verify(passwordEncoder).encode(codeCaptor.capture());
        String code = codeCaptor.getValue();

        assertDoesNotThrow(() -> passwordResetService.resetPassword(
                "customer@example.com", code, "new-password"));
        verify(userRepository).save(user);
    }

    @Test
    void unknownEmailReturnsNormallyWithoutSendingMail() {
        when(userRepository.findByEmailIgnoreCase("missing@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString()))
                .thenAnswer(invocation -> "encoded:" + invocation.getArgument(0, String.class));

        assertDoesNotThrow(() -> passwordResetService.sendCode("missing@example.com"));

        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void rejectsInvalidEmailAndWeakPassword() {
        assertThrows(IllegalArgumentException.class,
                () -> passwordResetService.sendCode("not-an-email"));
        assertThrows(IllegalArgumentException.class,
                () -> passwordResetService.resetPassword("customer@example.com", "123456", "123"));
    }
}
