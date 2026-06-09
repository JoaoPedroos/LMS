package br.com.gerenciamento.service;

import br.com.gerenciamento.model.Usuario;
import br.com.gerenciamento.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Optional;

@Service
public class ServiceUsuario {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public Usuario salvarUsuarioAdmin(Usuario user) throws Exception {
        Optional<Usuario> existente = usuarioRepository.findByEmail(user.getEmail());
        if (existente.isPresent()) {
            throw new Exception("Este e-mail já está em uso.");
        }
        user.setSenha(criptografarSenha(user.getSenha()));
        return usuarioRepository.save(user);
    }

    public Usuario autenticar(String email, String senhaLimpa, String slugEmpresa) throws Exception {
        String senhaCripto = criptografarSenha(senhaLimpa);

        Usuario usuario = usuarioRepository.buscarLoginPorTenant(email, senhaCripto, slugEmpresa).orElse(null);

        if (usuario == null) {
            throw new Exception("E-mail ou senha incorretos para este portal corporativo.");
        }
        return usuario;
    }

    private String criptografarSenha(String senha) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] messageDigest = md.digest(senha.getBytes());
        BigInteger no = new BigInteger(1, messageDigest);
        StringBuilder hashtext = new StringBuilder(no.toString(16));
        while (hashtext.length() < 32) {
            hashtext.insert(0, "0");
        }
        return hashtext.toString();
    }
}