package br.pucminas.karv_coins.repository;

import br.pucminas.karv_coins.model.Vantagem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VantagemRepository extends JpaRepository<Vantagem, Long> {
    List<Vantagem> findByEmpresaEmailOrderByIdDesc(String email);

    Optional<Vantagem> findByIdAndEmpresaEmail(Long id, String email);

    @EntityGraph(attributePaths = "empresa")
    Page<Vantagem> findAllByOrderByIdDesc(Pageable pageable);

    @EntityGraph(attributePaths = "empresa")
    @Query("select v from Vantagem v where v.id = :id")
    Optional<Vantagem> findWithEmpresaById(@Param("id") Long id);
}
