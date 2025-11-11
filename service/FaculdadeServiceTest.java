package com.senai.skillmanager.service;

import com.senai.skillmanager.dto.EnderecoDTO;
import com.senai.skillmanager.dto.FaculdadeDTO;
import com.senai.skillmanager.dto.FaculdadeResponseDTO;
import com.senai.skillmanager.model.Endereco;
import com.senai.skillmanager.model.faculdade.Faculdade;
import com.senai.skillmanager.repository.FaculdadeRepository;
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
public class FaculdadeServiceTest {

    @InjectMocks
    private FaculdadeService faculdadeService;

    @Mock
    private FaculdadeRepository faculdadeRepository;

    private FaculdadeDTO faculdadeDTO;
    private Faculdade faculdadeEntidade;

    @BeforeEach
    void setUp() {
        EnderecoDTO enderecoDTO = new EnderecoDTO();
        enderecoDTO.setCep("12345678");

        faculdadeDTO = new FaculdadeDTO();
        faculdadeDTO.setNome("Faculdade Teste");
        faculdadeDTO.setCnpj("22222222000122");
        faculdadeDTO.setTelefone("11987654321");
        faculdadeDTO.setEndereco(enderecoDTO);

        faculdadeEntidade = new Faculdade();
        faculdadeEntidade.setId(1L);
        faculdadeEntidade.setNome("Faculdade Teste");
        faculdadeEntidade.setCnpj("22222222000122");
        faculdadeEntidade.setEndereco(new Endereco());
    }

    @Test
    void testSalvar_Sucesso() {
        when(faculdadeRepository.findByCnpj(anyString())).thenReturn(Optional.empty());
        when(faculdadeRepository.save(any(Faculdade.class))).thenReturn(faculdadeEntidade);

        FaculdadeResponseDTO resultado = faculdadeService.salvar(faculdadeDTO);

        assertNotNull(resultado);
        assertEquals("Faculdade Teste", resultado.getNome());
        assertEquals("22222222000122", resultado.getCnpj());

        verify(faculdadeRepository, times(1)).findByCnpj("22222222000122");
        verify(faculdadeRepository, times(1)).save(any(Faculdade.class));
    }

    @Test
    void testSalvar_Falha_CnpjDuplicado() {
        when(faculdadeRepository.findByCnpj(anyString())).thenReturn(Optional.of(faculdadeEntidade));

        assertThrows(RuntimeException.class, () -> {
            faculdadeService.salvar(faculdadeDTO);
        }, "CNPJ já cadastrado.");

        verify(faculdadeRepository, never()).save(any(Faculdade.class));
    }
}