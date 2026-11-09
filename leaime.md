# SpamBlocker

Aplicativo Android nativo desenvolvido em Kotlin e Jetpack Compose para bloquear chamadas indesejadas e números fora da lista de contatos.

---

## Como Funciona a Ferramenta

O aplicativo funciona interceptando chamadas telefônicas recebidas em tempo real utilizando APIs nativas do Android, rejeitando-as silenciosamente caso o número não esteja cadastrado na agenda de contatos do dispositivo.

### 1. Interceptação de Chamadas (`CallScreeningService`)
- O aplicativo registra o serviço `CallBlockingService`, associado à permissão nativa `android.permission.BIND_SCREENING_SERVICE`.
- Ao receber uma chamada telefônica, o sistema operacional Android repassa os detalhes da ligação para o método `onScreenCall(Call.Details)`.

### 2. Validação de Contatos (`ContactsContract`)
- O serviço extrai o número da chamada recebida e realiza uma consulta no provedor de conteúdos do Android (`ContactsContract.PhoneLookup`).
- Requer a permissão de tempo de execução `READ_CONTACTS`.

### 3. Regra de Negócio e Rejeição Silenciosa
- **Se o número estiver nos contatos:** A ligação é liberada normalmente.
- **Se o número NÃO estiver nos contatos:** A chamada é imediatamente bloqueada e rejeitada sem exibir notificações ou tocar o aparelho, através da configuração da resposta do `CallResponse.Builder`:
  - `setDisallowCall(true)`: Impede a continuidade da chamada.
  - `setRejectCall(true)`: Rejeita a ligação na rede telefônica.
  - `setSkipNotification(true)`: Omite qualquer notificação do sistema.
  - `setSkipCallLog(true)`: Suprime o registro no histórico padrão de chamadas.

### 4. Registro no Banco de Dados Local (Room Database)
- Toda chamada bloqueada é salva localmente e de forma assíncrona no banco de dados SQLite gerenciado via **Room**:
  - **Entidade (`BlockedCall`):** Armazena o identificador único, número bloqueado, data/hora da chamada (`timestamp`) e o motivo do bloqueio.
  - **DAO (`BlockedCallDao`):** Disponibiliza operações de inserção, exclusão e consulta com retorno reativo via Kotlin Coroutines `Flow`.
  - **Repositório (`BlockedCallRepository`):** Centraliza a camada de acesso a dados.

### 5. Interface Gráfica e Gerenciamento (Jetpack Compose)
- Interface reativa construída com **Material Design 3**:
  - **Status do Serviço:** Exibe em tempo real o estado da permissão de contatos e se a função `RoleManager.ROLE_CALL_SCREENING` foi concedida pelo usuário (Android 10+).
  - **Histórico de Chamadas Bloqueadas:** Apresenta a listagem completa com número, data e hora formatada.
  - **Ações:** Permite excluir registros individuais, limpar todo o histórico e simular o recebimento de chamadas para testes.

---

## Permissões e Requisitos

- **Android SDK Mínimo:** 24 (Android 7.0)
- **Target SDK:** 36
- **Permissões Utilizadas:**
  - `android.permission.READ_CONTACTS`: Leitura de contatos locais para determinar se o número é confiável.
  - `android.permission.BIND_SCREENING_SERVICE`: Associação do serviço ao sistema de triagem de chamadas.
  - `RoleManager.ROLE_CALL_SCREENING` (Android 10+ / API 29+): Permite ao app atuar como triador oficial de chamadas no dispositivo.

---

## Estrutura do Projeto

```text
app/src/main/
├── AndroidManifest.xml                  # Declarações de permissões e serviço
├── java/com/example/
│   ├── MainActivity.kt                  # Interface principal em Jetpack Compose e solicitação de permissões
│   ├── BlockedCallViewModel.kt          # ViewModel responsável pelo estado da UI
│   ├── CallBlockingService.kt           # Serviço de triagem e bloqueio de chamadas
│   ├── data/
│   │   ├── AppDatabase.kt               # Banco de dados Room
│   │   ├── BlockedCall.kt               # Entidade da chamada bloqueada
│   │   ├── BlockedCallDao.kt            # Data Access Object (consultas e comandos)
│   │   └── BlockedCallRepository.kt     # Repositório de dados
│   └── ui/theme/                        # Tema, tipografia e cores M3
└── res/values/strings.xml               # Recursos de texto internacionalizados
```

---

## Compilação e Execução

Para compilar e gerar o aplicativo via linha de comando ou no Android Studio:

```bash
# Compilar o APK de depuração
gradle assembleDebug

# Executar testes unitários e testes do Room com Robolectric
gradle testDebugUnitTest
```
