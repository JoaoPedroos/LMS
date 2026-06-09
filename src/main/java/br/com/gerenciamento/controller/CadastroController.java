package br.com.gerenciamento.controller;

import br.com.gerenciamento.model.Usuario;
import br.com.gerenciamento.model.TipoUsuario;
import br.com.gerenciamento.repository.UsuarioRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@Controller
@RequestMapping("/cadastro")
public class CadastroController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping
    public ModelAndView exibirFormularioCadastro() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("cadastro/cadastro");
        modelAndView.addObject("usuario", new Usuario());
        return modelAndView;
    }

    @PostMapping("/processar")
    public ModelAndView processarCadastroInicial(@Valid Usuario usuario, HttpSession session) {
        ModelAndView modelAndView = new ModelAndView();

        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            modelAndView.setViewName("cadastro/cadastro");
            modelAndView.addObject("erro", "Este e-mail já está cadastrado no sistema. Faça login ou recupere sua senha.");
            return modelAndView;
        }

        if (usuarioRepository.existsByTelefone(usuario.getTelefone())) {
            modelAndView.setViewName("cadastro/cadastro");
            modelAndView.addObject("erro", "Este número de telefone já está sendo utilizado em outra conta.");
            return modelAndView;
        }

        TipoUsuario tipoAdmin = new TipoUsuario();

        tipoAdmin.setId(1L);

        usuario.setTipoUsuario(tipoAdmin);

        String senhaPura = usuario.getSenha();
        String senhaCriptografada = BCrypt.hashpw(senhaPura, BCrypt.gensalt());
        usuario.setSenha(senhaCriptografada);

        session.setAttribute("adminTemp", usuario);

        modelAndView.setViewName("redirect:/planos/selecionar");
        return modelAndView;
    }
}