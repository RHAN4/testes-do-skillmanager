package com.senai.skillmanager.service;

import com.senai.skillmanager.dto.CoordenadorDTO;
import com.senai.skillmanager.dto.CoordenadorResponseDTO;
import com.senai.skillmanager.dto.FaculdadeDTO;
import com.senai.skillmanager.dto.FaculdadeResponseDTO;
import com.senai.skillmanager.model.faculdade.Coordenador;
import com.senai.skillmanager.model.faculdade.Faculdade;
import com.senai.skillmanager.repository.CoordenadorRepository;
import com.senai.skillmanager.repository.FaculdadeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CoordenadorServiceTest {

    @InjectMocks
    private CoordenadorService coordenadorService;

    @Mock
    private CoordenadorRepository coordenadorRepository;

    @Mock
    private FaculdadeRepository faculdadeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private FaculdadeService faculdadeService;

    @Mock
    private Authentication authentication;

    private CoordenadorDTO coordenadorDTO;
    private Coordenador coordenadorEntidade;
    private Faculdade faculdadeEntidade;
    private FaculdadeResponseDTO faculdadeResponseDTO;

    @BeforeEach
    void setUp() {
        coordenadorDTO = new CoordenadorDTO();
        coordenadorDTO.setNome("Coordenador Teste");
        coordenadorDTO.setEmail("teste@faculdade.com");
        coordenadorDTO.setSenha("senha123");
        coordenadorDTO.setFaculdadeCnpj("22222222000122");
        coordenadorDTO.setFaculdadeNome("Nova Faculdade Teste");
        coordenadorDTO.setFaculdadeTelefone("11999998888");

        faculdadeEntidade = new Faculdade();
        faculdadeEntidade.setId(1L);
        faculdadeEntidade.setNome("Nova Faculdade Teste");
        faculdadeEntidade.setCnpj("22222222000122");

        coordenadorEntidade = new Coordenador();
        coordenadorEntidade.setId(1L);
        coordenadorEntidade.setNome("Coordenador Teste");
        coordenadorEntidade.setEmail("teste@faculdade.com");
        coordenadorEntidade.setSenha("hashed_senha123");
        coordenadorEntidade.setFaculdade(faculdadeEntidade);

        faculdadeResponseDTO = new FaculdadeResponseDTO();
        faculdadeResponseDTO.setId(1L);
        faculdadeResponseDTO.setNome("Nova Faculdade Teste");
        faculdadeResponseDTO.setCnpj("22222222000122");
    }

    @Test
    void testSalvar_FluxoA_NovaFaculdade() {
        when(coordenadorRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(faculdadeRepository.findByCnpj(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_senha123");
        when(faculdadeService.salvar(any(FaculdadeDTO.class))).thenReturn(faculdadeResponseDTO);
        when(faculdadeService.buscarEntidadePorId(anyLong())).thenReturn(faculdadeEntidade);
        when(coordenadorRepository.save(any(Coordenador.class))).thenReturn(coordenadorEntidade);

        CoordenadorResponseDTO resultado = coordenadorService.salvar(coordenadorDTO);

        assertNotNull(resultado);
        assertEquals("teste@faculdade.com", resultado.getEmail());
        assertNotNull(resultado.getFaculdade());
        assertEquals("Nova Faculdade Teste", resultado.getFaculdade().getNome());

        verify(faculdadeRepository, times(1)).findByCnpj("22222222000122");
        verify(faculdadeService, times(1)).salvar(any(FaculdadeDTO.class));
        verify(coordenadorRepository, times(1)).save(any(Coordenador.class));
    }

    @Test
    void testSalvar_FluxoB_FaculdadeExistente() {
        coordenadorDTO.setFaculdadeNome(null);
        coordenadorDTO.setFaculdadeTelefone(null);

        when(coordenadorRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(faculdadeRepository.findByCnpj(anyString())).thenReturn(Optional.of(faculdadeEntidade));
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_senha123");
        when(coordenadorRepository.save(any(Coordenador.class))).thenReturn(coordenadorEntidade);

        CoordenadorResponseDTO resultado = coordenadorService.salvar(coordenadorDTO);

        assertNotNull(resultado);
        assertEquals("teste@faculdade.com", resultado.getEmail());
        assertEquals(1L, resultado.getFaculdade().getId());

        verify(faculdadeRepository, times(1)).findByCnpj("22222222000122");
        verify(faculdadeService, never()).salvar(any(FaculdadeDTO.class));
        verify(coordenadorRepository, times(1)).save(any(Coordenador.class));
    }

    @Test
    void testBuscarPorId_Sucesso_Proprietario() {
        when(coordenadorRepository.findById(1L)).thenReturn(Optional.of(coordenadorEntidade));
        when(authentication.getName()).thenReturn("teste@faculdade.com");
        when(authentication.getAuthorities()).thenReturn(Collections.singleton(new SimpleGrantedAuthority("ROLE_FACULDADE")));

        CoordenadorResponseDTO resultado = coordenadorService.buscarPorId(1L, authentication);

        assertNotNull(resultado);
        assertEquals("teste@faculdade.com", resultado.getEmail());
    }

    @Test
    void testBuscarPorId_Falha_NaoProprietario() {
        Coordenador outroCoordenador = new Coordenador();
        outroCoordenador.setId(2L);
        outroCoordenador.setEmail("outro@email.com");

        when(coordenadorRepository.findById(2L)).thenReturn(Optional.of(outroCoordenador));
        when(authentication.getName()).thenReturn("teste@faculdade.com");
        when(authentication.getAuthorities()).thenReturn(Collections.singleton(new SimpleGrantedAuthority("ROLE_FACULDADE")));

        assertThrows(SecurityException.class, () -> {
            coordenadorService.buscarPorId(2L, authentication);
        }, "Acesso negado. Você não tem permissão para acessar este recurso.");
    }
}