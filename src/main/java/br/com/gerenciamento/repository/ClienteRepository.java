package br.com.gerenciamento.repository;

import br.com.gerenciamento.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    List<Cliente> findByEmpresaSlug(String slug);
    List<Cliente> findByEmpresaSlugAndAtivo(String slug, Boolean ativo);
    long countByEmpresaSlugAndAtivo(String slug, Boolean ativo);
}