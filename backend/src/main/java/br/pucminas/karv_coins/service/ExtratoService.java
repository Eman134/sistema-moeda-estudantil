package br.pucminas.karv_coins.service;

import br.pucminas.karv_coins.dto.response.ExtratoItemResponseDto;
import br.pucminas.karv_coins.dto.response.ExtratoResponseDto;
import br.pucminas.karv_coins.dto.response.PaginaResponseDto;
import br.pucminas.karv_coins.model.Aluno;
import br.pucminas.karv_coins.model.Professor;
import br.pucminas.karv_coins.model.Transacao;
import br.pucminas.karv_coins.repository.AlunoRepository;
import br.pucminas.karv_coins.repository.ProfessorRepository;
import br.pucminas.karv_coins.repository.TransacaoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExtratoService {
    private final AlunoRepository alunoRepository;
    private final ProfessorRepository professorRepository;
    private final TransacaoRepository transacaoRepository;

    public ExtratoService(
            AlunoRepository alunoRepository,
            ProfessorRepository professorRepository,
            TransacaoRepository transacaoRepository
    ) {
        this.alunoRepository = alunoRepository;
        this.professorRepository = professorRepository;
        this.transacaoRepository = transacaoRepository;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ALUNO')")
    public ExtratoResponseDto consultarExtratoAluno(String email, int page, int size) {
        Aluno aluno = alunoRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Aluno não encontrado."));

        Page<Transacao> transacoes = transacaoRepository.findByAluno_EmailOrderByDataDesc(
                email,
                PageRequest.of(page, size)
        );

        Page<ExtratoItemResponseDto> itens = transacoes.map(ExtratoItemResponseDto::creditoAluno);
        return new ExtratoResponseDto(aluno.getSaldo(), PaginaResponseDto.from(itens));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PROFESSOR')")
    public ExtratoResponseDto consultarExtratoProfessor(String email, int page, int size) {
        Professor professor = professorRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Professor não encontrado."));

        Page<Transacao> transacoes = transacaoRepository.findByProfessor_EmailOrderByDataDesc(
                email,
                PageRequest.of(page, size)
        );

        Page<ExtratoItemResponseDto> itens = transacoes.map(ExtratoItemResponseDto::debitoProfessor);
        return new ExtratoResponseDto(professor.getSaldo(), PaginaResponseDto.from(itens));
    }
}
