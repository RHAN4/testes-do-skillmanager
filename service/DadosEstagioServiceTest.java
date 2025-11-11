package com.senai.skillmanager.service;

import com.senai.skillmanager.dto.DadosEstagioDTO;
import com.senai.skillmanager.dto.DadosEstagioResponseDTO;
import com.senai.skillmanager.model.empresa.Empresa;
import com.senai.skillmanager.model.empresa.Supervisor;
import com.senai.skillmanager.model.estagiario.DadosEstagio;
import com.senai.skillmanager.model.estagiario.Estagiario;
import com.senai.skillmanager.repository.DadosEstagioRepository;
import com.senai.skillmanager.repository.EstagiarioRepository;
import com.senai.skillmanager.repository.SupervisorRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DadosEstagioServiceTest {

    @InjectMocks
    private DadosEstagioService dadosEstagioService;

    @Mock
    private DadosEstagioRepository dadosEstagioRepository;

    @Mock
    private SupervisorRepository supervisorRepository;

    @Mock
    private EstagiarioRepository estagiarioRepository;

    @Mock
    private SupervisorService supervisorService;

    @Mock
    private EstagiarioService estagiarioService;

    @Mock
    private Authentication authentication;

    private DadosEstagioDTO dadosEstagioDTO;
    private Supervisor supervisorEntidade;
    private Estagiario estagiarioEntidade;
    private Empresa empresaEntidade;
    private DadosEstagio dadosEstagioEntidade;

    @BeforeEach
    void setUp() {
        dadosEstagioDTO = new DadosEstagioDTO();
        dadosEstagioDTO.setTitulo("Estágio Teste");
        dadosEstagioDTO.setEstagiarioId(1L);

        empresaEntidade = new Empresa();
        empresaEntidade.setId(1L);

        supervisorEntidade = new Supervisor();
        supervisorEntidade.setId(1L);
        supervisorEntidade.setEmail("supervisor@empresa.com");
        supervisorEntidade.setEmpresa(empresaEntidade);

        estagiarioEntidade = new Estagiario();
        estagiarioEntidade.setId(1L);
        estagiarioEntidade.setEmpresa(empresaEntidade);

        dadosEstagioEntidade = new DadosEstagio();
        dadosEstagioEntidade.setId(1L);
        dadosEstagioEntidade.setTitulo("Estágio Teste");
        dadosEstagioEntidade.setSupervisor(supervisorEntidade);
        dadosEstagioEntidade.setEstagiario(estagiarioEntidade);
    }

    @Test
    void testSalvar_Sucesso() {
        when(authentication.getName()).thenReturn("supervisor@empresa.com");
        when(supervisorRepository.findByEmail(anyString())).thenReturn(Optional.of(supervisorEntidade));
        when(estagiarioRepository.findById(1L)).thenReturn(Optional.of(estagiarioEntidade));
        when(dadosEstagioRepository.save(any(DadosEstagio.class))).thenReturn(dadosEstagioEntidade);

        when(supervisorService.toResponseDTO(any(Supervisor.class))).thenReturn(null);
        when(estagiarioService.toResponseDTO(any(Estagiario.class))).thenReturn(null);

        DadosEstagioResponseDTO resultado = dadosEstagioService.salvar(dadosEstagioDTO, authentication);

        assertNotNull(resultado);
        assertEquals("Estágio Teste", resultado.getTitulo());

        verify(supervisorRepository, times(1)).findByEmail("supervisor@empresa.com");
        verify(estagiarioRepository, times(1)).findById(1L);
        verify(dadosEstagioRepository, times(1)).save(any(DadosEstagio.class));
    }

    @Test
    void testSalvar_Falha_SupervisorNaoEncontrado() {
        when(authentication.getName()).thenReturn("supervisor@empresa.com");
        when(supervisorRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            dadosEstagioService.salvar(dadosEstagioDTO, authentication);
        });

        verify(estagiarioRepository, never()).findById(anyLong());
        verify(dadosEstagioRepository, never()).save(any(DadosEstagio.class));
    }

    @Test
    void testSalvar_Falha_EmpresasDiferentes() {
        Empresa outraEmpresa = new Empresa();
        outraEmpresa.setId(2L);
        estagiarioEntidade.setEmpresa(outraEmpresa);

        when(authentication.getName()).thenReturn("supervisor@empresa.com");
        when(supervisorRepository.findByEmail(anyString())).thenReturn(Optional.of(supervisorEntidade));
        when(estagiarioRepository.findById(1L)).thenReturn(Optional.of(estagiarioEntidade));

        assertThrows(SecurityException.class, () -> {
            dadosEstagioService.salvar(dadosEstagioDTO, authentication);
        }, "Acesso negado: O estagiário não pertence à empresa deste supervisor.");

        verify(dadosEstagioRepository, never()).save(any(DadosEstagio.class));
    }
}