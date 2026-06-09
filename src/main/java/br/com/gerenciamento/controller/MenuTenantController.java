package br.com.gerenciamento.controller;

import br.com.gerenciamento.model.*;
import br.com.gerenciamento.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/{slugEmpresa}")
public class MenuTenantController {

    @Autowired private ClienteRepository clienteRepository;
    @Autowired private FinanceiroRepository financeiroRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private DemandaRepository demandaRepository;
    @Autowired private EventoRepository eventoRepository;
    @Autowired private ProcessoRepository processoRepository;
    @Autowired private EmpresaRepository empresaRepository;

    private boolean isUsuarioLogado(HttpSession session) {
        return session.getAttribute("usuarioLogado") != null;
    }

    private Usuario getUsuarioAdmin(HttpSession session, String slugEmpresa) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");

        if (usuario == null || usuario.getEmpresa() == null || !usuario.getEmpresa().getSlug().equals(slugEmpresa)) {

            usuario = usuarioRepository.findAll().stream()
                    .filter(u -> u.getEmpresa() != null && u.getEmpresa().getSlug().equals(slugEmpresa))
                    .findFirst()
                    .orElse(new Usuario());

            session.setAttribute("usuarioLogado", usuario);
        }
        return usuario;
    }
    private boolean temPermissao(HttpSession session, String nomePermissao) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null) return false;
        if (usuario.getTipoUsuario() != null && "ADMIN".equals(usuario.getTipoUsuario().getNome())) return true;
        if (usuario.getPermissoes() == null) return false;
        return usuario.getPermissoes().stream().anyMatch(p -> p.getNome().equalsIgnoreCase(nomePermissao));
    }

    @GetMapping("/index")
    public ModelAndView painelIndex(@PathVariable String slugEmpresa, HttpSession session) {

        if (session.getAttribute("usuarioLogado") == null) {
            return new ModelAndView("redirect:/" + slugEmpresa + "/login");
        }

        ModelAndView mv = new ModelAndView("dashboard/index");
        mv.addObject("slugEmpresa", slugEmpresa);
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");

        List<Demanda> minhasDemandas = demandaRepository.findByEmpresaSlugAndAtribuidoParaId(slugEmpresa, usuarioLogado.getId());
        mv.addObject("minhasDemandas", minhasDemandas != null ? minhasDemandas : Collections.emptyList());

        long pendentesCount = minhasDemandas != null ? minhasDemandas.stream().filter(d -> !d.getConcluida()).count() : 0;
        mv.addObject("demandasPendentesCount", pendentesCount);

        if (temPermissao(session, "VISUALIZAR_CLIENTES") || temPermissao(session, "GERENCIAR_CLIENTES")) {
            mv.addObject("totalClientes", clienteRepository.countByEmpresaSlugAndAtivo(slugEmpresa, true));
        } else { mv.addObject("totalClientes", "---"); }

        if (temPermissao(session, "VISUALIZAR_PROCESSOS")) {
            List<Processo> procs = processoRepository.findByEmpresaSlug(slugEmpresa);
            mv.addObject("totalProcessos", procs != null ? procs.size() : 0);
        } else { mv.addObject("totalProcessos", "---"); }

        boolean podeVerFinanceiro = temPermissao(session, "FINANCEIRO");
        mv.addObject("podeVerFinanceiro", podeVerFinanceiro);

        if (podeVerFinanceiro) {
            List<Financeiro> fluxo = financeiroRepository.findByEmpresaSlug(slugEmpresa);
            int mesAtual = LocalDate.now().getMonthValue();
            int anoAtual = LocalDate.now().getYear();

            if (fluxo != null) {
                BigDecimal totalRecebido = fluxo.stream()
                        .filter(f -> f.getTipo() != null && f.getStatus() != null && f.getDataVencimento() != null && f.getValor() != null)
                        .filter(f -> f.getDataVencimento().getMonthValue() == mesAtual && f.getDataVencimento().getYear() == anoAtual)
                        .filter(f -> {
                            String tipo = f.getTipo().toUpperCase();
                            return tipo.contains("REC") || tipo.contains("ENT");
                        })
                        .filter(f -> {
                            String status = f.getStatus().toUpperCase();
                            return status.contains("PAG") || status.contains("CON") || status.contains("REC") || status.contains("QUI");
                        })
                        .map(Financeiro::getValor)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal totalGasto = fluxo.stream()
                        .filter(f -> f.getTipo() != null && f.getStatus() != null && f.getDataVencimento() != null && f.getValor() != null)
                        .filter(f -> f.getDataVencimento().getMonthValue() == mesAtual && f.getDataVencimento().getYear() == anoAtual)
                        .filter(f -> {
                            String tipo = f.getTipo().toUpperCase();
                            return tipo.contains("DES") || tipo.contains("PAG") || tipo.contains("SAI");
                        })
                        .filter(f -> {
                            String status = f.getStatus().toUpperCase();
                            return status.contains("PAG") || status.contains("CON") || status.contains("QUI");
                        })
                        .map(Financeiro::getValor)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                mv.addObject("totalRecebidoMes", totalRecebido);
                mv.addObject("totalGastoMes", totalGasto);
            } else {
                mv.addObject("totalRecebidoMes", BigDecimal.ZERO);
                mv.addObject("totalGastoMes", BigDecimal.ZERO);
            }
        }

        if (temPermissao(session, "VISUALIZAR_AGENDA")) {
            List<Evento> proximosEventos = eventoRepository.findByEmpresaSlug(slugEmpresa);
            mv.addObject("eventos", proximosEventos != null ? proximosEventos : Collections.emptyList());
        }

        return mv;
    }

    @GetMapping("/clientes")
    public ModelAndView listarClientes(@PathVariable String slugEmpresa, HttpSession session) {
        if (!isUsuarioLogado(session)) return new ModelAndView("redirect:/" + slugEmpresa + "/login");

        ModelAndView mv = new ModelAndView("dashboard/clientes");
        List<Cliente> clientes = clienteRepository.findByEmpresaSlug(slugEmpresa);
        mv.addObject("clientes", clientes != null ? clientes : Collections.emptyList());
        mv.addObject("podeGerenciar", temPermissao(session, "GERENCIAR_CLIENTES"));
        mv.addObject("slugEmpresa", slugEmpresa);
        return mv;
    }

    @PostMapping("/clientes/salvar")
    public String salvarCliente(@PathVariable String slugEmpresa, Cliente cliente, HttpSession session) {
        Usuario user = (Usuario) session.getAttribute("usuarioLogado");
        cliente.setEmpresa(user.getEmpresa());
        clienteRepository.save(cliente);
        return "redirect:/" + slugEmpresa + "/clientes";
    }

    @GetMapping("/clientes/editar/{id}")
    public ModelAndView editarCliente(@PathVariable String slugEmpresa, @PathVariable Long id, HttpSession session) {
        if (!temPermissao(session, "GERENCIAR_CLIENTES")) return new ModelAndView("redirect:/" + slugEmpresa + "/clientes");
        ModelAndView mv = new ModelAndView("dashboard/clientes-form");
        mv.addObject("cliente", clienteRepository.findById(id).orElse(new Cliente()));
        mv.addObject("slugEmpresa", slugEmpresa);
        return mv;
    }

    @GetMapping("/clientes/deletar/{id}")
    public String deletarCliente(@PathVariable String slugEmpresa, @PathVariable Long id) {
        clienteRepository.deleteById(id);
        return "redirect:/" + slugEmpresa + "/clientes";
    }


    @GetMapping("/processos")
    public ModelAndView telaProcessos(@PathVariable String slugEmpresa, HttpSession session) {
        if (!isUsuarioLogado(session)) return new ModelAndView("redirect:/" + slugEmpresa + "/login");
        if (!temPermissao(session, "VISUALIZAR_PROCESSOS")) return new ModelAndView("redirect:/" + slugEmpresa + "/index");
        ModelAndView mv = new ModelAndView("dashboard/processos");
        mv.addObject("slugEmpresa", slugEmpresa);
        List<Processo> listaProcessos = processoRepository.findByEmpresaSlug(slugEmpresa);
        mv.addObject("processos", listaProcessos != null ? listaProcessos : Collections.emptyList());
        List<Cliente> listaClientes = clienteRepository.findByEmpresaSlugAndAtivo(slugEmpresa, true);
        mv.addObject("clientes", listaClientes != null ? listaClientes : Collections.emptyList());
        return mv;
    }

    @PostMapping("/processos/salvar")
    public String salvarProcesso(@PathVariable String slugEmpresa, Processo processo, HttpSession session) {
        if (!isUsuarioLogado(session)) return "redirect:/" + slugEmpresa + "/login";
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        processo.setEmpresa(usuarioLogado.getEmpresa());
        processoRepository.save(processo);
        return "redirect:/" + slugEmpresa + "/processos";
    }


    @GetMapping("/demandas")
    public ModelAndView telaDemandas(@PathVariable String slugEmpresa, HttpSession session) {
        if (!isUsuarioLogado(session)) return new ModelAndView("redirect:/" + slugEmpresa + "/login");

        ModelAndView mv = new ModelAndView("dashboard/demandas");
        mv.addObject("slugEmpresa", slugEmpresa);
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");

        boolean masterDemanda = "ADMIN".equals(usuarioLogado.getTipoUsuario().getNome()) || temPermissao(session, "GERENCIAR_DEMANDAS");

        if (masterDemanda) {
            mv.addObject("demandas", demandaRepository.findAll());
            mv.addObject("isDiretoria", true);

            List<Usuario> equipe = usuarioRepository.findAll();
            equipe.removeIf(u -> u.getEmpresa() == null || !u.getEmpresa().getSlug().equals(slugEmpresa));
            mv.addObject("equipe", equipe);

        } else {
            mv.addObject("demandas", demandaRepository.findByEmpresaSlugAndAtribuidoParaId(slugEmpresa, usuarioLogado.getId()));
            mv.addObject("isDiretoria", false);
        }

        return mv;
    }

    @PostMapping("/demandas/salvar")
    public String salvarDemanda(@PathVariable String slugEmpresa, Demanda demanda, HttpSession session) {
        if (!isUsuarioLogado(session)) return "redirect:/" + slugEmpresa + "/login";
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        demanda.setEmpresa(usuarioLogado.getEmpresa());
        demanda.setCriadoPor(usuarioLogado);
        if (!"ADMIN".equals(usuarioLogado.getTipoUsuario().getNome()) && !temPermissao(session, "GERENCIAR_DEMANDAS")) {
            demanda.setAtribuidoPara(usuarioLogado);
        } else if (demanda.getAtribuidoPara() == null) {
            demanda.setAtribuidoPara(usuarioLogado);
        }
        demandaRepository.save(demanda);
        return "redirect:/" + slugEmpresa + "/demandas";
    }

    @PostMapping("/demandas/alternar/{id}")
    @ResponseBody
    public ResponseEntity<?> alternarStatusDemanda(@PathVariable String slugEmpresa, @PathVariable Long id, HttpSession session) {
        if (!isUsuarioLogado(session)) return ResponseEntity.status(401).body("Não autorizado");
        Demanda demanda = demandaRepository.findById(id).orElse(null);
        if (demanda != null) {
            demanda.setConcluida(!demanda.getConcluida());
            demandaRepository.save(demanda);
            return ResponseEntity.ok(demanda.getConcluida());
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/demandas/deletar/{id}")
    public String deletarDemanda(@PathVariable String slugEmpresa, @PathVariable Long id) {
        demandaRepository.deleteById(id);
        return "redirect:/" + slugEmpresa + "/demandas";
    }


    @GetMapping("/financeiro")
    public ModelAndView telaFinanceiro(@PathVariable String slugEmpresa, HttpSession session) {
        if (!isUsuarioLogado(session) || !temPermissao(session, "FINANCEIRO")) return new ModelAndView("redirect:/" + slugEmpresa + "/index");
        ModelAndView mv = new ModelAndView("dashboard/financeiro");
        mv.addObject("slugEmpresa", slugEmpresa);
        List<Financeiro> lancamentos = financeiroRepository.findByEmpresaSlug(slugEmpresa);
        mv.addObject("lancamentos", lancamentos != null ? lancamentos : Collections.emptyList());
        mv.addObject("clientes", clienteRepository.findByEmpresaSlug(slugEmpresa));
        return mv;
    }

    @PostMapping("/financeiro/salvar")
    public String salvarFinanceiro(@PathVariable String slugEmpresa, Financeiro financeiro,
                                   @RequestParam(name="parcelas", defaultValue="1") Integer parcelas, HttpSession session) {
        if (!isUsuarioLogado(session) || !temPermissao(session, "FINANCEIRO")) return "redirect:/" + slugEmpresa + "/login";
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");

        if (parcelas > 1) {
            BigDecimal valorParcela = financeiro.getValor().divide(new BigDecimal(parcelas), 2, RoundingMode.HALF_UP);
            for (int i = 0; i < parcelas; i++) {
                Financeiro parcela = new Financeiro();
                parcela.setDescricao(financeiro.getDescricao() + " (" + (i + 1) + "/" + parcelas + ")");
                parcela.setValor(valorParcela);
                parcela.setTipo(financeiro.getTipo());
                parcela.setStatus(i == 0 ? financeiro.getStatus() : "PENDENTE");
                parcela.setDataVencimento(financeiro.getDataVencimento().plusMonths(i));
                parcela.setEmpresa(usuarioLogado.getEmpresa());
                parcela.setCliente(financeiro.getCliente());
                financeiroRepository.save(parcela);
            }
        } else {
            financeiro.setEmpresa(usuarioLogado.getEmpresa());
            financeiroRepository.save(financeiro);
        }
        return "redirect:/" + slugEmpresa + "/financeiro";
    }

    @GetMapping("/financeiro/status/{id}")
    public String alternarStatusFinanceiro(@PathVariable String slugEmpresa, @PathVariable Long id, HttpSession session) {
        if (!isUsuarioLogado(session) || !temPermissao(session, "FINANCEIRO")) return "redirect:/" + slugEmpresa + "/login";

        Financeiro fin = financeiroRepository.findById(id).orElse(null);
        if (fin != null) {
            if ("PAGO".equalsIgnoreCase(fin.getStatus()) || "CONCLUIDO".equalsIgnoreCase(fin.getStatus())) {
                fin.setStatus("PENDENTE");
            } else {
                fin.setStatus("PAGO");
            }
            financeiroRepository.save(fin);
        }
        return "redirect:/" + slugEmpresa + "/financeiro";
    }

    @GetMapping("/agenda")
    public ModelAndView telaAgenda(@PathVariable String slugEmpresa, HttpSession session) {
        if (!isUsuarioLogado(session)) return new ModelAndView("redirect:/" + slugEmpresa + "/login");
        ModelAndView mv = new ModelAndView("dashboard/agenda");
        mv.addObject("slugEmpresa", slugEmpresa);
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");

        mv.addObject("eventos", eventoRepository.findByEmpresaSlug(slugEmpresa));
        mv.addObject("demandas", demandaRepository.findByEmpresaSlugAndAtribuidoParaId(slugEmpresa, usuarioLogado.getId()));

        if (temPermissao(session, "FINANCEIRO")) {
            mv.addObject("contasPrazo", financeiroRepository.findByEmpresaSlug(slugEmpresa));
        }
        return mv;
    }

    @PostMapping("/agenda/salvar")
    public String salvarEvento(@PathVariable String slugEmpresa, Evento evento, HttpSession session) {
        if (!isUsuarioLogado(session)) return "redirect:/" + slugEmpresa + "/login";
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        evento.setEmpresa(usuarioLogado.getEmpresa());
        evento.setResponsavel(usuarioLogado);
        eventoRepository.save(evento);
        return "redirect:/" + slugEmpresa + "/agenda";
    }

    @PostMapping("/agenda/atualizar-data")
    @ResponseBody
    public String atualizarDataAgenda(@PathVariable String slugEmpresa, @RequestParam("id") String idString, @RequestParam("novaData") String novaDataStr, HttpSession session) {
        if (!isUsuarioLogado(session)) return "Erro: Sessão Expirada";
        try {
            String cleanDate = novaDataStr.contains("T") ? novaDataStr.split("T")[0] : novaDataStr;
            if (idString.startsWith("E_")) {
                Long id = Long.parseLong(idString.replace("E_", ""));
                Evento ev = eventoRepository.findById(id).orElse(null);
                if (ev != null) {
                    ev.setDataHora(java.time.LocalDate.parse(cleanDate).atTime(14, 0));
                    eventoRepository.save(ev);
                }
            } else if (idString.startsWith("F_")) {
                Long id = Long.parseLong(idString.replace("F_", ""));
                Financeiro fin = financeiroRepository.findById(id).orElse(null);
                if (fin != null) {
                    fin.setDataVencimento(java.time.LocalDate.parse(cleanDate));
                    financeiroRepository.save(fin);
                }
            } else if (idString.startsWith("D_")) {
                Long id = Long.parseLong(idString.replace("D_", ""));
                Demanda dem = demandaRepository.findById(id).orElse(null);
                if (dem != null) {
                    dem.setDataFim(java.time.LocalDate.parse(cleanDate).atTime(18, 0));
                    demandaRepository.save(dem);
                }
            }
            return "OK";
        } catch (Exception e) { return "Erro: " + e.getMessage(); }
    }

    @GetMapping("/funcionarios")
    public ModelAndView listarFuncionarios(@PathVariable String slugEmpresa, HttpSession session) {
        if (!isUsuarioLogado(session)) return new ModelAndView("redirect:/" + slugEmpresa + "/login");

        Usuario user = (Usuario) session.getAttribute("usuarioLogado");
        boolean isAdmin = user.getTipoUsuario() != null && "ADMIN".equals(user.getTipoUsuario().getNome());

        if (!isAdmin && !temPermissao(session, "GERENCIAR_FUNCIONARIOS"))
            return new ModelAndView("redirect:/" + slugEmpresa + "/index");

        ModelAndView mv = new ModelAndView("dashboard/funcionarios");
        List<Usuario> equipe = usuarioRepository.findAll();
        equipe.removeIf(u -> u.getEmpresa() == null || !u.getEmpresa().getSlug().equals(slugEmpresa));
        mv.addObject("funcionarios", equipe);
        mv.addObject("slugEmpresa", slugEmpresa);
        return mv;
    }

    @GetMapping("/configuracoes")
    public ModelAndView telaConfiguracoes(@PathVariable String slugEmpresa, HttpSession session) {
        if (!isUsuarioLogado(session) || !temPermissao(session, "GERENCIAR_CONFIGURACOES"))
            return new ModelAndView("redirect:/" + slugEmpresa + "/index");

        ModelAndView mv = new ModelAndView("dashboard/configuracoes");
        mv.addObject("slugEmpresa", slugEmpresa);

        Usuario user = (Usuario) session.getAttribute("usuarioLogado");
        Empresa empresaBanco = empresaRepository.findById(user.getEmpresa().getId()).orElse(user.getEmpresa());
        mv.addObject("empresa", empresaBanco);

        List<Usuario> equipe = usuarioRepository.findAll();
        equipe.removeIf(u -> u.getEmpresa() == null || !u.getEmpresa().getSlug().equals(slugEmpresa));
        mv.addObject("equipe", equipe);

        return mv;
    }

    @PostMapping("/configuracoes/salvar-institucional")
    public String salvarInstitucional(@PathVariable String slugEmpresa,
                                      @RequestParam("nomeFantasia") String nomeFantasia,
                                      @RequestParam("cnpj") String cnpj,
                                      HttpSession session) {
        Usuario user = (Usuario) session.getAttribute("usuarioLogado");
        Empresa emp = empresaRepository.findById(user.getEmpresa().getId()).orElse(null);

        if(emp != null) {
            emp.setNome(nomeFantasia);
            emp.setCnpj(cnpj);
            empresaRepository.save(emp);
        }
        return "redirect:/" + slugEmpresa + "/configuracoes";
    }

    @PostMapping("/configuracoes/operador/salvar")
    public String salvarOperador(@PathVariable String slugEmpresa, Usuario novoUsuario, HttpSession session) {
        Usuario user = (Usuario) session.getAttribute("usuarioLogado");
        novoUsuario.setEmpresa(user.getEmpresa());
        usuarioRepository.save(novoUsuario);
        return "redirect:/" + slugEmpresa + "/configuracoes";
    }

    @GetMapping("/perfil")
    public ModelAndView telaPerfil(@PathVariable String slugEmpresa, HttpSession session) {
        if (!isUsuarioLogado(session)) return new ModelAndView("redirect:/" + slugEmpresa + "/login");

        ModelAndView mv = new ModelAndView("dashboard/perfil");
        mv.addObject("slugEmpresa", slugEmpresa);

        Usuario userSessao = (Usuario) session.getAttribute("usuarioLogado");
        Usuario usuarioBanco = usuarioRepository.findById(userSessao.getId()).orElse(userSessao);

        mv.addObject("usuario", usuarioBanco);
        return mv;
    }

    @PostMapping("/perfil/salvar")
    public String salvarPerfil(@PathVariable String slugEmpresa,
                               @RequestParam("nome") String nome,
                               @RequestParam("email") String email,
                               @RequestParam(value = "senha", required = false) String senha,
                               HttpSession session,
                               RedirectAttributes attributes) {

        if (!isUsuarioLogado(session)) return "redirect:/" + slugEmpresa + "/login";

        Usuario userSessao = (Usuario) session.getAttribute("usuarioLogado");
        Usuario usuarioBanco = usuarioRepository.findById(userSessao.getId()).orElse(null);

        if (usuarioBanco != null) {
            usuarioBanco.setNome(nome);
            usuarioBanco.setEmail(email);

            if (senha != null && !senha.trim().isEmpty()) {
                String senhaCriptografada = org.mindrot.jbcrypt.BCrypt.hashpw(senha, org.mindrot.jbcrypt.BCrypt.gensalt());
                usuarioBanco.setSenha(senhaCriptografada);
            }

            usuarioRepository.save(usuarioBanco);
            session.setAttribute("usuarioLogado", usuarioBanco);
            attributes.addFlashAttribute("sucesso", "Perfil atualizado com sucesso!");
        }

        return "redirect:/" + slugEmpresa + "/perfil";
    }

    @GetMapping("/logout")
    public String realizarLogout(@PathVariable String slugEmpresa, HttpSession session) {
        session.invalidate();
        return "redirect:/" + slugEmpresa + "/login";
    }
}