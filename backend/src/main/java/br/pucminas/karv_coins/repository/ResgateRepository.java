package br.pucminas.karv_coins.repository;

import br.pucminas.karv_coins.model.Resgate;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ResgateRepository extends JpaRepository<Resgate, Long> {
    boolean existsByCodigoCupom(String codigoCupom);

    @EntityGraph(attributePaths = {"aluno", "utilizadoPor", "vantagem", "vantagem.empresa"})
    Optional<Resgate> findByCodigoCupom(String codigoCupom);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"aluno", "utilizadoPor", "vantagem", "vantagem.empresa"})
    @Query("select r from Resgate r where r.codigoCupom = :codigoCupom")
    Optional<Resgate> findByCodigoCupomForUpdate(@Param("codigoCupom") String codigoCupom);

    @EntityGraph(attributePaths = {"utilizadoPor", "vantagem", "vantagem.empresa"})
    List<Resgate> findAllByAluno_EmailOrderByDataDesc(String email);

    @EntityGraph(attributePaths = {"aluno", "vantagem", "vantagem.empresa"})
    List<Resgate> findAllByUtilizadoPor_EmailOrderByDataDesc(String email);
}
