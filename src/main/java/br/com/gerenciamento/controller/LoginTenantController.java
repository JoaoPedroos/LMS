package br.com.gerenciamento.controller;

import br.com.gerenciamento.model.Usuario;
import br.com.gerenciamento.repository.UsuarioRepository;
import br.com.gerenciamento.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import org.springframework.ui.Model;

@Controller
@RequestMapping("/{slugEmpresa}")
public class LoginTenantController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmailService emailService;

    @GetMapping("/login")
    public String telaLogin(@PathVariable String slugEmpresa, Model model) {
        model.addAttribute("usuario", new Usuario());
        return "Login/loginTenant";
    }

    @PostMapping("/login")
    public String processarLogin(@RequestParam String email,
                                 @RequestParam String senha,
                                 HttpSession session,
                                 @PathVariable String slugEmpresa) {

        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);

        if (usuario != null && usuario.getEmpresa() != null && usuario.getEmpresa().getSlug().equals(slugEmpresa)) {

            boolean senhaValida = false;
            try {
                senhaValida = org.mindrot.jbcrypt.BCrypt.checkpw(senha, usuario.getSenha());
            } catch (Exception e) {
                senhaValida = senha.equals(usuario.getSenha());
            }

            if (senhaValida) {
                String codigo2FA = emailService.gerarCodigo2FA();

                session.setAttribute("usuarioPendente2FA", usuario);
                session.setAttribute("codigo2FA", codigo2FA);

                emailService.enviarEmail2FA(usuario.getEmail(), codigo2FA);

                return "redirect:/" + slugEmpresa + "/verificar-2fa";
            }
        }
        return "redirect:/" + slugEmpresa + "/login?erro=true";
    }

    @GetMapping("/verificar-2fa")
    public String telaVerificar2FA(@PathVariable String slugEmpresa) {
        return "Login/verificacao-2fa";
    }

    @PostMapping("/verificar-2fa")
    public String validar2FA(@RequestParam String codigo, HttpSession session, @PathVariable String slugEmpresa) {
        String codigoSessao = (String) session.getAttribute("codigo2FA");

        if (codigoSessao != null && codigoSessao.equals(codigo)) {
            Usuario usuario = (Usuario) session.getAttribute("usuarioPendente2FA");

            session.setAttribute("usuarioLogado", usuario);
            session.removeAttribute("codigo2FA");
            session.removeAttribute("usuarioPendente2FA");

            return "redirect:/" + slugEmpresa + "/index";
        }
        return "redirect:/" + slugEmpresa + "/verificar-2fa?erro=true";
    }
}