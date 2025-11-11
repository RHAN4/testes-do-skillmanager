package com.senai.skillmanager.service;

import com.senai.skillmanager.dto.EmpresaDTO;
import com.senai.skillmanager.dto.EmpresaResponseDTO;
import com.senai.skillmanager.dto.EnderecoDTO;
import com.senai.skillmanager.model.Endereco;
import com.senai.skillmanager.model.empresa.Empresa;
import com.senai.skillmanager.model.empresa.TipoEmpresa;
import com.senai.skillmanager.repository.EmpresaRepository;
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
public class EmpresaServiceTest {

    @InjectMocks
    private EmpresaService empresaService;

    @Mock
    private EmpresaRepository empresaRepository;

    private EmpresaDTO empresaDTO;
    private Empresa empresaEntidade;

    @BeforeEach
    void setUp() {
        EnderecoDTO enderecoDTO = new EnderecoDTO();
        enderecoDTO.setCep("12345678");

        empresaDTO = new EmpresaDTO();
        empresaDTO.setNome("Empresa Teste");
        empresaDTO.setRazaoSocial("Empresa Razão Social");
        empresaDTO.setCnpj("11111111000111");
        empresaDTO.setTipoEmpresa(TipoEmpresa.SERVICO);
        empresaDTO.setInscricaoMunicipal("123456");
        empresaDTO.setEndereco(enderecoDTO);

        empresaEntidade = new Empresa();
        empresaEntidade.setId(1L);
        empresaEntidade.setNome("Empresa Teste");
        empresaEntidade.setCnpj("11111111000111");
        empresaEntidade.setCodigoEmpresa("MOCK-CODE");
        empresaEntidade.setTipoEmpresa(TipoEmpresa.SERVICO);
        empresaEntidade.setEndereco(new Endereco());
    }

    @Test
    void testSalvar_Sucesso_GeraCodigo() {
        when(empresaRepository.findByCnpj(anyString())).thenReturn(Optional.empty());
        when(empresaRepository.findByCodigoEmpresa(anyString())).thenReturn(Optional.empty());
        when(empresaRepository.save(any(Empresa.class))).thenAnswer(invocation -> {
            Empresa empresaSalva = invocation.getArgument(0);
            empresaSalva.setId(1L);
            assertNotNull(empresaSalva.getCodigoEmpresa());
            assertTrue(empresaSalva.getCodigoEmpresa().matches("[A-Z0-9]{3}-[A-Z0-9]{3}"));
            return empresaSalva;
        });

        EmpresaResponseDTO resultado = empresaService.salvar(empresaDTO);

        assertNotNull(resultado);
        assertNotNull(resultado.getCodigoEmpresa());
        assertEquals("Empresa Teste", resultado.getNome());

        verify(empresaRepository, times(1)).findByCnpj("11111111000111");
        verify(empresaRepository, times(1)).save(any(Empresa.class));
    }

    @Test
    void testSalvar_Falha_CnpjDuplicado() {
        when(empresaRepository.findByCnpj(anyString())).thenReturn(Optional.of(empresaEntidade));

        assertThrows(RuntimeException.class, () -> {
            empresaService.salvar(empresaDTO);
        }, "Empresa já cadastrada com este CNPJ.");

        verify(empresaRepository, never()).save(any(Empresa.class));
    }

    @Test
    void testSalvar_Falha_ValidacaoInscricao() {
        empresaDTO.setTipoEmpresa(TipoEmpresa.COMERCIO);
        empresaDTO.setInscricaoEstadual(null);

        when(empresaRepository.findByCnpj(anyString())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            empresaService.salvar(empresaDTO);
        }, "Empresas de Comércio devem possuir Inscrição Estadual.");

        verify(empresaRepository, never()).save(any(Empresa.class));
    }
}