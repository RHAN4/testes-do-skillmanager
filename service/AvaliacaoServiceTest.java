package com.senai.skillmanager.service;

import com.senai.skillmanager.dto.AvaliacaoDTO;
import com.senai.skillmanager.dto.AvaliacaoResponseDTO;
import com.senai.skillmanager.model.avaliacao.Avaliacao;
import com.senai.skillmanager.model.empresa.Supervisor;
import com.senai.skillmanager.model.estagiario.Estagiario;
import com.senai.skillmanager.repository.AvaliacaoRepository;
import com.senai.skillmanager.repository.EstagiarioRepository;
import com.senai.skillmanager.repository.SupervisorRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AvaliacaoServiceTest {

    @InjectMocks
    private AvaliacaoService avaliacaoService;

    @Mock
    private AvaliacaoRepository avaliacaoRepository;

    @Mock
    private SupervisorRepository supervisorRepository;

    @Mock
    private EstagiarioRepository estagiarioRepository;

    @Mock
    private SupervisorService supervisorService;

    @Mock
    private EstagiarioService estagiarioService;

    private AvaliacaoDTO avaliacaoDTO;
    private Supervisor supervisorEntidade;
    private Estagiario estagiarioEntidade;
    private Avaliacao avaliacaoEntidade;

    @BeforeEach
    void setUp() {
        avaliacaoDTO = new AvaliacaoDTO();
        avaliacaoDTO.setTitulo("Avaliação Teste");
        avaliacaoDTO.setSupervisorId(1L);
        avaliacaoDTO.setEstagiarioId(1L);
        avaliacaoDTO.setNotaDesempenho(5);

        supervisorEntidade = new Supervisor();
        supervisorEntidade.setId(1L);

        estagiarioEntidade = new Estagiario();
        estagiarioEntidade.setId(1L);

        avaliacaoEntidade = new Avaliacao();
        avaliacaoEntidade.setId(1L);
        avaliacaoEntidade.setTitulo("Avaliação Teste");
        avaliacaoEntidade.setDataAvaliacao(LocalDate.now());
        avaliacaoEntidade.setSupervisor(supervisorEntidade);
        avaliacaoEntidade.setEstagiario(estagiarioEntidade);
    }

    @Test
    void testSalvar_Sucesso() {
        when(supervisorRepository.findById(1L)).thenReturn(Optional.of(supervisorEntidade));
        when(estagiarioRepository.findById(1L)).thenReturn(Optional.of(estagiarioEntidade));
        when(avaliacaoRepository.save(any(Avaliacao.class))).thenReturn(avaliacaoEntidade);
        when(supervisorService.toResponseDTO(any(Supervisor.class))).thenReturn(null);
        when(estagiarioService.toResponseDTO(any(Estagiario.class))).thenReturn(null);

        AvaliacaoResponseDTO resultado = avaliacaoService.salvar(avaliacaoDTO);

        assertNotNull(resultado);
        assertEquals("Avaliação Teste", resultado.getTitulo());
        assertNotNull(resultado.getDataAvaliacao());

        verify(supervisorRepository, times(1)).findById(1L);
        verify(estagiarioRepository, times(1)).findById(1L);
        verify(avaliacaoRepository, times(1)).save(any(Avaliacao.class));
    }

    @Test
    void testSalvar_Falha_SupervisorNaoEncontrado() {
        when(supervisorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            avaliacaoService.salvar(avaliacaoDTO);
        });

        verify(estagiarioRepository, never()).findById(anyLong());
        verify(avaliacaoRepository, never()).save(any(Avaliacao.class));
    }
}