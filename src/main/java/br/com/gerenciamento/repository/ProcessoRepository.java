package br.com.gerenciamento.repository;

import br.com.gerenciamento.model.Processo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProcessoRepository extends JpaRepository<Processo, Long> {
    List<Processo> findByEmpresaSlug(String slug);
}