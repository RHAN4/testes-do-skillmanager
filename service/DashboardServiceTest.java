package com.senai.skillmanager.service;

import com.senai.skillmanager.dto.DashboardEstagiarioDTO;
import com.senai.skillmanager.dto.EstagiarioResponseDTO;
import com.senai.skillmanager.model.empresa.Empresa;
import com.senai.skillmanager.model.empresa.Supervisor;
import com.senai.skillmanager.model.estagiario.Estagiario;
import com.senai.skillmanager.model.faculdade.Coordenador;
import com.senai.skillmanager.model.faculdade.Faculdade;
import com.senai.skillmanager.repository.CoordenadorRepository;
import com.senai.skillmanager.repository.EstagiarioRepository;
import com.senai.skillmanager.repository.SupervisorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DashboardServiceTest {

    @InjectMocks
    private DashboardService dashboardService;

    @Mock
    private SupervisorRepository supervisorRepository;

    @Mock
    private CoordenadorRepository coordenadorRepository;

    @Mock
    private EstagiarioRepository estagiarioRepository;

    @Mock
    private EstagiarioService estagiarioService;

    @Mock
    private AvaliacaoService avaliacaoService;

    @Mock
    private Authentication authentication;

    private Supervisor supervisorEntidade;
    private Coordenador coordenadorEntidade;
    private Estagiario estagiarioEntidade;

    @BeforeEach
    void setUp() {
        Empresa empresa = new Empresa();
        empresa.setId(1L);

        supervisorEntidade = new Supervisor();
        supervisorEntidade.setId(1L);
        supervisorEntidade.setEmail("supervisor@empresa.com");
        supervisorEntidade.setEmpresa(empresa);

        Faculdade faculdade = new Faculdade();
        faculdade.setId(1L);

        coordenadorEntidade = new Coordenador();
        coordenadorEntidade.setId(1L);
        coordenadorEntidade.setEmail("coordenador@faculdade.com");
        coordenadorEntidade.setFaculdade(faculdade);

        estagiarioEntidade = new Estagiario();
        estagiarioEntidade.setId(1L);
        estagiarioEntidade.setEmail("estagiario@teste.com");
    }

    @Test
    void testGetSupervisorDashboardData() {
        when(authentication.getName()).thenReturn("supervisor@empresa.com");
        when(supervisorRepository.findByEmail(anyString())).thenReturn(Optional.of(supervisorEntidade));
        when(estagiarioRepository.findByEmpresaId(anyLong())).thenReturn(Collections.singletonList(estagiarioEntidade));
        when(estagiarioService.toResponseDTO(any(Estagiario.class))).thenReturn(new EstagiarioResponseDTO());

        List<EstagiarioResponseDTO> resultado = dashboardService.getSupervisorDashboardData(authentication);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(estagiarioRepository, times(1)).findByEmpresaId(1L);
    }

    @Test
    void testGetFaculdadeDashboardData() {
        when(authentication.getName()).thenReturn("coordenador@faculdade.com");
        when(coordenadorRepository.findByEmail(anyString())).thenReturn(Optional.of(coordenadorEntidade));
        when(estagiarioRepository.findByDadosAcademicos_Faculdade_Id(anyLong())).thenReturn(Collections.singletonList(estagiarioEntidade));

        when(estagiarioService.buscarPorId(anyLong(), any(Authentication.class))).thenReturn(new EstagiarioResponseDTO());
        when(avaliacaoService.listarPorEstagiario(anyLong())).thenReturn(Collections.emptyList());

        List<DashboardEstagiarioDTO> resultado = dashboardService.getFaculdadeDashboardData(authentication);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(estagiarioRepository, times(1)).findByDadosAcademicos_Faculdade_Id(1L);
        verify(estagiarioService, times(1)).buscarPorId(1L, authentication);
        verify(avaliacaoService, times(1)).listarPorEstagiario(1L);
    }

    @Test
    void testGetEstagiarioDashboardData() {
        when(authentication.getName()).thenReturn("estagiario@teste.com");
        when(estagiarioRepository.findByEmail(anyString())).thenReturn(Optional.of(estagiarioEntidade));

        when(estagiarioService.buscarPorId(anyLong(), any(Authentication.class))).thenReturn(new EstagiarioResponseDTO());
        when(avaliacaoService.listarPorEstagiario(anyLong())).thenReturn(Collections.emptyList());

        DashboardEstagiarioDTO resultado = dashboardService.getEstagiarioDashboardData(authentication);

        assertNotNull(resultado);
        verify(estagiarioRepository, times(1)).findByEmail("estagiario@teste.com");
        verify(estagiarioService, times(1)).buscarPorId(1L, authentication);
        verify(avaliacaoService, times(1)).listarPorEstagiario(1L);
    }
}