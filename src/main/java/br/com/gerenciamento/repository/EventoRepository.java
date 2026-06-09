package br.com.gerenciamento.repository;

import br.com.gerenciamento.model.Evento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventoRepository extends JpaRepository<Evento, Long> {
    List<Evento> findByEmpresaSlug(String slug);
    List<Evento> findByEmpresaSlugAndDataHoraBetween(String slug, LocalDateTime inicio, LocalDateTime fim);
}