package br.edu.utfpr.integration;

import br.edu.utfpr.model.Canteiro;
import br.edu.utfpr.model.Trabalhador;
import br.edu.utfpr.service.CanteiroService;
import br.edu.utfpr.service.TrabalhadorService;
import br.edu.utfpr.util.JPAUtil;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TrabalhadorCanteiroIntegrationTest {

    private EntityManager em;
    private TrabalhadorService trabalhadorService;
    private CanteiroService canteiroService;

    @BeforeEach
    public void setUp() {
        em = JPAUtil.getEntityManager();
        em.getTransaction().begin();
        trabalhadorService = new TrabalhadorService(em);
        canteiroService = new CanteiroService(em);
    }

    @AfterEach
    public void tearDown() {
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
        em.close();
    }

    @Test
    public void testAssociarTrabalhadorAoCanteiroEVerificarPersistencia() {
        Canteiro canteiro = new Canteiro();
        canteiro.setNome("Obra UTFPR Bloco C");
        canteiro.setLocalizacao("Pato Branco");
        canteiro.setResponsavel("Prof Andreia");
        canteiro.setDataInicio(LocalDate.now());
        canteiro.setDataPrevisaoTermino(LocalDate.now().plusMonths(6));

        canteiroService.inserir(canteiro);

        String cpfDinamico = String.valueOf(System.currentTimeMillis()).substring(2, 13);

        Trabalhador trabalhador = new Trabalhador();
        trabalhador.setNomeCompleto("Matheus C. P. Santos");
        trabalhador.setCpf(cpfDinamico);
        trabalhador.setFuncao("Desenvolvedor");
        trabalhador.setDataContratacao(LocalDate.now());
        trabalhador.setTipoContrato("Estagio");
        trabalhador.setCanteiroAtual(canteiro);

        trabalhadorService.inserir(trabalhador);

        em.flush();
        em.clear();

        Trabalhador trabalhadorRecuperado = trabalhadorService.buscarPorId(trabalhador.getId());

        assertNotNull(trabalhadorRecuperado);
        assertNotNull(trabalhadorRecuperado.getCanteiroAtual());
        assertEquals("Obra UTFPR Bloco C", trabalhadorRecuperado.getCanteiroAtual().getNome());
        assertEquals(canteiro.getId(), trabalhadorRecuperado.getCanteiroAtual().getId());
    }
}