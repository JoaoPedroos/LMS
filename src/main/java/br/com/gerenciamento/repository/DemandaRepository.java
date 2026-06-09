package br.com.gerenciamento.repository;

import br.com.gerenciamento.model.Demanda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DemandaRepository extends JpaRepository<Demanda, Long> {

    List<Demanda> findByEmpresaSlugAndAtribuidoParaId(String slug, Integer usuarioId);
}