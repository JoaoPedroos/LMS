package br.com.gerenciamento.repository;

import br.com.gerenciamento.model.Convite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConviteRepository extends JpaRepository<Convite, Long> {
    Optional<Convite> findByTokenAndUtilizadoFalse(String token);
}