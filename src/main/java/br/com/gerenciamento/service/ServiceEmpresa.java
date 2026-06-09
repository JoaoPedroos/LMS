package br.com.gerenciamento.service;

import br.com.gerenciamento.model.Empresa;
import br.com.gerenciamento.model.Plano;
import br.com.gerenciamento.repository.EmpresaRepository;
import br.com.gerenciamento.repository.PlanoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class ServiceEmpresa {

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private PlanoRepository planoRepository;

    public Empresa criarNovaEmpresa(String nome, String planoStr) throws Exception {
        String slug = gerarSlug(nome);

        if (empresaRepository.existsBySlug(slug)) {
            throw new Exception("Uma empresa com uma URL semelhante já está cadastrada.");
        }

        Empresa empresa = new Empresa();
        empresa.setNome(nome);
        empresa.setSlug(slug);

        Plano plano = planoRepository.findByNome(planoStr.toUpperCase())
                .orElseThrow(() -> new Exception("Plano não encontrado no sistema."));

        empresa.setPlano(plano);

        return empresaRepository.save(empresa);
    }

    private String gerarSlug(String input) {
        String nfdNormalizedString = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String noAccent = pattern.matcher(nfdNormalizedString).replaceAll("");
        return noAccent.toLowerCase(Locale.ENGLISH)
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-");
    }
}