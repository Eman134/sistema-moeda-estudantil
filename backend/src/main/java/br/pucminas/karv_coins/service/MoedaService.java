package br.pucminas.karv_coins.service;

import br.pucminas.karv_coins.dto.notification.TransacaoNotificacaoDto;
import br.pucminas.karv_coins.dto.request.EnvioMoedasItemRequestDto;
import br.pucminas.karv_coins.dto.request.EnviarMoedasRequestDto;
import br.pucminas.karv_coins.dto.response.EnvioMoedasItemResponseDto;
import br.pucminas.karv_coins.dto.response.EnviarMoedasResponseDto;
import br.pucminas.karv_coins.event.MoedasEnviadasEvent;
import br.pucminas.karv_coins.model.Aluno;
import br.pucminas.karv_coins.model.Professor;
import br.pucminas.karv_coins.model.Transacao;
import br.pucminas.karv_coins.repository.AlunoRepository;
import br.pucminas.karv_coins.repository.ProfessorRepository;
import br.pucminas.karv_coins.repository.TransacaoRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MoedaService {
    private final ProfessorRepository professorRepository;
    private final AlunoRepository alunoRepository;
    private final TransacaoRepository transacaoRepository;
    private final ApplicationEventPublisher eventPublisher;

    public MoedaService(
            ProfessorRepository professorRepository,
            AlunoRepository alunoRepository,
            TransacaoRepository transacaoRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.professorRepository = professorRepository;
        this.alunoRepository = alunoRepository;
        this.transacaoRepository = transacaoRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    @PreAuthorize("hasAuthority('PROFESSOR')")
    public EnviarMoedasResponseDto enviarMoedas(String emailProfessor, EnviarMoedasRequestDto request) {
        validarAlunosDuplicados(request.envios());

        Professor professor = professorRepository.findByEmailForUpdate(emailProfessor)
                .orElseThrow(() -> new EntityNotFoundException("Professor não encontrado."));

        List<Long> alunoIds = request.envios().stream()
                .map(EnvioMoedasItemRequestDto::alunoId)
                .toList();
        Map<Long, Aluno> alunosPorId = buscarAlunosParaAtualizacao(alunoIds);
        Double totalEnviado = request.envios().stream()
                .mapToDouble(EnvioMoedasItemRequestDto::valor)
                .sum();

        if (professor.getSaldo() == null || professor.getSaldo() < totalEnviado) {
            throw new IllegalArgumentException("Saldo insuficiente para completar a operação.");
        }

        professor.setSaldo(professor.getSaldo() - totalEnviado);

        List<Transacao> transacoes = new ArrayList<>();
        for (EnvioMoedasItemRequestDto envio : request.envios()) {
            Aluno aluno = alunosPorId.get(envio.alunoId());
            aluno.setSaldo((aluno.getSaldo() == null ? 0.0 : aluno.getSaldo()) + envio.valor());

            Transacao transacao = new Transacao();
            transacao.setProfessor(professor);
            transacao.setAluno(aluno);
            transacao.setValor(envio.valor());
            transacao.setMotivo(envio.motivo().trim());
            transacao.setData(LocalDateTime.now());
            transacoes.add(transacao);
        }

        List<Transacao> transacoesSalvas = transacaoRepository.saveAll(transacoes);
        publicarEventoNotificacao(transacoesSalvas, professor.getSaldo());

        List<EnvioMoedasItemResponseDto> envios = transacoesSalvas.stream()
                .map(EnvioMoedasItemResponseDto::from)
                .toList();

        return new EnviarMoedasResponseDto(professor.getSaldo(), totalEnviado, envios);
    }

    private void validarAlunosDuplicados(List<EnvioMoedasItemRequestDto> envios) {
        Set<Long> alunoIds = new HashSet<>();
        for (EnvioMoedasItemRequestDto envio : envios) {
            if (!alunoIds.add(envio.alunoId())) {
                throw new IllegalArgumentException(
                        "Não é permitido enviar mais de uma movimentação para o mesmo aluno na mesma requisição."
                );
            }
        }
    }

    private void publicarEventoNotificacao(List<Transacao> transacoes, Double saldoProfessor) {
        List<TransacaoNotificacaoDto> notificacoes = transacoes.stream()
                .map(transacao -> new TransacaoNotificacaoDto(
                        transacao.getAluno().getEmail(),
                        transacao.getAluno().getNome(),
                        transacao.getAluno().getSaldo(),
                        transacao.getProfessor().getEmail(),
                        transacao.getProfessor().getNome(),
                        saldoProfessor,
                        transacao.getValor(),
                        transacao.getMotivo(),
                        transacao.getData()
                ))
                .toList();

        eventPublisher.publishEvent(new MoedasEnviadasEvent(notificacoes));
    }

    private Map<Long, Aluno> buscarAlunosParaAtualizacao(List<Long> alunoIds) {
        List<Aluno> alunos = alunoRepository.findAllByIdInForUpdate(alunoIds);
        if (alunos.size() != alunoIds.size()) {
            throw new EntityNotFoundException("Um ou mais alunos informados não foram encontrados.");
        }

        Map<Long, Aluno> alunosPorId = new HashMap<>();
        for (Aluno aluno : alunos) {
            alunosPorId.put(aluno.getId(), aluno);
        }
        return alunosPorId;
    }
}
