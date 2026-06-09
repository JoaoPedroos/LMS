package br.com.gerenciamento.repository;

import br.com.gerenciamento.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    boolean existsByEmail(String email);
    boolean existsByTelefone(String telefone);

    Optional<Usuario> findByEmail(String email);
        
    @Query("SELECT u FROM Usuario u WHERE u.email = :email AND u.senha = :senha AND u.empresa.slug = :slug")
    Optional<Usuario> buscarLoginPorTenant(@Param("email") String email, @Param("senha") String senha, @Param("slug") String slug);


}