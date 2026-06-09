package br.com.gerenciamento.controller;

import br.com.gerenciamento.model.Usuario;
import br.com.gerenciamento.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class GlobalLoginController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/login")
    public ModelAndView telaLoginGlobal() {
        ModelAndView mv = new ModelAndView("Login/loginGlobal");
        return mv;
    }

    @PostMapping("/login/buscar-empresa")
    public String buscarEmpresa(@RequestParam("email") String email, RedirectAttributes attributes) {

        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();

            if (usuario.getEmpresa() != null && usuario.getEmpresa().getSlug() != null) {
                String slug = usuario.getEmpresa().getSlug();

                return "redirect:/" + slug + "/login";
            } else {
                attributes.addFlashAttribute("erro", "Esta conta existe, mas o registo do escritório não foi concluído.");
                return "redirect:/login";
            }
        }

        attributes.addFlashAttribute("erro", "Não foi encontrada nenhuma conta com este e-mail.");
        return "redirect:/login";
    }
}