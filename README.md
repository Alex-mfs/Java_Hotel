# Guia Prático do código

**deixar o codigo em camadas
(1) Interface (menus)** → **(2) Casos de uso (listar/criar/editar/cancelar) →** **(3) Regras de negócio (conflito, melhor quarto, ocupação)** → **(4) Acesso a dados (getters e CSV)** → **(5) Utilitários (leitura/parse)**

## Mapa de chamadas dentro do código:

- **Começo:** `main` → `carregarTudo` → `(carregarQuartos/hospedes/reservas)` → `ajustarProximosIds` → `atualizarOcupacaoHoje`
- **Menu Quartos:** `menuQuartos` → `atualizarOcupacaoHoje` → `(listar... | verQuartoEspecifico)`
- **Menu Hóspedes:** `menuHospedes` → `(listar | buscar(doc→getHospedeByDocumento) | editar(id→getHospedeById + documentoJaExiste))`
- **Menu Reservas:**
    - criar: `criarReserva` → `(getHospedeByDocumento / cria Hospede)` → `lerData` →`encontrarMelhorQuartoDisponivel` → `temConflito` → `cria Reserva` →`atualizarOcupacaoHoje`
    - editar: `editarReserva` → `getReservaById` → `getQuartoById` → `temConflito(ignora a própria)` → `atualizarOcupacaoHoje`
    - cancelar: `cancelarReserva` → `getReservaById` → `atualizarOcupacaoHoje`

---

### Carregar

`File` transforma o caminho em um objeto tipo file, `FileReader` abre esse arquivo como fluxo de caracteres, o `BufferedReader` coloca esses caracteres em um buffer na memória e, a partir dele, você lê cada linha com `readLine()`.

atualiza os ids da reserva e hospede no fim.

### AtualizarOcupacao

fazemos isso no início pra sincronizar o estado dos quartos com a realidade das reservas do dia atual antes de qualquer decisão do usuário.

`atualizarOcupacaoHoje` percorre todos os quartos e coloca `estaOcupado` como **true** se `acharReservaAtualDoQuarto` achar uma **reserva ativa** daquele quarto cujo período inclui **hoje** (senão fica **false**).

---

### MenuQuartos

fica em loop mostrando opções de listagem/consulta de quartos, e antes de listar recalcula a ocupação de hoje pra não ficar desatualizado.

### ListarQuartosTodos

percorre `quartos[]` até `qtdQuartos` e imprime `id`, `numero`, `capacidade` e o booleano `estaOcupado`.

### ListarQuartosLivres

percorre `quartos[]` e imprime apenas os que estão com `estaOcupado == false` (ou seja, livres hoje).

### ListarQuartosOcupados

percorre os quartos ocupados hoje e, para cada um, busca a reserva atual com `acharReservaAtualDoQuarto` e o nome do hóspede via `getHospedeById`.

### VerQuartoEspecifico

pega um quarto por `id`, imprime os dados dele e depois passa por todas `reservas[]` para listar todas as reservas cujo `idQuarto` bate com o quarto.

---

### MenuHospedes

loop de opções relacionadas a hóspedes (listar, buscar por documento, editar), usando os getters pra localizar os registos.

### ListarHospedes

fazemos um loop para escrever no terminal todos os hospedes com suas respetivas informacoes.

### BuscarHospedePorDocumento

pede um documento ao user, usa `getHospedeByDocumento(doc)` e mostra o hóspede encontrado (ou informa que não existe).

### EditarHospede

localiza o hóspede por `id`, lê “novo nome” e “novo documento” (podem vir em branco pra manter) e se mudar documento garante ser unico com `documentoJaExiste(doc, idAtual)`.

---

### MenuReservas

loop de opções de reservas (criar/listar/editar/cancelar), recalculando a ocupação sempre que necessário pra refletir as mudanças.

### CriarReserva

acha (ou regista) o hóspede pelo documento, lê quantidade e datas, escolhe o quarto mais adequado e livre com `encontrarMelhorQuartoDisponivel`, cria a `Reserva` e atualiza ocupação.

### ListarReservasTodas

passa por todas guardadas em  `reservas[]` e imprime cada reserva junto com informações do quarto `getQuartoById` e do hóspede `getHospedeById` como um “join manual”.

### ListarReservasPorQuarto

pega `idQuarto`, valida o quarto, e lista reservas ativas daquele quarto que ainda não terminaram (presentes/futuras).

### ListarReservasPorHospede

acha hóspede pelo documento e lista reservas ativas dele que ainda não terminaram, trazendo também o número do quarto.

### EditarReserva

localiza a reserva por `id`, lê novos valores opcionais (num hóspedes e datas), valida capacidade/datas e checa conflito com `temConflito(..., ignorarReservaId=r.id)`, aplica e recalcula ocupação.

### CancelarReserva

localiza a reserva e faz um soft delete marcando `ativa=false`, mantendo histórico e recalculando ocupação.

---

## Regras de Negocio

### EncontrarMelhorQuartoDisponivel

passa pot todos os quartos que comportam `numHosp` e escolhe o que não conflita no período, minimizando a “sobra de capacidade” (e desempata pelo menor número do quarto).

### TemConflito

verifica se existe alguma reserva ativa do mesmo quarto cujo intervalo de datas intersecta o novo intervalo (ignorando uma reserva específica quando está editando).

---

## Salvar em CSV

### SalvarTudo

escreve em CSV os hóspedes e as reservas (mantendo o formato “primeira linha = quantidade”).

### SalvarHospedes

escreve `qtdHospedes` na primeira linha e depois cada hóspede em `id,nome,documento` (limpando separadores com `limparCsv`).

### SalvarReservas

escreve `qtdReservas` na primeira linha e depois cada reserva em `id,idQuarto,idHospede,numHosp,inicio,fim,ativa`.
