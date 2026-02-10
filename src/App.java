import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

public class App {

    // ======== LIMITES FIXOS ========
    static final int MAX_QUARTOS = 200; // deixamos assim para facilitar caso queira mudar depois
    static final int MAX_HOSPEDES = 1000;
    static final int MAX_RESERVAS = 1000;

    // ======== ARQUIVOS ========
    static final String ARQ_QUARTOS = "quartos.csv"; // deixamos assim para facilitar caso precise mudar depois
    static final String ARQ_HOSPEDES = "hospedes.csv";
    static final String ARQ_RESERVAS = "reservas.csv";

    // ======== DADOS ========
    static Quarto[] quartos = new Quarto[MAX_QUARTOS]; // criamos arrays static para serem facilmente acessados como variaveis de classe
    static Hospede[] hospedes = new Hospede[MAX_HOSPEDES];
    static Reserva[] reservas = new Reserva[MAX_RESERVAS];

    static int qtdQuartos = 0;
    static int qtdHospedes = 0;
    static int qtdReservas = 0;

    static int proxHospedeId = 1; // autoincrementacao para manter ja pronta a posicao do proximo;
    static int proxReservaId = 1; // autoincrementacao // // // 

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        carregarTudo(); // autoexplicativa
        atualizarOcupacaoHoje(); // recalcular o booleano estaOcupado de cada quarto para hoje.

        while (true) {
            System.out.println("\n=== HOTEL SISTEMA ===");
            System.out.println("1) Quartos");
            System.out.println("2) Hóspedes");
            System.out.println("3) Reservas");
            System.out.println("0) Sair (salvar)");
            int cmd = lerInt(input, "Escolha: ");

            if (cmd == 1)
                menuQuartos(input);
            else if (cmd == 2)
                menuHospedes(input);
            else if (cmd == 3)
                menuReservas(input);
            else if (cmd == 0) {
                salvarTudo();
                System.out.println("Dados salvos. Volte logo!");
                break;
            } else {
                System.out.println("Opção inválida.");
            }
        }

