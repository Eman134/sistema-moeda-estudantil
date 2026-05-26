package br.pucminas.karv_coins.repository;

import br.pucminas.karv_coins.model.Aluno;
import jakarta.persistence.LockModeType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AlunoRepository extends JpaRepository<Aluno, Long> {

    Optional<Aluno> findByEmail(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Aluno a where a.email = :email")
    Optional<Aluno> findByEmailForUpdate(@Param("email") String email);

    Optional<Aluno> findByCpf(String cpf);

    boolean existsByCpf(String cpf);

    boolean existsByRg(String rg);

    @Query("""
            select a from Aluno a
            where lower(a.nome) like :search
               or lower(a.email) like :search
               or lower(a.curso) like :search
               or lower(a.instituicao) like :search
            """)
    Page<Aluno> buscarComFiltro(@Param("search") String search, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Aluno a where a.id in :ids")
    List<Aluno> findAllByIdInForUpdate(@Param("ids") List<Long> ids);
}
