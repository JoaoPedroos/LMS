package br.com.gerenciamento.service;

import br.com.gerenciamento.model.Convite;
import br.com.gerenciamento.model.Empresa;
import br.com.gerenciamento.repository.ConviteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ServiceConvite {

    @Autowired
    private ConviteRepository conviteRepository;

    public String gerarLinkConvite(Empresa empresa) {
        String token = UUID.randomUUID().toString();

        Convite convite = new Convite();
        convite.setToken(token);
        convite.setEmpresa(empresa);
        convite.setExpiraEm(LocalDateTime.now().plusHours(24));

        conviteRepository.save(convite);
        return "/cadastro/convite?token=" + token;
    }

    public Convite validarConvite(String token) throws Exception {
        Convite convite = conviteRepository.findByTokenAndUtilizadoFalse(token)
                .orElseThrow(() -> new Exception("Link de convite inválido ou já utilizado."));

        if (convite.getExpiraEm().isBefore(LocalDateTime.now())) {
            throw new Exception("Este link de convite expirou.");
        }

        return convite;
    }
}