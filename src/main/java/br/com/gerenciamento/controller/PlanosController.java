package br.com.gerenciamento.controller;

import br.com.gerenciamento.model.Plano;
import br.com.gerenciamento.model.Usuario;
import br.com.gerenciamento.repository.PlanoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/planos")
public class PlanosController {

    @Autowired
    private PlanoRepository planoRepository;

    @GetMapping("/selecionar")
    public ModelAndView exibirPlanos(HttpSession session) {
        ModelAndView modelAndView = new ModelAndView();

        Usuario adminTemp = (Usuario) session.getAttribute("adminTemp");

        if (adminTemp == null) {
            return new ModelAndView("redirect:/cadastro");
        }

        modelAndView.setViewName("onboarding/planos");
        modelAndView.addObject("admin", adminTemp);

        List<Plano> planosList = planoRepository.findAll();
        modelAndView.addObject("planosList", planosList);

        return modelAndView;
    }

    @PostMapping("/escolher")
    public String escolherPlano(@RequestParam("idPlano") Long idPlano, HttpSession session) {
        Usuario adminTemp = (Usuario) session.getAttribute("adminTemp");

        if (adminTemp == null) {
            return "redirect:/cadastro";
        }

        Plano planoEscolhido = planoRepository.findById(idPlano).orElse(null);

        if (planoEscolhido != null) {
            session.setAttribute("idPlanoEscolhido", planoEscolhido.getId());
            return "redirect:/empresa/cadastro";
        } else {

            return "redirect:/planos/selecionar";
        }
    }
}