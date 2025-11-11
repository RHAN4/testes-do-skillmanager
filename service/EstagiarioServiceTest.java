package com.senai.skillmanager.service;

import com.senai.skillmanager.dto.DadosAcademicosDTO;
import com.senai.skillmanager.dto.EnderecoDTO;
import com.senai.skillmanager.dto.EstagiarioDTO;
import com.senai.skillmanager.dto.EstagiarioResponseDTO;
import com.senai.skillmanager.model.Endereco;
import com.senai.skillmanager.model.empresa.Empresa;
import com.senai.skillmanager.model.estagiario.DadosAcademicos;
import com.senai.skillmanager.model.estagiario.Estagiario;
import com.senai.skillmanager.model.faculdade.Faculdade;
import com.senai.skillmanager.repository.EmpresaRepository;
import com.senai.skillmanager.repository.EstagiarioRepository;
import com.senai.skillmanager.repository.FaculdadeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EstagiarioServiceTest {

    @InjectMocks
    private EstagiarioService estagiarioService;

    @Mock
    private EstagiarioRepository estagiarioRepository;

    @Mock
    private FaculdadeRepository faculdadeRepository;

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private EstagiarioDTO estagiarioDTO;
    private Empresa empresaEntidade;
    private Faculdade faculdadeEntidade;
    private Estagiario estagiarioEntidade;

    @BeforeEach
    void setUp() {
        EnderecoDTO enderecoDTO = new EnderecoDTO();
        enderecoDTO.setCep("12345678");

        DadosAcademicosDTO dadosAcademicosDTO = new DadosAcademicosDTO();
        dadosAcademicosDTO.setFaculdadeCnpj("22222222000122");
        dadosAcademicosDTO.setCurso("Engenharia de Testes");
        dadosAcademicosDTO.setRa("123456");

        estagiarioDTO = new EstagiarioDTO();
        estagiarioDTO.setNome("Estagiario Teste");
        estagiarioDTO.setEmail("estagiario@teste.com");
        estagiarioDTO.setCpf("12345678900");
        estagiarioDTO.setSenha("senha123");
        estagiarioDTO.setCodigoEmpresa("A1B-C2D");
        estagiarioDTO.setEndereco(enderecoDTO);
        estagiarioDTO.setDadosAcademicos(dadosAcademicosDTO);

        empresaEntidade = new Empresa();
        empresaEntidade.setId(1L);
        empresaEntidade.setCodigoEmpresa("A1B-C2D");

        faculdadeEntidade = new Faculdade();
        faculdadeEntidade.setId(1L);
        faculdadeEntidade.setCnpj("22222222000122");

        estagiarioEntidade = new Estagiario();
        estagiarioEntidade.setId(1L);
        estagiarioEntidade.setNome("Estagiario Teste");
        estagiarioEntidade.setEmail("estagiario@teste.com");
        estagiarioEntidade.setCpf("12345678900");
        estagiarioEntidade.setSenha("hashed_senha123");
        estagiarioEntidade.setEmpresa(empresaEntidade);
        estagiarioEntidade.setDadosAcademicos(new DadosAcademicos());
        estagiarioEntidade.setEndereco(new Endereco());
    }

    @Test
    void testSalvar_Sucesso() {
        when(estagiarioRepository.findByCpf(anyString())).thenReturn(Optional.empty());
        when(estagiarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(empresaRepository.findByCodigoEmpresa(anyString())).thenReturn(Optional.of(empresaEntidade));
        when(faculdadeRepository.findByCnpj(anyString())).thenReturn(Optional.of(faculdadeEntidade));
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_senha123");
        when(estagiarioRepository.save(any(Estagiario.class))).thenReturn(estagiarioEntidade);

        EstagiarioResponseDTO resultado = estagiarioService.salvar(estagiarioDTO);

        assertNotNull(resultado);
        assertEquals("estagiario@teste.com", resultado.getEmail());
        assertEquals("12345678900", resultado.getCpf());

        verify(empresaRepository, times(1)).findByCodigoEmpresa("A1B-C2D");
        verify(faculdadeRepository, times(1)).findByCnpj("22222222000122");
        verify(estagiarioRepository, times(1)).save(any(Estagiario.class));
    }

    @Test
    void testSalvar_Falha_CodigoEmpresaInvalido() {
        when(estagiarioRepository.findByCpf(anyString())).thenReturn(Optional.empty());
        when(estagiarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(empresaRepository.findByCodigoEmpresa(anyString())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            estagiarioService.salvar(estagiarioDTO);
        }, "Empresa não encontrada com o código: A1B-C2D");

        verify(faculdadeRepository, never()).findByCnpj(anyString());
        verify(estagiarioRepository, never()).save(any(Estagiario.class));
    }

    @Test
    void testSalvar_Falha_FaculdadeCnpjInvalido() {
        when(estagiarioRepository.findByCpf(anyString())).thenReturn(Optional.empty());
        when(estagiarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(empresaRepository.findByCodigoEmpresa(anyString())).thenReturn(Optional.of(empresaEntidade));
        when(faculdadeRepository.findByCnpj(anyString())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            estagiarioService.salvar(estagiarioDTO);
        }, "Faculdade não encontrada com CNPJ: 22222222000122");

        verify(estagiarioRepository, never()).save(any(Estagiario.class));
    }
}