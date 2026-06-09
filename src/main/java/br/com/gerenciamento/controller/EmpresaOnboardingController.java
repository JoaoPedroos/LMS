package br.com.gerenciamento.controller;

import br.com.gerenciamento.model.Empresa;
import br.com.gerenciamento.model.Plano;
import br.com.gerenciamento.model.TipoUsuario;
import br.com.gerenciamento.model.Usuario;
import br.com.gerenciamento.repository.PlanoRepository;
import br.com.gerenciamento.repository.TipoUsuarioRepository;
import br.com.gerenciamento.repository.UsuarioRepository;
import br.com.gerenciamento.service.ServiceEmpresa;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/empresa")
public class EmpresaOnboardingController {

    @Autowired
    private ServiceEmpresa serviceEmpresa;

    @Autowired
    private PlanoRepository planoRepository;

    @Autowired
    private TipoUsuarioRepository tipoUsuarioRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/cadastro")
    public ModelAndView exibirFormularioEmpresa(HttpSession session) {
        Usuario adminTemp = (Usuario) session.getAttribute("adminTemp");
        Long idPlanoEscolhido = (Long) session.getAttribute("idPlanoEscolhido");

        if (adminTemp == null || idPlanoEscolhido == null) {
            return new ModelAndView("redirect:/cadastro");
        }

        ModelAndView modelAndView = new ModelAndView("onboarding/empresa");
        modelAndView.addObject("admin", adminTemp);
        return modelAndView;
    }

    @PostMapping("/salvar")
    public ModelAndView salvarEmpresa(@RequestParam("nomeEmpresa") String nomeEmpresa, HttpSession session) {
        ModelAndView modelAndView = new ModelAndView();

        Usuario adminTemp = (Usuario) session.getAttribute("adminTemp");

        Long idPlanoEscolhido = (Long) session.getAttribute("idPlanoEscolhido");

        if (adminTemp == null || idPlanoEscolhido == null) {
            modelAndView.setViewName("redirect:/cadastro");
            return modelAndView;
        }

        try {
            Plano plano = planoRepository.findById(idPlanoEscolhido)
                    .orElseThrow(() -> new Exception("Plano não encontrado."));

            Empresa novaEmpresa = serviceEmpresa.criarNovaEmpresa(nomeEmpresa, plano.getNome());

            TipoUsuario tipoAdmin = tipoUsuarioRepository.findByNome("ADMIN")
                    .orElseThrow(() -> new Exception("Tipo de usuário ADMIN não configurado."));

            adminTemp.setEmpresa(novaEmpresa);
            adminTemp.setTipoUsuario(tipoAdmin);
            usuarioRepository.save(adminTemp);

            session.removeAttribute("adminTemp");
            session.removeAttribute("idPlanoEscolhido");

            modelAndView.setViewName("redirect:/" + novaEmpresa.getSlug() + "/login");
            return modelAndView;

        } catch (Exception e) {
            modelAndView.setViewName("onboarding/empresa");
            modelAndView.addObject("admin", adminTemp);
            modelAndView.addObject("erro", e.getMessage());
            return modelAndView;
        }
    }
}