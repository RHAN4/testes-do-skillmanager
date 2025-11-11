package com.senai.skillmanager.service;

import com.senai.skillmanager.dto.*;
import com.senai.skillmanager.model.Endereco;
import com.senai.skillmanager.model.empresa.Cargo;
import com.senai.skillmanager.model.empresa.Empresa;
import com.senai.skillmanager.model.empresa.Supervisor;
import com.senai.skillmanager.model.empresa.TipoEmpresa;
import com.senai.skillmanager.repository.EmpresaRepository;
import com.senai.skillmanager.repository.SupervisorRepository;
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
public class SupervisorServiceTest {

    @InjectMocks
    private SupervisorService supervisorService;

    @Mock
    private SupervisorRepository supervisorRepository;

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmpresaService empresaService;

    @Mock
    private Authentication authentication;

    private SupervisorDTO supervisorDTO;
    private Supervisor supervisorEntidade;
    private Empresa empresaEntidade;
    private EmpresaResponseDTO empresaResponseDTO;
    private EnderecoDTO enderecoDTO;

    @BeforeEach
    void setUp() {
        enderecoDTO = new EnderecoDTO();
        enderecoDTO.setCep("12345678");
        enderecoDTO.setLogradouro("Rua Teste");

        Endereco endereco = new Endereco();
        endereco.setId(1L);
        endereco.setCep("12345678");
        endereco.setLogradouro("Rua Teste");

        supervisorDTO = new SupervisorDTO();
        supervisorDTO.setNome("Supervisor Teste");
        supervisorDTO.setEmail("teste@empresa.com");
        supervisorDTO.setSenha("senha123");
        supervisorDTO.setCargo(Cargo.SUPERVISOR);
        supervisorDTO.setEmpresaCnpj("11111111000111");
        supervisorDTO.setEmpresaNome("Nova Empresa Teste");
        supervisorDTO.setEmpresaRazaoSocial("Nova Empresa Razão Social");
        supervisorDTO.setEmpresaTipo(TipoEmpresa.SERVICO);
        supervisorDTO.setEmpresaInscricaoMunicipal("123456");
        supervisorDTO.setEmpresaEndereco(enderecoDTO);

        empresaEntidade = new Empresa();
        empresaEntidade.setId(1L);
        empresaEntidade.setNome("Nova Empresa Teste");
        empresaEntidade.setCnpj("11111111000111");
        empresaEntidade.setCodigoEmpresa("A1B-C2D");
        empresaEntidade.setEndereco(endereco);

        supervisorEntidade = new Supervisor();
        supervisorEntidade.setId(1L);
        supervisorEntidade.setNome("Supervisor Teste");
        supervisorEntidade.setEmail("teste@empresa.com");
        supervisorEntidade.setSenha("hashed_senha123");
        supervisorEntidade.setCargo(Cargo.SUPERVISOR);
        supervisorEntidade.setEmpresa(empresaEntidade);

        empresaResponseDTO = new EmpresaResponseDTO();
        empresaResponseDTO.setId(1L);
        empresaResponseDTO.setNome("Nova Empresa Teste");
        empresaResponseDTO.setCnpj("11111111000111");
        empresaResponseDTO.setCodigoEmpresa("A1B-C2D");
    }

    @Test
    void testSalvar_FluxoA_NovaEmpresa() {
        when(supervisorRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(empresaRepository.findByCnpj(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_senha123");
        when(empresaService.salvar(any(EmpresaDTO.class))).thenReturn(empresaResponseDTO);
        when(empresaService.buscarEntidadePorId(anyLong())).thenReturn(empresaEntidade);
        when(supervisorRepository.save(any(Supervisor.class))).thenReturn(supervisorEntidade);

        SupervisorResponseDTO resultado = supervisorService.salvar(supervisorDTO);

        assertNotNull(resultado);
        assertEquals("Supervisor Teste", resultado.getNome());
        assertEquals("teste@empresa.com", resultado.getEmail());
        assertNotNull(resultado.getEmpresa());
        assertEquals("Nova Empresa Teste", resultado.getEmpresa().getNome());
        assertEquals("A1B-C2D", resultado.getEmpresa().getCodigoEmpresa());

        verify(supervisorRepository, times(1)).findByEmail("teste@empresa.com");
        verify(empresaRepository, times(1)).findByCnpj("11111111000111");
        verify(empresaService, times(1)).salvar(any(EmpresaDTO.class));
        verify(supervisorRepository, times(1)).save(any(Supervisor.class));
    }

    @Test
    void testSalvar_FluxoB_EmpresaExistente() {
        supervisorDTO.setEmpresaNome(null);
        supervisorDTO.setEmpresaEndereco(null);

        when(supervisorRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(empresaRepository.findByCnpj(anyString())).thenReturn(Optional.of(empresaEntidade));
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_senha123");
        when(supervisorRepository.save(any(Supervisor.class))).thenReturn(supervisorEntidade);

        SupervisorResponseDTO resultado = supervisorService.salvar(supervisorDTO);

        assertNotNull(resultado);
        assertEquals("teste@empresa.com", resultado.getEmail());
        assertEquals(1L, resultado.getEmpresa().getId());

        verify(empresaRepository, times(1)).findByCnpj("11111111000111");
        verify(empresaService, never()).salvar(any(EmpresaDTO.class));
        verify(supervisorRepository, times(1)).save(any(Supervisor.class));
    }

    @Test
    void testSalvar_Falha_EmailJaCadastrado() {
        when(supervisorRepository.findByEmail(anyString())).thenReturn(Optional.of(supervisorEntidade));

        assertThrows(RuntimeException.class, () -> {
            supervisorService.salvar(supervisorDTO);
        }, "Email já cadastrado.");

        verify(empresaRepository, never()).findByCnpj(anyString());
        verify(supervisorRepository, never()).save(any(Supervisor.class));
    }

    @Test
    void testBuscarPorId_Sucesso_Proprietario() {
        when(supervisorRepository.findById(1L)).thenReturn(Optional.of(supervisorEntidade));
        when(authentication.getName()).thenReturn("teste@empresa.com");
        when(authentication.getAuthorities()).thenReturn(Collections.singleton(new SimpleGrantedAuthority("ROLE_SUPERVISOR")));

        SupervisorResponseDTO resultado = supervisorService.buscarPorId(1L, authentication);

        assertNotNull(resultado);
        assertEquals("teste@empresa.com", resultado.getEmail());
    }

    @Test
    void testBuscarPorId_Sucesso_Admin() {
        when(supervisorRepository.findById(1L)).thenReturn(Optional.of(supervisorEntidade));
        when(authentication.getAuthorities()).thenReturn(Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN")));

        SupervisorResponseDTO resultado = supervisorService.buscarPorId(1L, authentication);

        assertNotNull(resultado);
        assertEquals("teste@empresa.com", resultado.getEmail());
    }

    @Test
    void testBuscarPorId_Falha_NaoProprietario() {
        Supervisor outroSupervisor = new Supervisor();
        outroSupervisor.setId(2L);
        outroSupervisor.setEmail("outro@email.com");

        when(supervisorRepository.findById(2L)).thenReturn(Optional.of(outroSupervisor));
        when(authentication.getName()).thenReturn("teste@empresa.com");
        when(authentication.getAuthorities()).thenReturn(Collections.singleton(new SimpleGrantedAuthority("ROLE_SUPERVISOR")));

        assertThrows(SecurityException.class, () -> {
            supervisorService.buscarPorId(2L, authentication);
        }, "Acesso negado. Você não tem permissão para acessar este recurso.");
    }
}