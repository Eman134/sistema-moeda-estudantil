package br.pucminas.karv_coins.repository;

import br.pucminas.karv_coins.model.Transacao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransacaoRepository extends JpaRepository<Transacao, Long> {
}
