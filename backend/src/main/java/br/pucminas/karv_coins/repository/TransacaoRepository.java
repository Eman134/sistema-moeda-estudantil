package br.pucminas.karv_coins.repository;

import br.pucminas.karv_coins.model.Transacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransacaoRepository extends JpaRepository<Transacao, Long> {

    Page<Transacao> findByAluno_EmailOrderByDataDesc(String email, Pageable pageable);

    Page<Transacao> findByProfessor_EmailOrderByDataDesc(String email, Pageable pageable);
}
