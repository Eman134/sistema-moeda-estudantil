package br.pucminas.karv_coins.service;

import br.pucminas.karv_coins.config.FrontendProperties;
import br.pucminas.karv_coins.dto.notification.ResgateNotificacaoDto;
import br.pucminas.karv_coins.dto.response.ResgateResponseDto;
import br.pucminas.karv_coins.dto.response.ResgateVerificacaoResponseDto;
import br.pucminas.karv_coins.event.ResgateCriadoEvent;
import br.pucminas.karv_coins.model.Aluno;
import br.pucminas.karv_coins.model.Empresa;
import br.pucminas.karv_coins.model.Resgate;
import br.pucminas.karv_coins.model.Vantagem;
import br.pucminas.karv_coins.repository.AlunoRepository;
import br.pucminas.karv_coins.repository.ResgateRepository;
import br.pucminas.karv_coins.repository.VantagemRepository;
import jakarta.persistence.EntityNotFoundException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResgateService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final char[] CODIGO_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();

    private final AlunoRepository alunoRepository;
    private final VantagemRepository vantagemRepository;
    private final ResgateRepository resgateRepository;
    private final FrontendProperties frontendProperties;
    private final ApplicationEventPublisher eventPublisher;

    public ResgateService(
            AlunoRepository alunoRepository,
            VantagemRepository vantagemRepository,
            ResgateRepository resgateRepository,
            FrontendProperties frontendProperties,
            ApplicationEventPublisher eventPublisher
    ) {
        this.alunoRepository = alunoRepository;
        this.vantagemRepository = vantagemRepository;
        this.resgateRepository = resgateRepository;
        this.frontendProperties = frontendProperties;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    @PreAuthorize("hasAuthority('ALUNO')")
    public ResgateResponseDto resgatar(String emailAluno, Long vantagemId) {
        Aluno aluno = alunoRepository.findByEmailForUpdate(emailAluno)
                .orElseThrow(() -> new EntityNotFoundException("Aluno não encontrado."));
        Vantagem vantagem = vantagemRepository.findWithEmpresaById(vantagemId)
                .orElseThrow(() -> new EntityNotFoundException("Vantagem não encontrada."));

        Double custo = vantagem.getCustoMoedas();
        Double saldoAtual = aluno.getSaldo() == null ? 0.0 : aluno.getSaldo();
        if (saldoAtual < custo) {
            throw new IllegalArgumentException("Saldo insuficiente para resgatar esta vantagem.");
        }

        aluno.setSaldo(saldoAtual - custo);

        Resgate resgate = new Resgate();
        resgate.setAluno(aluno);
        resgate.setVantagem(vantagem);
        resgate.setValorMoedas(custo);
        resgate.setCodigoCupom(gerarCodigoCupomUnico());
        resgate.setData(LocalDateTime.now());

        Resgate resgateSalvo = resgateRepository.save(resgate);
        String urlVerificacao = frontendProperties.verificationUrl(resgateSalvo.getCodigoCupom());
        publicarEventoNotificacao(resgateSalvo, urlVerificacao);

        return ResgateResponseDto.from(resgateSalvo, aluno.getSaldo(), urlVerificacao);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('EMPRESA')")
    public ResgateVerificacaoResponseDto verificar(String emailEmpresa, String codigoCupom) {
        Resgate resgate = resgateRepository.findByCodigoCupom(codigoCupom)
                .orElseThrow(() -> new EntityNotFoundException("Resgate não encontrado."));

        Empresa empresa = resgate.getVantagem().getEmpresa();
        if (!empresa.getEmail().equals(emailEmpresa)) {
            throw new EntityNotFoundException("Resgate não encontrado para a empresa autenticada.");
        }

        return ResgateVerificacaoResponseDto.from(resgate);
    }

    private void publicarEventoNotificacao(Resgate resgate, String urlVerificacao) {
        ResgateNotificacaoDto notificacao = new ResgateNotificacaoDto(
                resgate.getAluno().getEmail(),
                resgate.getAluno().getNome(),
                resgate.getAluno().getSaldo(),
                resgate.getVantagem().getEmpresa().getEmail(),
                resgate.getVantagem().getEmpresa().getNome(),
                resgate.getVantagem().getDescricao(),
                resgate.getValorMoedas(),
                resgate.getCodigoCupom(),
                resgate.getData(),
                urlVerificacao
        );
        eventPublisher.publishEvent(new ResgateCriadoEvent(notificacao));
    }

    private String gerarCodigoCupomUnico() {
        String codigo;
        do {
            codigo = gerarCodigoCupom();
        } while (resgateRepository.existsByCodigoCupom(codigo));
        return codigo;
    }

    private String gerarCodigoCupom() {
        StringBuilder codigo = new StringBuilder("KC-");
        for (int i = 0; i < 12; i++) {
            codigo.append(CODIGO_CHARS[RANDOM.nextInt(CODIGO_CHARS.length)]);
        }
        return codigo.toString();
    }
}