        input.close();
    }

    // ===================== MENUS =====================

    static void menuQuartos(Scanner sc) {
        while (true) {
            atualizarOcupacaoHoje();
            System.out.println("\n--- MENU QUARTOS ---");
            System.out.println("1) Listar todos");
            System.out.println("2) Listar livres (hoje)");
            System.out.println("3) Listar ocupados (hoje)");
            System.out.println("4) Ver um quarto (por ID) e suas reservas");
            System.out.println("0) Voltar");
            int cmd = lerInt(sc, "Escolha: ");

            if (cmd == 1)
                listarQuartosTodos();
            else if (cmd == 2)
                listarQuartosLivres();
            else if (cmd == 3)
                listarQuartosOcupados();
            else if (cmd == 4)
                verQuartoEspecifico(sc);
            else if (cmd == 0)
                return;
            else System.out.println("Opção inválida.");
        }
    }

    static void menuHospedes(Scanner sc) {
        while (true) {
            System.out.println("\n--- MENU HÓSPEDES ---");
            System.out.println("1) Listar hóspedes");
            System.out.println("2) Buscar por documento");
            System.out.println("3) Editar hóspede (por ID)");
            System.out.println("0) Voltar");
            int cmd = lerInt(sc, "Escolha: ");

            if (cmd == 1)
                listarHospedes();
            else if (cmd == 2)
                buscarHospedePorDocumento(sc);
            else if (cmd == 3)
                editarHospede(sc);
            else if (cmd == 0)
                return;
            else
                System.out.println("Opção inválida.");
        }
    }

    static void menuReservas(Scanner sc) {
        while (true) {
            atualizarOcupacaoHoje(); // mantem atualizado a cada chamada
            System.out.println("\n--- MENU RESERVAS ---");
            System.out.println("1) Criar reserva");
            System.out.println("2) Listar todas as reservas");
            System.out.println("3) Listar reservas por quarto (presentes/futuras)");
            System.out.println("4) Listar reservas por hóspede (presentes/futuras)");
            System.out.println("5) Editar reserva");
            System.out.println("6) Cancelar reserva");
            System.out.println("0) Voltar");
            int cmd = lerInt(sc, "Escolha: ");

            if (cmd == 1)
                criarReserva(sc);
            else if (cmd == 2) listarReservasTodas();
            else if (cmd == 3) listarReservasPorQuarto(sc);
            else if (cmd == 4) listarReservasPorHospede(sc);
            else if (cmd == 5) editarReserva(sc);
            else if (cmd == 6) cancelarReserva(sc);
            else if (cmd == 0)
                return;
            else
                System.out.println("Opção inválida.");
        }
    }

    // ===================== QUARTOS =====================

    static void listarQuartosTodos() {
        if (qtdQuartos == 0) {
            System.out.println("Nenhum quarto carregado.");
            return;
        }
        System.out.println("\nID | Número | Capacidade | OcupadoHoje");
        for (int i = 0; i < qtdQuartos; i++) {
            Quarto q = quartos[i];
            System.out.printf("%d | %d | %d | %s%n", q.id, q.numero, q.capacidade, q.estaOcupado ? "SIM" : "NÃO");
        }
    }

    static void listarQuartosLivres() {
        boolean achou = false;
        System.out.println("\n--- Quartos LIVRES (hoje) ---");
        for (int i = 0; i < qtdQuartos; i++) {
            Quarto q = quartos[i];
            if (!q.estaOcupado) { // imprime se nao estiver ocupado hoje
                achou = true;
                System.out.printf("ID %d | Nº %d | Cap %d%n", q.id, q.numero, q.capacidade);
            }
        }
        if (!achou) System.out.println("Nenhum quarto livre hoje.");
    }

    static void listarQuartosOcupados() {
        boolean achou = false;
        LocalDate hoje = LocalDate.now();

        System.out.println("\n--- Quartos OCUPADOS (hoje) ---");
        for (int i = 0; i < qtdQuartos; i++) {
            Quarto q = quartos[i];
            if (q.estaOcupado) {
                achou = true;
                Reserva r = acharReservaAtualDoQuarto(q.id, hoje); // retorna null se nao for de hoje
                if (r != null) {
                    Hospede h = getHospedeById(r.idHospede);
                    String nome = (h != null) ? h.nome : "(hóspede não encontrado)";
                    System.out.printf("Quarto %d (ID %d) | Hóspede: %s | %s até %s | %d pessoas%n",
                            q.numero, q.id, nome, r.dataInicio, r.dataFim, r.numeroHospedes);
                } else {
                    System.out.printf("Quarto %d (ID %d) | Ocupado hoje (reserva atual não encontrada)%n", q.numero, q.id);
                }
            }
        }
        if (!achou) System.out.println("Nenhum quarto ocupado hoje.");
    }

    static void verQuartoEspecifico(Scanner sc) {
        int id = lerInt(sc, "ID do quarto: "); // pedimos e esperamos pelo numero do quarto
        Quarto q = getQuartoById(id); // associamos o objeto 'q' ao quarto salvo, atraves do getter
        if (q == null) {
            System.out.println("Quarto não encontrado.");
            return;
        }
        System.out.printf("Quarto ID %d | Nº %d | Cap %d | OcupadoHoje: %s%n",
                q.id, q.numero, q.capacidade, q.estaOcupado ? "SIM" : "NÃO");

        System.out.println("\nReservas desse quarto:");
        boolean achou = false;
        for (int i = 0; i < qtdReservas; i++)
        {
            Reserva r = reservas[i];
            if (r.idQuarto == q.id) {
                achou = true;
                Hospede h = getHospedeById(r.idHospede);
                String nome = (h != null) ? h.nome : "(hóspede não encontrado)";
                System.out.printf("Reserva %d | %s | %s até %s | %d pessoas | Hóspede: %s%n",
                        r.id, (r.ativa ? "ATIVA" : "CANCELADA"), r.dataInicio, r.dataFim, r.numeroHospedes, nome);
            }
        }
        if (!achou) System.out.println("(nenhuma reserva)");
    }

    // ===================== HÓSPEDES =====================

    static void listarHospedes() {
        if (qtdHospedes == 0) {
            System.out.println("Nenhum hóspede cadastrado.");
            return;
        }
        System.out.println("\nID | Nome | Documento");
        for (int i = 0; i < qtdHospedes; i++) {
            Hospede h = hospedes[i];
            System.out.printf("%d | %s | %s%n", h.id, h.nome, h.documento);
        }
    }

    static void buscarHospedePorDocumento(Scanner sc) {
        String doc = lerStringNaoVazia(sc, "Documento: "); // nao aceitar string vazia
        Hospede h = getHospedeByDocumento(doc); // associa o objeto h ao objeto que possui o documento correspondente
        if (h == null)
            System.out.println("Hóspede não encontrado.");
        else
            System.out.printf("Encontrado: ID %d | %s | %s%n", h.id, h.nome, h.documento);
    }

    static void editarHospede(Scanner sc) {
        int id = lerInt(sc, "ID do hóspede: ");
        Hospede h = getHospedeById(id);
        if (h == null) {
            System.out.println("Hóspede não encontrado.");
            return;
        }

        System.out.println("Deixe em branco para manter.");
        String novoNome = lerString(sc, "Novo nome: "); // recebe a linha completa
        String novoDoc = lerString(sc, "Novo documento: "); // recebe a linha completa

        if (!novoNome.trim().isEmpty()) { // verifica se nao fica vazio mesmo apos o trim
            h.nome = novoNome.trim();
        }

        if (!novoDoc.trim().isEmpty()) {
            String doc = novoDoc.trim();
            if (documentoJaExiste(doc, h.id)) {
                System.out.println("Falha: já existe outro hóspede com esse documento.");
                return;
            }
            h.documento = doc;
        }

        System.out.println("Hóspede atualizado com sucesso.");
    }

    // ===================== RESERVAS =====================

    static void criarReserva(Scanner sc) {
        if (qtdQuartos == 0) {
            System.out.println("Não há quartos carregados. Verifique o arquivo " + ARQ_QUARTOS);
            return;
        }
        if (qtdReservas >= MAX_RESERVAS) {
            System.out.println("Array de reservas cheio.");
            return;
        }

        String doc = lerStringNaoVazia(sc, "Documento do hóspede: ");
        Hospede h = getHospedeByDocumento(doc);
        // registar o novo hospede
        if (h == null) {
            System.out.println("Hóspede não encontrado. Vamos registar!");
            if (qtdHospedes >= MAX_HOSPEDES) {
                System.out.println("Array de hóspedes está cheio.");
                return;
            }
            String nome = lerStringNaoVazia(sc, "Nome do hóspede: ");
            if (documentoJaExiste(doc, -1)) {
                System.out.println("Falha: documento já existe.");
                return;
            }
            h = new Hospede(proxHospedeId++, nome, doc); // criamos um novo objeto hospede
            hospedes[qtdHospedes++] = h; // associamos ele a posicao correspondente no array principal
            System.out.println("Hóspede registado: ID " + h.id);
        }

        int numHosp = lerInt(sc, "Número de hóspedes: ");
        if (numHosp < 1) {
            System.out.println("Falha: número de hóspedes deve ser >= 1.");
            return;
        }

        LocalDate inicio = lerData(sc, "Data de início (YYYY-MM-DD): ");
        LocalDate fim = lerData(sc, "Data de fim (YYYY-MM-DD): ");
        if (inicio.isAfter(fim)) {
            System.out.println("Falha: data de início não pode ser maior que data de fim.");
            return;
        }

        int idQuartoEscolhido = encontrarMelhorQuartoDisponivel(numHosp, inicio, fim);
        if (idQuartoEscolhido == -1) {
            System.out.println("Não foi encontrado quarto disponível para essas datas e quantidade.");
            return;
        }

        Reserva r = new Reserva(proxReservaId++, idQuartoEscolhido, h.id, numHosp, inicio, fim, true); // criamos um novo objeto do tipo reserva
        reservas[qtdReservas++] = r; // salvamos uma posicao para ele no array global

        atualizarOcupacaoHoje();
        System.out.println("Reserva criada com sucesso! ID da reserva: " + r.id + " | Quarto ID: " + r.idQuarto);
    }

    static void listarReservasTodas() {
        if (qtdReservas == 0) {
            System.out.println("Nenhuma reserva registada.");
            return;
        }
        System.out.println("\nID | Quarto(ID/Nº) | Hóspede | Pessoas | Início | Fim | Status");
        for (int i = 0; i < qtdReservas; i++) {
            Reserva r = reservas[i];
            Quarto q = getQuartoById(r.idQuarto);
            Hospede h = getHospedeById(r.idHospede);
            String numQuarto = (q != null) ? String.valueOf(q.numero) : "?"; // transforma int em string
            String nome = (h != null) ? h.nome : "?";
            System.out.printf("%d | %d/%s | %s | %d | %s | %s | %s%n",
                    r.id, r.idQuarto, numQuarto, nome, r.numeroHospedes, r.dataInicio, r.dataFim, r.ativa ? "ATIVA" : "CANCELADA");
        }
    }

    static void listarReservasPorQuarto(Scanner sc) {
        int idQuarto = lerInt(sc, "ID do quarto: ");
        Quarto q = getQuartoById(idQuarto);
        if (q == null) {
            System.out.println("Quarto não encontrado.");
            return;
        }

        LocalDate hoje = LocalDate.now();
        boolean achou = false;
        System.out.println("\nReservas (presentes ou futuras) do quarto Nº " + q.numero + " (ID " + q.id + "):");
        for (int i = 0; i < qtdReservas; i++) {
            Reserva r = reservas[i];
            if (r.ativa && r.idQuarto == idQuarto && !r.dataFim.isBefore(hoje)) {
                achou = true;
                Hospede h = getHospedeById(r.idHospede);
                String nome = (h != null) ? h.nome : "?";
                System.out.printf("Reserva %d | %s até %s | %d pessoas | Hóspede: %s%n",
                        r.id, r.dataInicio, r.dataFim, r.numeroHospedes, nome);
            }
        }
        if (!achou) System.out.println("(nenhuma)");
    }

    static void listarReservasPorHospede(Scanner sc) {
        String doc = lerStringNaoVazia(sc, "Documento do hóspede: ");
        Hospede h = getHospedeByDocumento(doc);
        if (h == null) {
            System.out.println("Hóspede não encontrado.");
            return;
        }

        LocalDate hoje = LocalDate.now();
        boolean achou = false;
        System.out.println("\nReservas (presentes ou futuras) do hóspede " + h.nome + " (ID " + h.id + "):");
        for (int i = 0; i < qtdReservas; i++) {
            Reserva r = reservas[i];
            if (r.ativa && r.idHospede == h.id && !r.dataFim.isBefore(hoje)) {
                achou = true;
                Quarto q = getQuartoById(r.idQuarto);
                String numQuarto = (q != null) ? String.valueOf(q.numero) : "?";
                System.out.printf("Reserva %d | Quarto %s | %s até %s | %d pessoas%n",
                        r.id, numQuarto, r.dataInicio, r.dataFim, r.numeroHospedes);
            }
        }
        if (!achou) System.out.println("(nenhuma)");
    }

    static void editarReserva(Scanner sc) {
        int id = lerInt(sc, "ID da reserva: "); // usuario nos diz qual reserva quer editar
        Reserva r = getReservaById(id);
        if (r == null) {
            System.out.println("Reserva não encontrada.");
            return;
        }
        if (!r.ativa) {
            System.out.println("Reserva cancelada. Não dá para editar.");
            return;
        }

        Quarto q = getQuartoById(r.idQuarto);
        if (q == null) {
            System.out.println("Erro: quarto da reserva não encontrado.");
            return;
        }

        System.out.println("Deixe em branco para manter.");
        String sNum = lerString(sc, "Novo número de hóspedes: ");
        String sInicio = lerString(sc, "Nova data início (YYYY-MM-DD): ");
        String sFim = lerString(sc, "Nova data fim (YYYY-MM-DD): ");

        int novoNumHosp = r.numeroHospedes;
        LocalDate novoInicio = r.dataInicio;
        LocalDate novoFim = r.dataFim;

        if (!sNum.trim().isEmpty()) {
            try {
                novoNumHosp = Integer.parseInt(sNum.trim());
            } catch (NumberFormatException e) {
                System.out.println("Formato inválido para número de hóspedes.");
                return;
            }
        }

        if (!sInicio.trim().isEmpty()) {
            try {
                novoInicio = LocalDate.parse(sInicio.trim());
            } catch (DateTimeParseException e) {
                System.out.println("Formato inválido de data início.");
                return;
            }
        }

        if (!sFim.trim().isEmpty()) {
            try {
                novoFim = LocalDate.parse(sFim.trim());
            } catch (DateTimeParseException e) {
                System.out.println("Formato inválido de data fim.");
                return;
            }
        }

        // validações
        if (novoNumHosp < 1) {
            System.out.println("Falha: número de hóspedes deve ser >= 1.");
            return;
        }
        if (novoNumHosp > q.capacidade) {
            System.out.println("Falha: excede a capacidade do quarto (" + q.capacidade + ").");
            return;
        }
        if (novoInicio.isAfter(novoFim)) {
            System.out.println("Falha: data início não pode ser maior que data fim.");
            return;
        }
        if (temConflito(q.id, novoInicio, novoFim, r.id)) {
            System.out.println("Falha: conflito com outra reserva ativa do mesmo quarto.");
            return;
        }

        // aplicar
        r.numeroHospedes = novoNumHosp;
        r.dataInicio = novoInicio;
        r.dataFim = novoFim;

        atualizarOcupacaoHoje();
        System.out.println("Reserva atualizada com sucesso.");
    }

    static void cancelarReserva(Scanner sc) {
        int id = lerInt(sc, "ID da reserva: ");
        Reserva r = getReservaById(id);
        if (r == null) {
            System.out.println("Reserva não encontrada.");
            return;
        }
        if (!r.ativa) {
            System.out.println("Reserva já está cancelada.");
            return;
        }
        r.ativa = false;
        atualizarOcupacaoHoje();
        System.out.println("Reserva cancelada com sucesso.");
    }

    // ===================== REGRAS DE NEGÓCIO =====================

    static int encontrarMelhorQuartoDisponivel(int numHosp, LocalDate inicio, LocalDate fim) {
        int melhorId = -1;
        int melhorDiff = Integer.MAX_VALUE;
        int melhorNumeroQuarto = Integer.MAX_VALUE;

        for (int i = 0; i < qtdQuartos; i++)
        {
            Quarto q = quartos[i];
            if (q.capacidade < numHosp)
                continue;

            if (!temConflito(q.id, inicio, fim, -1)) {
                int diff = q.capacidade - numHosp; // quanto menor a diferenca melhor
                if (diff < melhorDiff || (diff == melhorDiff && q.numero < melhorNumeroQuarto)) {
                    melhorDiff = diff;
                    melhorNumeroQuarto = q.numero;
                    melhorId = q.id;
                }
            }
        }
        return melhorId; // retorna o id do quarto mais eficiente
    }

    // se ha conflito nas datas de ocupacao do quarto
    static boolean temConflito(int idQuarto, LocalDate inicio, LocalDate fim, int ignorarReservaId) {
        for (int i = 0; i < qtdReservas; i++)
        {
            Reserva r = reservas[i];
            if (!r.ativa)
                continue;
            if (r.id == ignorarReservaId)
                continue;
            if (r.idQuarto != idQuarto)
                continue;

            // interseção ocorre se: inicio <= r.dataFim && fim >= r.dataInicio
            boolean interseta = (!inicio.isAfter(r.dataFim) && !fim.isBefore(r.dataInicio));
            if (interseta)
                return true;
        }
        return false;
    }

    static void atualizarOcupacaoHoje() {
        LocalDate hoje = LocalDate.now();
        for (int i = 0; i < qtdQuartos; i++)
        {
            Quarto q = quartos[i]; // percorremos o array de objetos do tipo quartos da classe principal
            q.estaOcupado = (acharReservaAtualDoQuarto(q.id, hoje) != null);
        }
    }

    static Reserva acharReservaAtualDoQuarto(int idQuarto, LocalDate hoje) {
        for (int i = 0; i < qtdReservas; i++) {
            Reserva r = reservas[i]; // percorremos o array de objetos do tipo reservas da classe principal
            if (!r.ativa)
                continue;
            if (r.idQuarto != idQuarto)
                continue;
            boolean hojeDentro = (!hoje.isBefore(r.dataInicio)) && (!hoje.isAfter(r.dataFim)); // nem antes nem depois de hoje
            if (hojeDentro)
                return r;
        }
        return null;
    }

    // ===================== GETTERS =====================

    static Quarto getQuartoById(int id) {
        for (int i = 0; i < qtdQuartos; i++) {
            if (quartos[i].id == id) return quartos[i];
        }
        return null;
    }

    static Hospede getHospedeById(int id) {
        for (int i = 0; i < qtdHospedes; i++) {
            if (hospedes[i].id == id) return hospedes[i];
        }
        return null;
    }

    static Hospede getHospedeByDocumento(String doc) {
        for (int i = 0; i < qtdHospedes; i++) {
            if (hospedes[i].documento.equals(doc)) return hospedes[i];
        }
        return null;
    }

    static Reserva getReservaById(int id) {
        for (int i = 0; i < qtdReservas; i++) {
            if (reservas[i].id == id) return reservas[i];
        }
        return null;
    }

    static boolean documentoJaExiste(String doc, int ignorarHospedeId) {
        for (int i = 0; i < qtdHospedes; i++) {
            Hospede h = hospedes[i];
            if (h.id == ignorarHospedeId) // ignora ele mesmo
                continue;
            if (h.documento.equals(doc))
                return true;
        }
        return false;
    }

    // ===================== LEITURA =====================

    static int lerInt(Scanner sc, String msg) {
        while (true) {
            System.out.print(msg);
            String s = sc.nextLine().trim(); // recebe o que foi escrito pelo usuario
            try {
                return Integer.parseInt(s); // transforma a string em int
            } catch (NumberFormatException e) {
                System.out.println("Digite um número inteiro válido.");
            }
        }
    }

    static String lerString(Scanner sc, String msg) {
        System.out.print(msg);
        return sc.nextLine();
    }

    static String lerStringNaoVazia(Scanner sc, String msg) {
        while (true) {
            System.out.print(msg);
            String s = sc.nextLine().trim();
            if (!s.isEmpty()) // se nao esta empty
                return s;
            System.out.println("Não pode ser vazio.");
        }
    }

    static LocalDate lerData(Scanner sc, String msg) {
        while (true) {
            System.out.print(msg);
            String s = sc.nextLine().trim();
            try {
                return LocalDate.parse(s); // padrao YYYY-MM-DD
            } catch (DateTimeParseException e) { // exception usada para erro em data
                System.out.println("Data inválida. Use YYYY-MM-DD.");
            }
        }
    }

    // ===================== CSV (carregar e salvar) =====================

    static void carregarTudo() {
        carregarQuartos();
        carregarHospedes();
        carregarReservas();
        ajustarProximosIds();
    }

    static void salvarTudo() {
        salvarHospedes();
        salvarReservas();
    }

    static void carregarQuartos() {
        qtdQuartos = 0;
        File f = new File(ARQ_QUARTOS);
        if (!f.exists()) {
            System.out.println("Aviso: " + ARQ_QUARTOS + " não encontrado. Sem quartos carregados.");
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(f))) { // try para garantir fechar o bufferedreader e o fileReader
            String line;
            boolean primeiraLinhaPodeSerQtd = true;

            while ((line = br.readLine()) != null) { // para ler linha por linha, retornando uma string
                line = line.trim(); // remover espacos da sobrando
                if (line.isEmpty())
                    continue;

                String[] p = splitCsvSimples(line); // separar as strigns por virgula ou ponto virgula
    
                // Se a primeira linha for só um numero comer ele
                if (primeiraLinhaPodeSerQtd && p.length == 1) {
                    Integer talvezQtd = tentaParseInt(p[0]);
                    if (talvezQtd != null) {
                        primeiraLinhaPodeSerQtd = false;
                        continue;
                    }
                }
                primeiraLinhaPodeSerQtd = false;
    
                if (p.length < 3)
                    continue;
    
                Integer id = tentaParseInt(p[0]);
                Integer numero = tentaParseInt(p[1]);
                Integer cap = tentaParseInt(p[2]);
                if (id == null || numero == null || cap == null) // garantir todas as infos corretas
                    continue;

                boolean ocupado = false;
                if (p.length >= 4) {
                    ocupado = parseBool(p[3]);
                }
    
                if (qtdQuartos >= MAX_QUARTOS) {
                    System.out.println("Aviso: limite de quartos atingido (" + MAX_QUARTOS + ").");
                    break;
                }
    
                quartos[qtdQuartos++] = new Quarto(id, numero, cap, ocupado);
            }
    
        } catch (IOException e) {
            System.out.println("Erro ao ler " + ARQ_QUARTOS + ": " + e.getMessage());
        }
    }
    

    static void carregarHospedes() {
        qtdHospedes = 0;
        File f = new File(ARQ_HOSPEDES);
        if (!f.exists())
            return; // checagem se esta vazio
    
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            boolean primeiraLinhaPodeSerQtd = true;
    
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty())
                    continue;
    
                String[] p = splitCsvSimples(line);
    
                // 1a linha, comer o numero
                if (primeiraLinhaPodeSerQtd && p.length == 1) {
                    Integer talvezQtd = tentaParseInt(p[0]);
                    if (talvezQtd != null) {
                        primeiraLinhaPodeSerQtd = false;
                        continue;
                    }
                }
                primeiraLinhaPodeSerQtd = false;

                if (p.length < 3)
                    continue;

                Integer id = tentaParseInt(p[0]);
                if (id == null)
                    continue;

                String nome = p[1].trim();
                String doc = p[2].trim();
                if (nome.isEmpty() || doc.isEmpty())
                    continue;
    
                if (qtdHospedes >= MAX_HOSPEDES) {
                    System.out.println("Aviso: limite de hóspedes atingido (" + MAX_HOSPEDES + ").");
                    break;
                }
    
                // evita documento duplicado se no CSV vier errado
                if (documentoJaExiste(doc, -1)) // -1 nao ignora nenhum hospede
                    continue;
    
                hospedes[qtdHospedes++] = new Hospede(id, nome, doc);
            }
        } catch (IOException e) {
            System.out.println("Erro ao ler " + ARQ_HOSPEDES + ": " + e.getMessage());
        }
    }    

    static void carregarReservas() {
        qtdReservas = 0;
        File f = new File(ARQ_RESERVAS);
        if (!f.exists())
            return; // se começar vazio
    
        try (BufferedReader br = new BufferedReader(new FileReader(f))) { // try para garantir fechamento do bufferedReader
            String line;
            boolean primeiraLinhaPodeSerQtd = true;
    
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
    
                String[] p = splitCsvSimples(line);
    
                // 1a linha, comer o numero
                if (primeiraLinhaPodeSerQtd && p.length == 1) {
                    Integer talvezQtd = tentaParseInt(p[0]);
                    if (talvezQtd != null) {
                        primeiraLinhaPodeSerQtd = false;
                        continue;
                    }
                }
                primeiraLinhaPodeSerQtd = false;
    
                if (p.length < 7) // ignorar reservas que nao venham com true ou false
                    continue;
    
                Integer id = tentaParseInt(p[0]);
                Integer idQuarto = tentaParseInt(p[1]);
                Integer idHospede = tentaParseInt(p[2]);
                Integer numHosp = tentaParseInt(p[3]);
    
                if (id == null || idQuarto == null || idHospede == null || numHosp == null) continue; // ignorar se a reserva tiver algo faltando
    
                LocalDate inicio;
                LocalDate fim;
                try { // segurança caso a data esteja mal escrita
                    inicio = LocalDate.parse(p[4].trim());
                    fim = LocalDate.parse(p[5].trim());
                } catch (Exception e) {
                    continue;
                }
    
                boolean ativa = parseBool(p[6]);
    
                if (qtdReservas >= MAX_RESERVAS) {
                    System.out.println("Aviso: limite de reservas atingido (" + MAX_RESERVAS + ").");
                    break;
                }
    
                reservas[qtdReservas++] = new Reserva(id, idQuarto, idHospede, numHosp, inicio, fim, ativa); // salvamos o novo objeto no array;
            }
        } catch (IOException e) {
            System.out.println("Erro ao ler " + ARQ_RESERVAS + ": " + e.getMessage());
        }
    }    

    static void salvarHospedes() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(ARQ_HOSPEDES))) { // selecionamos o arquivo que vamos escrever
            pw.println(qtdHospedes); // escrever normalmente, a qtd de hospedes
            for (int i = 0; i < qtdHospedes; i++) {
                Hospede h = hospedes[i];
                pw.printf("%d,%s,%s%n", h.id, limparCsv(h.nome), limparCsv(h.documento));
            }
        } catch (IOException e) {
            System.out.println("Erro ao salvar " + ARQ_HOSPEDES + ": " + e.getMessage());
        }
    }    

    static void salvarReservas() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(ARQ_RESERVAS))) {
            pw.println(qtdReservas);
            for (int i = 0; i < qtdReservas; i++) {
                Reserva r = reservas[i];
                pw.printf("%d,%d,%d,%d,%s,%s,%s%n", r.id, r.idQuarto, r.idHospede, r.numeroHospedes, r.dataInicio.toString(), r.dataFim.toString(), r.ativa ? "true" : "false");
            }
        } catch (IOException e) {
            System.out.println("Erro ao salvar " + ARQ_RESERVAS + ": " + e.getMessage());
        }
    }    

    static void ajustarProximosIds() {
        int maxH = 0;
        for (int i = 0; i < qtdHospedes; i++)
            if (hospedes[i].id > maxH)
                maxH = hospedes[i].id;
        proxHospedeId = maxH + 1; // assim o proximo hospede ja eh o proximo no array tambem;

        int maxR = 0;
        for (int i = 0; i < qtdReservas; i++)
            if (reservas[i].id > maxR)
                maxR = reservas[i].id;
        proxReservaId = maxR + 1; // do mesmo modo assim a proxima reserva ja eh a proxima no array;
    }

    static String[] splitCsvSimples(String line) {
        // aceita virgula ou ponto e virgula como separador
        if (line.indexOf(';') >= 0)
            return line.split(";");
        return line.split(",");
    }

    static Integer tentaParseInt(String s) { // integer pode ser null entao eh melhor para explicitar que nao deu certo
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return null;
        }
    }

    static boolean parseBool(String s) {
        if (s == null) return false;
        String x = s.trim().toLowerCase();
        return x.equals("true") || x.equals("1") || x.equals("sim") || x.equals("s") || x.equals("yes") || x.equals("y");
    }

    static String limparCsv(String s) {
        // evita quebrar o arquivo, pq como aceito , e ; como separadores nao posso ter eles nas strings
        return s.replace(",", " ").replace(";", " ").trim();
    }

    // ===================== CLASSES =====================

    static class Quarto {
        int id;
        int numero;
        int capacidade;
        boolean estaOcupado;

        Quarto(int id, int numero, int capacidade, boolean estaOcupado) { // construtor base da classe
            this.id = id;
            this.numero = numero;
            this.capacidade = capacidade;
            this.estaOcupado = estaOcupado;
        }
    }

    static class Hospede {
        int id;
        String nome;
        String documento;

        Hospede(int id, String nome, String documento) { // construtor base
            this.id = id;
            this.nome = nome;
            this.documento = documento;
        }
    }

    static class Reserva {
        int id;
        int idQuarto;
        int idHospede;
        int numeroHospedes;
        LocalDate dataInicio;
        LocalDate dataFim;
        boolean ativa;

        Reserva(int id, int idQuarto, int idHospede, int numeroHospedes, LocalDate dataInicio, LocalDate dataFim, boolean ativa) { // construtor base
            this.id = id;
            this.idQuarto = idQuarto;
            this.idHospede = idHospede;
            this.numeroHospedes = numeroHospedes;
            this.dataInicio = dataInicio;
            this.dataFim = dataFim;
            this.ativa = ativa;
        }
    }
}
