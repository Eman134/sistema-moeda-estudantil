package br.pucminas.karv_coins.service;

import br.pucminas.karv_coins.dto.response.ExtratoItemResponseDto;
import br.pucminas.karv_coins.dto.response.ExtratoResponseDto;
import br.pucminas.karv_coins.dto.response.PaginaResponseDto;
import br.pucminas.karv_coins.model.Aluno;
import br.pucminas.karv_coins.model.Professor;
import br.pucminas.karv_coins.model.Resgate;
import br.pucminas.karv_coins.model.Transacao;
import br.pucminas.karv_coins.repository.AlunoRepository;
import br.pucminas.karv_coins.repository.ProfessorRepository;
import br.pucminas.karv_coins.repository.ResgateRepository;
import br.pucminas.karv_coins.repository.TransacaoRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExtratoService {
    private final AlunoRepository alunoRepository;
    private final ProfessorRepository professorRepository;
    private final TransacaoRepository transacaoRepository;
    private final ResgateRepository resgateRepository;

    public ExtratoService(
            AlunoRepository alunoRepository,
            ProfessorRepository professorRepository,
            TransacaoRepository transacaoRepository,
            ResgateRepository resgateRepository
    ) {
        this.alunoRepository = alunoRepository;
        this.professorRepository = professorRepository;
        this.transacaoRepository = transacaoRepository;
        this.resgateRepository = resgateRepository;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ALUNO')")
    public ExtratoResponseDto consultarExtratoAluno(String email, int page, int size) {
        Aluno aluno = alunoRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Aluno não encontrado."));

        Pageable pageable = PageRequest.of(page, size);
        List<Resgate> resgatesComprados = resgateRepository.findAllByAluno_EmailOrderByDataDesc(email);
        List<Resgate> resgatesUtilizados = resgateRepository.findAllByUtilizadoPor_EmailOrderByDataDesc(email)
                .stream()
                .filter(resgate -> !resgate.getAluno().getEmail().equals(email))
                .toList();

        List<ExtratoItemResponseDto> lancamentos = Stream.concat(
                        transacaoRepository.findAllByAluno_EmailOrderByDataDesc(email)
                                .stream()
                                .map(ExtratoItemResponseDto::creditoAluno),
                        Stream.concat(
                                resgatesComprados.stream().map(ExtratoItemResponseDto::resgateAluno),
                                resgatesUtilizados.stream().map(ExtratoItemResponseDto::usoResgateAluno)
                        )
                )
                .sorted(Comparator.comparing(ExtratoItemResponseDto::data).reversed())
                .toList();

        Page<ExtratoItemResponseDto> itens = paginar(lancamentos, pageable);
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

    private Page<ExtratoItemResponseDto> paginar(List<ExtratoItemResponseDto> lancamentos, Pageable pageable) {
        int inicio = Math.min((int) pageable.getOffset(), lancamentos.size());
        int fim = Math.min(inicio + pageable.getPageSize(), lancamentos.size());
        return new PageImpl<>(lancamentos.subList(inicio, fim), pageable, lancamentos.size());
    }
}
