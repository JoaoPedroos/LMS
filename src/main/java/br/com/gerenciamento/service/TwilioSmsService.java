/* package br.com.gerenciamento.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Random;

@Service
public class TwilioSmsService {

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.phone.number}")
    private String twilioNumber;

    // Inicia o Twilio assim que o Spring Boot sobe
    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
    }

    // Gera um código de 6 dígitos
    public String gerarCodigo2FA() {
        Random random = new Random();
        int codigo = 100000 + random.nextInt(900000);
        return String.valueOf(codigo);
    }

    // Envia o SMS formatando o número para o padrão internacional E.164
    public void enviarSms(String numeroDestino, String codigo2fa) {

        // Limpa a formatação visual (ex: "(00) 90000-0000" vira "00900000000")
        String numeroLimpo = numeroDestino.replaceAll("[^0-9]", "");

        // Garante que o número tem o código do país (+55)
        if (!numeroLimpo.startsWith("55")) {
            numeroLimpo = "+55" + numeroLimpo;
        } else {
            numeroLimpo = "+" + numeroLimpo;
        }

        // Dispara o SMS
        Message.creator(
                new PhoneNumber(numeroLimpo),
                new PhoneNumber(twilioNumber),
                "Portal Administrativo: Seu codigo de verificacao e " + codigo2fa
        ).create();
    }
}
*/
