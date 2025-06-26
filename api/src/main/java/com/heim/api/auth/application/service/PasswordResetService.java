package com.heim.api.auth.application.service;


import com.heim.api.auth.domain.entity.PasswordReset;
import com.heim.api.auth.infraestructure.repository.PasswordResetRepository;
import com.heim.api.users.domain.entity.User;
import com.heim.api.users.infraestructure.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.method.P;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PasswordResetService {
    private final PasswordResetRepository passwordResetRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;

   @Autowired
    public PasswordResetService(
            PasswordResetRepository passwordResetRepository,
            UserRepository userRepository,
            JavaMailSender javaMailSender
   ){
       this.passwordResetRepository = passwordResetRepository;
       this.userRepository = userRepository;
       this.mailSender = javaMailSender;
   }

   public  void  createPasswordResetToken(String email){
       User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

       String token = UUID.randomUUID().toString();
       LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(30);

       PasswordReset passwordReset = new PasswordReset();
       passwordReset.setToken(token);
       passwordReset.setExpirationTime(expirationTime);
       passwordReset.setUser(user);

       sendResetEmail(user.getEmail(), token);

   }

    private void sendResetEmail(String toEmail, String token) {
        String resetLink = "https://tusitio.com/reset-password?token=" + token;
        String subject = "Recupera tu contraseña";
        String body = "Haz clic en el siguiente enlace para restablecer tu contraseña:\n" + resetLink;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

   public  boolean validateToken(String token){
       return  passwordResetRepository.findByToken(token).filter(t -> !t.isUsed() && t.getExpirationTime().isAfter(LocalDateTime.now())).isPresent();

   }

   public void resetPassword(String token, String newPassword){
       PasswordReset passwordReset = passwordResetRepository.findByToken(token).orElseThrow(()-> new RuntimeException("Token inválido"));

       if (passwordReset.isUsed() || passwordReset.getExpirationTime().isBefore(LocalDateTime.now())){
           throw new RuntimeException("Token expirado o ya usado");
       }

       User user = passwordReset.getUser();
       user.setPassword(new BCryptPasswordEncoder().encode(newPassword));
       userRepository.save(user);
       passwordReset.setUsed(true);
       passwordResetRepository.save(passwordReset);
   }
}
