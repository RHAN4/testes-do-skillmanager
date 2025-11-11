package com.senai.skillmanager.service;

import com.senai.skillmanager.dto.DadosAcademicosDTO;
import com.senai.skillmanager.dto.DadosAcademicosResponseDTO;
import com.senai.skillmanager.model.estagiario.DadosAcademicos;
import com.senai.skillmanager.model.faculdade.Faculdade;
import com.senai.skillmanager.repository.DadosAcademicosRepository;
import com.senai.skillmanager.repository.FaculdadeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DadosAcademicosServiceTest {

    @InjectMocks
    private DadosAcademicosService dadosAcademicosService;

    @Mock
    private DadosAcademicosRepository dadosAcademicosRepository;

    @Mock
    private FaculdadeRepository faculdadeRepository;

    private DadosAcademicosDTO dadosAcademicosDTO;
    private Faculdade faculdadeEntidade;
    private DadosAcademicos dadosAcademicosEntidade;

    @BeforeEach
    void setUp() {
        dadosAcademicosDTO = new DadosAcademicosDTO();
        dadosAcademicosDTO.setCurso("Engenharia de Testes");
        dadosAcademicosDTO.setFaculdadeCnpj("22222222000122");

        faculdadeEntidade = new Faculdade();
        faculdadeEntidade.setId(1L);
        faculdadeEntidade.setCnpj("22222222000122");

        dadosAcademicosEntidade = new DadosAcademicos();
        dadosAcademicosEntidade.setId(1L);
        dadosAcademicosEntidade.setCurso("Engenharia de Testes");
        dadosAcademicosEntidade.setFaculdade(faculdadeEntidade);
    }

    @Test
    void testSalvar_Sucesso() {
        when(faculdadeRepository.findByCnpj(anyString())).thenReturn(Optional.of(faculdadeEntidade));
        when(dadosAcademicosRepository.save(any(DadosAcademicos.class))).thenReturn(dadosAcademicosEntidade);

        DadosAcademicosResponseDTO resultado = dadosAcademicosService.salvar(dadosAcademicosDTO);

        assertNotNull(resultado);
        assertEquals("Engenharia de Testes", resultado.getCurso());
        assertNotNull(resultado.getFaculdade());
        assertEquals("22222222000122", resultado.getFaculdade().getCnpj());

        verify(faculdadeRepository, times(1)).findByCnpj("22222222000122");
        verify(dadosAcademicosRepository, times(1)).save(any(DadosAcademicos.class));
    }

    @Test
    void testSalvar_Falha_FaculdadeNaoEncontrada() {
        when(faculdadeRepository.findByCnpj(anyString())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            dadosAcademicosService.salvar(dadosAcademicosDTO);
        }, "Faculdade não encontrada com CNPJ: 22222222000122");

        verify(dadosAcademicosRepository, never()).save(any(DadosAcademicos.class));
    }
}