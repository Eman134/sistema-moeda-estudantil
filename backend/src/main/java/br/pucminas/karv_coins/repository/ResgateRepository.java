package br.pucminas.karv_coins.repository;

import br.pucminas.karv_coins.model.Resgate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResgateRepository extends JpaRepository<Resgate, Long> {
    boolean existsByCodigoCupom(String codigoCupom);

    @EntityGraph(attributePaths = {"aluno", "vantagem", "vantagem.empresa"})
    Optional<Resgate> findByCodigoCupom(String codigoCupom);

    @EntityGraph(attributePaths = {"vantagem", "vantagem.empresa"})
    List<Resgate> findAllByAluno_EmailOrderByDataDesc(String email);
}
