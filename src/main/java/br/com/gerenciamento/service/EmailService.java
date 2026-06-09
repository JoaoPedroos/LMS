package br.com.gerenciamento.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.util.Random;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public String gerarCodigo2FA() {
        Random random = new Random();
        int codigo = 100000 + random.nextInt(900000);
        return String.valueOf(codigo);
    }

    public void enviarEmail2FA(String emailDestino, String codigo) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("contix362@gmail.com");
        message.setTo(emailDestino);
        message.setSubject("Código de Verificação - Segundo Fator de Autenticação");
        message.setText("Olá,\n\nSeu código de segurança para acessar o sistema é: " + codigo + "\n\nSe não foi você quem solicitou este código, por favor ignore este e-mail.");

        mailSender.send(message);
    }

}