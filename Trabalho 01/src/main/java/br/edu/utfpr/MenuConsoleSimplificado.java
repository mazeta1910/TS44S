package br.edu.utfpr;

import br.edu.utfpr.model.*;
import br.edu.utfpr.service.*;
import br.edu.utfpr.util.*;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class MenuConsoleSimplificado {

    private static final Scanner scanner = new Scanner(System.in);
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static void main(String[] args) {
        EntityManager em = JPAUtil.getEntityManager();

        TrabalhadorService trabalhadorService = new TrabalhadorService(em);
        CanteiroService canteiroService = new CanteiroService(em);
        EPIService epiService = new EPIService(em);

        boolean continuar = true;

        System.out.println("\n" + "=".repeat(60));
        System.out.println("     SISTEMA DE GESTÃO DE TRABALHO DECENTE");
        System.out.println("     Objetivo de Desenvolvimento Sustentável 8 (ODS 8)");
        System.out.println("=".repeat(60));

        while (continuar) {
            System.out.println("\n" + "=".repeat(60));
            System.out.println("                    MENU PRINCIPAL");
            System.out.println("=".repeat(60));
            System.out.println("1. 👷 Gerenciar Trabalhadores");
            System.out.println("2. 🏗️ Gerenciar Canteiros");
            System.out.println("3. 🦺 Gerenciar EPIs");
            System.out.println("4. 📊 Relatórios");
            System.out.println("5. 💾 Exportar Dados");
            System.out.println("6. 🔄 Backup e Restauração");
            System.out.println("0. 🚪 Sair");
            System.out.println("=".repeat(60));
            System.out.print("Escolha uma opção: ");

            int opcao = lerOpcao();

            switch (opcao) {
                case 1:
                    menuTrabalhadores(trabalhadorService, canteiroService);
                    break;
                case 2:
                    menuCanteiros(canteiroService);
                    break;
                case 3:
                    menuEPIs(epiService, trabalhadorService);
                    break;
                case 4:
                    menuRelatorios(trabalhadorService, canteiroService, epiService);
                    break;
                case 5:
                    menuExportacao(trabalhadorService, canteiroService, epiService);
                    break;
                case 6:
                    menuBackup(trabalhadorService);
                    break;
                case 0:
                    continuar = false;
                    System.out.println("\n👋 Encerrando sistema...");
                    break;
                default:
                    System.out.println("\n❌ Opção inválida!");
            }
        }

        em.close();
        scanner.close();
    }

    private static void menuTrabalhadores(TrabalhadorService service, CanteiroService cService) {
        while (true) {
            System.out.println("\n" + "-".repeat(60));
            System.out.println("           GERENCIAR TRABALHADORES");
            System.out.println("-".repeat(60));
            System.out.println("1. Cadastrar Trabalhador");
            System.out.println("2. Listar Todos");
            System.out.println("3. Buscar por ID");
            System.out.println("4. Atualizar Trabalhador");
            System.out.println("5. Remover Trabalhador");
            System.out.println("6. Exemplos de Streams API");
            System.out.println("0. Voltar");
            System.out.println("-".repeat(60));
            System.out.print("Escolha uma opção: ");

            int opcao = lerOpcao();

            switch (opcao) {
                case 1:
                    cadastrarTrabalhador(service, cService);
                    break;
                case 2:
                    listarTrabalhadores(service);
                    break;
                case 3:
                    buscarTrabalhadorPorId(service);
                    break;
                case 4:
                    atualizarTrabalhador(service, cService);
                    break;
                case 5:
                    removerTrabalhador(service);
                    break;
                case 6:
                    exemploStreams(service);
                    break;
                case 0:
                    return;
                default:
                    System.out.println("\n❌ Opção inválida!");
            }
        }
    }

    private static void cadastrarTrabalhador(TrabalhadorService service, CanteiroService cService) {
        System.out.println("\n" + "-".repeat(60));
        System.out.println("           CADASTRAR TRABALHADOR");
        System.out.println("-".repeat(60));

        Trabalhador novo = new Trabalhador();

        System.out.print("Nome Completo: ");
        novo.setNomeCompleto(scanner.nextLine());

        String cpf;
        while (true) {
            System.out.print("CPF (apenas números): ");
            cpf = scanner.nextLine();
            if (ValidacaoUtil.validarCPF(cpf)) {
                novo.setCpf(cpf);
                break;
            } else {
                System.out.println("❌ CPF inválido! Tente novamente.");
            }
        }

        System.out.print("Função: ");
        String funcao = scanner.nextLine();
        novo.setFuncao(funcao);

        if (ValidacaoUtil.isFuncaoRequerCREA(funcao)) {
            System.out.print("Número CREA: ");
            novo.setNumeroCREA(scanner.nextLine());
            System.out.print("Especialidade: ");
            novo.setEspecialidade(scanner.nextLine());
        } else if (ValidacaoUtil.isFuncaoRequerRegistro(funcao)) {
            System.out.print("Número de Registro Profissional: ");
            novo.setNumeroRegistroProfissional(scanner.nextLine());
        }

        novo.setDataContratacao(LocalDate.now());

        System.out.print("Tipo de Contrato (CLT/PJ/TEMPORARIO): ");
        novo.setTipoContrato(scanner.nextLine());

        System.out.print("Associar a canteiro? (S/N): ");
        if (scanner.nextLine().equalsIgnoreCase("S")) {
            List<Canteiro> canteiros = cService.buscarTodos();
            if (!canteiros.isEmpty()) {
                System.out.println("\nCanteiros disponíveis:");
                canteiros.forEach(c -> System.out.println(c.getId() + " - " + c.getNome()));
                System.out.print("ID do canteiro: ");
                Long canteiroId = lerLong();
                Canteiro canteiro = cService.buscarPorId(canteiroId);
                if (canteiro != null) {
                    novo.setCanteiroAtual(canteiro);
                }
            }
        }

        service.inserir(novo);
        System.out.println("\n✅ Trabalhador cadastrado com sucesso! ID: " + novo.getId());
    }

    private static void listarTrabalhadores(TrabalhadorService service) {
        System.out.println("\n" + "-".repeat(60));
        System.out.println("           LISTA DE TRABALHADORES");
        System.out.println("-".repeat(60));

        List<Trabalhador> todos = service.buscarTodos();
        if (todos.isEmpty()) {
            System.out.println("⚠️  Nenhum trabalhador cadastrado.");
        } else {
            for (Trabalhador t : todos) {
                System.out.println("\nID: " + t.getId());
                System.out.println("Nome: " + t.getNomeCompleto());
                System.out.println("CPF: " + t.getCpf());
                System.out.println("Função: " + t.getFuncao());
                if (t.getNumeroCREA() != null) {
                    System.out.println("CREA: " + t.getNumeroCREA());
                }
                if (t.getNumeroRegistroProfissional() != null) {
                    System.out.println("Registro: " + t.getNumeroRegistroProfissional());
                }
                System.out.println("Contrato: " + t.getTipoContrato());
                if (t.getCanteiroAtual() != null) {
                    System.out.println("Canteiro: " + t.getCanteiroAtual().getNome());
                }
                System.out.println("-".repeat(60));
            }
            System.out.println("Total: " + todos.size() + " trabalhadores");
        }
    }

    private static void buscarTrabalhadorPorId(TrabalhadorService service) {
        System.out.print("\nDigite o ID: ");
        Long id = lerLong();
        Trabalhador t = service.buscarPorId(id);
        if (t != null) {
            System.out.println("\n" + t.toString());
        } else {
            System.out.println("\n❌ Trabalhador não encontrado!");
        }
    }

    private static void atualizarTrabalhador(TrabalhadorService service, CanteiroService cService) {
        System.out.print("\nID do trabalhador: ");
        Long id = lerLong();
        Trabalhador t = service.buscarPorId(id);

        if (t != null) {
            System.out.println("Trabalhador atual: " + t.getNomeCompleto());
            if (t.getCanteiroAtual() != null) {
                System.out.println("Canteiro atual: " + t.getCanteiroAtual().getNome());
            } else {
                System.out.println("Canteiro atual: Nenhum");
            }

            System.out.print("Nova função (vazio = manter): ");
            String funcao = scanner.nextLine();
            if (!funcao.trim().isEmpty()) {
                t.setFuncao(funcao);
            }

            System.out.print("Novo tipo de contrato (vazio = manter): ");
            String contrato = scanner.nextLine();
            if (!contrato.trim().isEmpty()) {
                t.setTipoContrato(contrato);
            }

            System.out.print("Alterar canteiro? (S/N): ");
            if (scanner.nextLine().equalsIgnoreCase("S")) {
                List<Canteiro> canteiros = cService.buscarTodos();
                if (!canteiros.isEmpty()) {
                    System.out.println("\nCanteiros disponíveis:");
                    System.out.println("0 - Remover de canteiro");
                    canteiros.forEach(c -> System.out.println(c.getId() + " - " + c.getNome()));
                    System.out.print("ID do canteiro (0 para remover): ");
                    Long canteiroId = lerLong();
                    if (canteiroId == 0) {
                        t.setCanteiroAtual(null);
                        System.out.println("✅ Trabalhador removido do canteiro");
                    } else {
                        Canteiro canteiro = cService.buscarPorId(canteiroId);
                        if (canteiro != null) {
                            t.setCanteiroAtual(canteiro);
                            System.out.println("✅ Canteiro alterado para: " + canteiro.getNome());
                        } else {
                            System.out.println("⚠️  Canteiro não encontrado, mantendo anterior");
                        }
                    }
                } else {
                    System.out.println("⚠️  Nenhum canteiro cadastrado");
                }
            }

            service.atualizar(t);
            System.out.println("\n✅ Trabalhador atualizado!");
        } else {
            System.out.println("\n❌ Trabalhador não encontrado!");
        }
    }

    private static void removerTrabalhador(TrabalhadorService service) {
        System.out.print("\nID do trabalhador: ");
        Long id = lerLong();
        Trabalhador t = service.buscarPorId(id);

        if (t != null) {
            System.out.print("Confirma remoção de " + t.getNomeCompleto() + "? (S/N): ");
            if (scanner.nextLine().equalsIgnoreCase("S")) {
                service.remover(id);
                System.out.println("\n✅ Trabalhador removido!");
            }
        } else {
            System.out.println("\n❌ Trabalhador não encontrado!");
        }
    }

    private static void exemploStreams(TrabalhadorService service) {
        System.out.println("\n" + "-".repeat(60));
        System.out.println("           EXEMPLOS DE STREAMS API");
        System.out.println("-".repeat(60));
        System.out.println("1. Filtrar por função");
        System.out.println("2. Buscar por CPF");
        System.out.println("3. Contar por tipo de contrato");
        System.out.println("4. Listar apenas nomes");
        System.out.println("5. Calcular estatísticas");
        System.out.println("0. Voltar");
        System.out.println("-".repeat(60));
        System.out.print("Escolha: ");

        int opcao = lerOpcao();

        switch (opcao) {
            case 1:
                System.out.print("\nFunção: ");
                String funcao = scanner.nextLine();
                service.filtrarPorFuncao(funcao).forEach(t ->
                        System.out.println("• " + t.getNomeCompleto() + " - " + t.getFuncao())
                );
                break;
            case 2:
                System.out.print("\nCPF: ");
                String cpf = scanner.nextLine();
                service.buscarPorCPF(cpf).ifPresentOrElse(
                        t -> System.out.println("✅ Encontrado: " + t.getNomeCompleto()),
                        () -> System.out.println("❌ Não encontrado")
                );
                break;
            case 3:
                System.out.println("\nContagem por tipo de contrato:");
                service.contarPorTipoContrato().forEach((tipo, count) ->
                        System.out.println("• " + tipo + ": " + count)
                );
                break;
            case 4:
                System.out.println("\nNomes dos trabalhadores:");
                service.listarNomes().forEach(nome -> System.out.println("• " + nome));
                break;
            case 5:
                System.out.println("\nEstatísticas:");
                System.out.println("Total: " + service.buscarTodos().size());
                System.out.println("Com CREA: " + service.buscarTodos().stream()
                        .filter(t -> t.getNumeroCREA() != null).count());
                break;
        }
    }

    private static void menuCanteiros(CanteiroService service) {
        while (true) {
            System.out.println("\n" + "-".repeat(60));
            System.out.println("           GERENCIAR CANTEIROS");
            System.out.println("-".repeat(60));
            System.out.println("1. Cadastrar Canteiro");
            System.out.println("2. Listar Todos");
            System.out.println("3. Buscar por ID");
            System.out.println("4. Atualizar Canteiro");
            System.out.println("5. Remover Canteiro");
            System.out.println("0. Voltar");
            System.out.println("-".repeat(60));
            System.out.print("Escolha uma opção: ");

            int opcao = lerOpcao();

            switch (opcao) {
                case 1:
                    cadastrarCanteiro(service);
                    break;
                case 2:
                    listarCanteiros(service);
                    break;
                case 3:
                    buscarCanteiroPorId(service);
                    break;
                case 4:
                    atualizarCanteiro(service);
                    break;
                case 5:
                    removerCanteiro(service);
                    break;
                case 0:
                    return;
                default:
                    System.out.println("\n❌ Opção inválida!");
            }
        }
    }

    private static void cadastrarCanteiro(CanteiroService service) {
        System.out.println("\n" + "-".repeat(60));
        System.out.println("           CADASTRAR CANTEIRO");
        System.out.println("-".repeat(60));

        Canteiro novo = new Canteiro();

        System.out.print("Nome do Canteiro: ");
        novo.setNome(scanner.nextLine());

        System.out.print("Localização: ");
        novo.setLocalizacao(scanner.nextLine());

        System.out.print("Responsável: ");
        novo.setResponsavel(scanner.nextLine());

        novo.setDataInicio(LocalDate.now());

        System.out.print("Data Previsão Término (dd/MM/yyyy): ");
        try {
            LocalDate dataTermino = LocalDate.parse(scanner.nextLine(), dateFormatter);
            novo.setDataPrevisaoTermino(dataTermino);
        } catch (DateTimeParseException e) {
            System.out.println("⚠️  Data inválida, usando 6 meses a partir de hoje.");
            novo.setDataPrevisaoTermino(LocalDate.now().plusMonths(6));
        }

        service.inserir(novo);
        System.out.println("\n✅ Canteiro cadastrado! ID: " + novo.getId());
    }

    private static void listarCanteiros(CanteiroService service) {
        System.out.println("\n" + "-".repeat(60));
        System.out.println("           LISTA DE CANTEIROS");
        System.out.println("-".repeat(60));

        List<Canteiro> todos = service.buscarTodos();
        if (todos.isEmpty()) {
            System.out.println("⚠️  Nenhum canteiro cadastrado.");
        } else {
            for (Canteiro c : todos) {
                System.out.println("\nID: " + c.getId());
                System.out.println("Nome: " + c.getNome());
                System.out.println("Localização: " + c.getLocalizacao());
                System.out.println("Responsável: " + c.getResponsavel());
                System.out.println("Início: " + c.getDataInicio().format(dateFormatter));
                System.out.println("Previsão Término: " + c.getDataPrevisaoTermino().format(dateFormatter));
                System.out.println("-".repeat(60));
            }
            System.out.println("Total: " + todos.size() + " canteiros");
        }
    }

    private static void buscarCanteiroPorId(CanteiroService service) {
        System.out.print("\nDigite o ID: ");
        Long id = lerLong();
        Canteiro c = service.buscarPorId(id);
        if (c != null) {
            System.out.println("\n" + c.toString());
        } else {
            System.out.println("\n❌ Canteiro não encontrado!");
        }
    }

    private static void atualizarCanteiro(CanteiroService service) {
        System.out.print("\nID do canteiro: ");
        Long id = lerLong();
        Canteiro c = service.buscarPorId(id);

        if (c != null) {
            System.out.println("Canteiro atual: " + c.getNome());
            System.out.print("Novo responsável (vazio = manter): ");
            String resp = scanner.nextLine();
            if (!resp.trim().isEmpty()) {
                c.setResponsavel(resp);
            }

            service.atualizar(c);
            System.out.println("\n✅ Canteiro atualizado!");
        } else {
            System.out.println("\n❌ Canteiro não encontrado!");
        }
    }

    private static void removerCanteiro(CanteiroService service) {
        System.out.print("\nID do canteiro: ");
        Long id = lerLong();
        Canteiro c = service.buscarPorId(id);

        if (c != null) {
            System.out.print("Confirma remoção de " + c.getNome() + "? (S/N): ");
            if (scanner.nextLine().equalsIgnoreCase("S")) {
                service.remover(id);
                System.out.println("\n✅ Canteiro removido!");
            }
        } else {
            System.out.println("\n❌ Canteiro não encontrado!");
        }
    }

    private static void menuEPIs(EPIService service, TrabalhadorService tService) {
        while (true) {
            System.out.println("\n" + "-".repeat(60));
            System.out.println("           GERENCIAR EPIs");
            System.out.println("-".repeat(60));
            System.out.println("1. Cadastrar EPI");
            System.out.println("2. Listar Todos");
            System.out.println("3. Buscar por ID");
            System.out.println("4. Atualizar EPI");
            System.out.println("5. Remover EPI");
            System.out.println("0. Voltar");
            System.out.println("-".repeat(60));
            System.out.print("Escolha uma opção: ");

            int opcao = lerOpcao();

            switch (opcao) {
                case 1:
                    cadastrarEPI(service, tService);
                    break;
                case 2:
                    listarEPIs(service);
                    break;
                case 3:
                    buscarEPIPorId(service);
                    break;
                case 4:
                    atualizarEPI(service);
                    break;
                case 5:
                    removerEPI(service);
                    break;
                case 0:
                    return;
                default:
                    System.out.println("\n❌ Opção inválida!");
            }
        }
    }

    private static void cadastrarEPI(EPIService service, TrabalhadorService tService) {
        System.out.println("\n" + "-".repeat(60));
        System.out.println("           CADASTRAR EPI");
        System.out.println("-".repeat(60));

        EPI novo = new EPI();

        System.out.print("Tipo (Capacete/Luva/Bota/Óculos/etc): ");
        novo.setTipo(scanner.nextLine());

        System.out.print("Número CA: ");
        novo.setNumeroCA(scanner.nextLine());

        novo.setDataEntrega(LocalDate.now());

        System.out.print("Data Validade (dd/MM/yyyy): ");
        try {
            LocalDate validade = LocalDate.parse(scanner.nextLine(), dateFormatter);
            novo.setDataValidade(validade);
        } catch (DateTimeParseException e) {
            System.out.println("⚠️  Data inválida, usando 1 ano a partir de hoje.");
            novo.setDataValidade(LocalDate.now().plusYears(1));
        }

        System.out.print("Associar a trabalhador? (S/N): ");
        if (scanner.nextLine().equalsIgnoreCase("S")) {
            List<Trabalhador> trabalhadores = tService.buscarTodos();
            if (!trabalhadores.isEmpty()) {
                System.out.println("\nTrabalhadores:");
                trabalhadores.forEach(t -> System.out.println(t.getId() + " - " + t.getNomeCompleto()));
                System.out.print("ID: ");
                Long tid = lerLong();
                Trabalhador t = tService.buscarPorId(tid);
                if (t != null) {
                    novo.setTrabalhador(t);
                }
            }
        }

        service.inserir(novo);
        System.out.println("\n✅ EPI cadastrado! ID: " + novo.getId());
    }

    private static void listarEPIs(EPIService service) {
        System.out.println("\n" + "-".repeat(60));
        System.out.println("           LISTA DE EPIs");
        System.out.println("-".repeat(60));

        List<EPI> todos = service.buscarTodos();
        if (todos.isEmpty()) {
            System.out.println("⚠️  Nenhum EPI cadastrado.");
        } else {
            for (EPI e : todos) {
                System.out.println("\nID: " + e.getId());
                System.out.println("Tipo: " + e.getTipo());
                System.out.println("CA: " + e.getNumeroCA());
                System.out.println("Entrega: " + e.getDataEntrega().format(dateFormatter));
                System.out.println("Validade: " + e.getDataValidade().format(dateFormatter));
                if (e.getDataValidade().isBefore(LocalDate.now())) {
                    System.out.println("⚠️  VENCIDO!");
                }
                if (e.getTrabalhador() != null) {
                    System.out.println("Trabalhador: " + e.getTrabalhador().getNomeCompleto());
                }
                System.out.println("-".repeat(60));
            }
            System.out.println("Total: " + todos.size() + " EPIs");
        }
    }

    private static void buscarEPIPorId(EPIService service) {
        System.out.print("\nDigite o ID: ");
        Long id = lerLong();
        EPI e = service.buscarPorId(id);
        if (e != null) {
            System.out.println("\n" + e.toString());
        } else {
            System.out.println("\n❌ EPI não encontrado!");
        }
    }

    private static void atualizarEPI(EPIService service) {
        System.out.print("\nID do EPI: ");
        Long id = lerLong();
        EPI e = service.buscarPorId(id);

        if (e != null) {
            System.out.println("EPI atual: " + e.getTipo());
            System.out.print("Nova validade (dd/MM/yyyy, vazio = manter): ");
            String data = scanner.nextLine();
            if (!data.trim().isEmpty()) {
                try {
                    e.setDataValidade(LocalDate.parse(data, dateFormatter));
                } catch (DateTimeParseException ex) {
                    System.out.println("⚠️  Data inválida, mantendo anterior.");
                }
            }

            service.atualizar(e);
            System.out.println("\n✅ EPI atualizado!");
        } else {
            System.out.println("\n❌ EPI não encontrado!");
        }
    }

    private static void removerEPI(EPIService service) {
        System.out.print("\nID do EPI: ");
        Long id = lerLong();
        EPI e = service.buscarPorId(id);

        if (e != null) {
            System.out.print("Confirma remoção do EPI " + e.getTipo() + "? (S/N): ");
            if (scanner.nextLine().equalsIgnoreCase("S")) {
                service.remover(id);
                System.out.println("\n✅ EPI removido!");
            }
        } else {
            System.out.println("\n❌ EPI não encontrado!");
        }
    }

    private static void menuRelatorios(TrabalhadorService tService, CanteiroService cService, EPIService eService) {
        System.out.println("\n" + "-".repeat(60));
        System.out.println("           RELATÓRIOS");
        System.out.println("-".repeat(60));
        System.out.println("1. Trabalhadores por Canteiro");
        System.out.println("2. EPIs Vencidos");
        System.out.println("3. Estatísticas Gerais");
        System.out.println("0. Voltar");
        System.out.println("-".repeat(60));
        System.out.print("Escolha: ");

        int opcao = lerOpcao();

        switch (opcao) {
            case 1:
                relatorioTrabalhadoresPorCanteiro(tService);
                break;
            case 2:
                relatorioEPIsVencidos(eService);
                break;
            case 3:
                relatorioEstatisticas(tService, cService, eService);
                break;
        }
    }

    private static void relatorioTrabalhadoresPorCanteiro(TrabalhadorService service) {
        System.out.println("\n📊 TRABALHADORES POR CANTEIRO");
        System.out.println("-".repeat(60));

        service.buscarTodos().stream()
                .filter(t -> t.getCanteiroAtual() != null)
                .collect(java.util.stream.Collectors.groupingBy(
                        t -> t.getCanteiroAtual().getNome(),
                        java.util.stream.Collectors.toList()
                ))
                .forEach((canteiro, trabalhadores) -> {
                    System.out.println("\n🏗️  " + canteiro + " (" + trabalhadores.size() + " trabalhadores)");
                    trabalhadores.forEach(t -> System.out.println("  • " + t.getNomeCompleto() + " - " + t.getFuncao()));
                });
    }

    private static void relatorioEPIsVencidos(EPIService service) {
        System.out.println("\n📊 EPIs VENCIDOS");
        System.out.println("-".repeat(60));

        List<EPI> vencidos = service.buscarTodos().stream()
                .filter(e -> e.getDataValidade().isBefore(LocalDate.now()))
                .toList();

        if (vencidos.isEmpty()) {
            System.out.println("✅ Nenhum EPI vencido!");
        } else {
            vencidos.forEach(e -> {
                System.out.println("\n⚠️  " + e.getTipo() + " (CA: " + e.getNumeroCA() + ")");
                System.out.println("   Vencido em: " + e.getDataValidade().format(dateFormatter));
                if (e.getTrabalhador() != null) {
                    System.out.println("   Trabalhador: " + e.getTrabalhador().getNomeCompleto());
                }
            });
            System.out.println("\nTotal: " + vencidos.size() + " EPIs vencidos");
        }
    }

    private static void relatorioEstatisticas(TrabalhadorService tService, CanteiroService cService, EPIService eService) {
        System.out.println("\n📊 ESTATÍSTICAS GERAIS");
        System.out.println("-".repeat(60));
        System.out.println("👷 Trabalhadores: " + tService.buscarTodos().size());
        System.out.println("🏗️  Canteiros: " + cService.buscarTodos().size());
        System.out.println("🦺 EPIs: " + eService.buscarTodos().size());

        long comCREA = tService.buscarTodos().stream()
                .filter(t -> t.getNumeroCREA() != null)
                .count();
        System.out.println("📋 Com CREA: " + comCREA);

        long episVencidos = eService.buscarTodos().stream()
                .filter(e -> e.getDataValidade().isBefore(LocalDate.now()))
                .count();
        System.out.println("⚠️  EPIs Vencidos: " + episVencidos);
    }

    private static void menuExportacao(TrabalhadorService tService, CanteiroService cService, EPIService eService) {
        System.out.println("\n" + "-".repeat(60));
        System.out.println("           EXPORTAR DADOS");
        System.out.println("-".repeat(60));
        System.out.println("1. Exportar Trabalhadores");
        System.out.println("2. Exportar Canteiros");
        System.out.println("3. Exportar EPIs");
        System.out.println("0. Voltar");
        System.out.println("-".repeat(60));
        System.out.print("Escolha: ");

        int opcao = lerOpcao();

        if (opcao >= 1 && opcao <= 3) {
            System.out.println("\nFormato:");
            System.out.println("1. TXT");
            System.out.println("2. JSON");
            System.out.println("3. BIN");
            System.out.print("Escolha: ");
            int formato = lerOpcao();

            System.out.print("\nNome do arquivo (sem extensão): ");
            String nome = scanner.nextLine();

            try {
                switch (opcao) {
                    case 1:
                        exportarDados(tService.buscarTodos(), nome, formato, "Trabalhadores");
                        break;
                    case 2:
                        exportarDados(cService.buscarTodos(), nome, formato, "Canteiros");
                        break;
                    case 3:
                        exportarDados(eService.buscarTodos(), nome, formato, "EPIs");
                        break;
                }
            } catch (Exception e) {
                System.out.println("❌ Erro ao exportar: " + e.getMessage());
            }
        }
    }

    private static void exportarDados(List<?> dados, String nome, int formato, String tipo) throws Exception {
        String caminho = "exports/" + nome;

        switch (formato) {
            case 1:
                ExportacaoUtil.exportarParaTxt(dados, caminho + ".txt", tipo);
                break;
            case 2:
                ExportacaoUtil.exportarParaJson(dados, caminho + ".json");
                break;
            case 3:
                ExportacaoUtil.exportarParaBin(dados, caminho + ".bin");
                break;
        }

        System.out.println("\n✅ Dados exportados com sucesso!");
        System.out.println("📁 Arquivo: " + caminho);
    }


    private static void menuBackup(TrabalhadorService service) {
        System.out.println("\n" + "-".repeat(60));
        System.out.println("           BACKUP E RESTAURAÇÃO");
        System.out.println("-".repeat(60));
        System.out.println("1. Fazer Backup");
        System.out.println("2. Restaurar Backup");
        System.out.println("0. Voltar");
        System.out.println("-".repeat(60));
        System.out.print("Escolha: ");

        int opcao = lerOpcao();

        switch (opcao) {
            case 1:
                try {
                    BackupUtil.fazerBackup(service.buscarTodos(), "backups/trabalhadores.bak");
                    System.out.println("\n✅ Backup realizado com sucesso!");
                } catch (Exception e) {
                    System.out.println("❌ Erro ao fazer backup: " + e.getMessage());
                }
                break;
            case 2:
                try {
                    List<Trabalhador> restaurados = BackupUtil.restaurarBackup("backups/trabalhadores.bak");
                    System.out.println("\n✅ Backup restaurado! " + restaurados.size() + " registros");
                } catch (Exception e) {
                    System.out.println("❌ Erro ao restaurar: " + e.getMessage());
                }
                break;
        }
    }


    private static int lerOpcao() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static Long lerLong() {
        try {
            return Long.parseLong(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1L;
        }
    }
}