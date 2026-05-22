package br.pucminas.karv_coins.service;

import br.pucminas.karv_coins.dto.response.ProfessorResumoResponseDto;
import br.pucminas.karv_coins.model.Professor;
import br.pucminas.karv_coins.repository.ProfessorRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfessorService {
    private final ProfessorRepository professorRepository;

    public ProfessorService(ProfessorRepository professorRepository) {
        this.professorRepository = professorRepository;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('PROFESSOR')")
    public ProfessorResumoResponseDto buscarResumo(String email) {
        Professor professor = professorRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Professor não encontrado."));

        return ProfessorResumoResponseDto.from(professor);
    }
}
