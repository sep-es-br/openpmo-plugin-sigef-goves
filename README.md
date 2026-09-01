# openpmo-plugin-sigef-goves

Plugin de integração entre o OpenPMO e o SIGEF do Governo do Estado do Espírito Santo (GOVES).

## Objetivo

Este projeto implementa o contrato [`openpmo-plugin-sigef-interface`](https://github.com/sep-es-br/openpmo-plugin-sigef-interface) para consultar dados orçamentários disponibilizados pelo SIGEF por meio das APIs do Pentaho.

O plugin isola do OpenPMO os detalhes específicos da integração: autenticação HTTP Basic, configuração dos endpoints, consultas ao Pentaho e extração do campo `resultset` das respostas.

## Funcionalidades

- consulta de unidades orçamentárias;
- consulta de planos orçamentários por unidade;
- consulta de instrumentos e contratos por unidade e período;
- consulta de valores liquidados por plano orçamentário e unidade;
- consulta de fontes de recurso;
- autenticação Basic nas requisições ao Pentaho;
- registro automático de `SigefesProvider` como implementação de `ISigefProvider` por meio do Spring.

## Fluxo da integração

1. A aplicação consumidora carrega o artefato do plugin no classpath do OpenPMO.
2. O Spring registra `SigefesProvider`, que implementa `ISigefProvider`.
3. O provider cria um cliente HTTP configurado para os endpoints do Pentaho.
4. Cada consulta envia as credenciais configuradas usando o cabeçalho `Authorization: Basic`.
5. As respostas JSON são interpretadas e, nas consultas de listas, o conteúdo de `resultset` é retornado ao contrato do OpenPMO.

## Requisitos

- Java 11 ou superior;
- Spring Boot 2.2.12;
- credenciais de acesso ao Pentaho/SIGEF;
- repositório JitPack configurado no projeto consumidor.

## Instalação

Adicione o JitPack aos repositórios do Gradle:

```groovy
repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' }
}
```

Adicione o contrato e o plugin às dependências da aplicação consumidora:

```groovy
dependencies {
    implementation 'com.github.sep-es-br:openpmo-plugin-sigef-interface:2.0.0'
    implementation 'com.github.sep-es-br:openpmo-plugin-sigef-goves:1.0.0'
}
```

O plugin depende do contrato `openpmo-plugin-sigef-interface:2.0.0` e fornece a implementação GOVES desse contrato para a aplicação consumidora.

## Uso no OpenPMO API

No OpenPMO API, habilite o plugin pela coordenada Maven no arquivo `application.properties`:

```properties
app.plugin.sigef.repository=com.github.sep-es-br:openpmo-plugin-sigef-goves:1.0.0
```

Quando a propriedade não está configurada ou o artefato não está disponível, o OpenPMO permanece sem um provider SIGEF. As funcionalidades que dependem do SIGEF devem considerar essa disponibilidade opcional.

As consultas de seleção ficam disponíveis no controller do OpenPMO API:

| Operação | Endpoint do OpenPMO API |
| --- | --- |
| Listar unidades orçamentárias | `GET /sigef-selections/budget-units` |
| Listar planos orçamentários | `GET /sigef-selections/budget-plans?budgetUnitCode={codUo}` |
| Listar fontes financeiras | `GET /sigef-selections/financial-sources` |
| Consultar valores liquidados | `GET /schedules/pentaho/po/liquidated/{codPo}/{codUo}` |

## Configuração

O plugin lê as credenciais e as URLs por propriedades Spring. Em uma aplicação consumidora, forneça pelo menos:

```properties
pentahoBI.userId=usuario-do-pentaho
pentahoBI.password=senha-do-pentaho

pentaho.api.uo.url=https://servidor/pentaho/plugin/cda/api/doQuery?path=/public/dashboard/plano_orcamentario/api_uo_all.cda&dataAccessId=api_uo_all
pentaho.api.po.url=https://servidor/pentaho/plugin/cda/api/doQuery?path=/public/dashboard/plano_orcamentario/api_po_uo.cda&dataAccessId=api_po_uo&parampCodUo=
pentaho.api.contratos.url=https://servidor/pentaho/plugin/cda/api/doQuery?path=/public/dashboard/plano_orcamentario/api_contrato_convenio.cda&dataAccessId=api_contrato_convenio&paramuo=%s&paramde=%d&paramate=%d
pentaho.api.liquidacao.url=https://servidor/pentaho/plugin/cda/api/doQuery?path=/public/dashboard/plano_orcamentario/api_po_instrumentos_valores.cda&dataAccessId=api_po_instrumento_valores&paramcod_uo=%s&paramcod_po=%s&paramcod_contrato=%s&paramcod_convenio=%s&paramcod_categoria=%s&paramcod_fonte=%s
pentaho.api.fontes.url=https://servidor/pentaho/plugin/cda/api/doQuery?path=/public/dashboard/pmo/sigefes_fontes.cda&dataAccessId=sigefes_fontes
```

A propriedade `pentaho.api.contratos.url` é um template usado com `String.format` e recebe, nesta ordem, o código da unidade orçamentária, o ano inicial e o ano final. A propriedade `pentaho.api.liquidacao.url` é usada como base; na chamada do provider, o código do plano é acrescentado ao final, seguido de `&parampCodUo=` e do código da unidade. Essa propriedade precisa estar configurada com o formato esperado pelo endpoint do Pentaho.

Recomenda-se fornecer credenciais por configuração externa ou variáveis de ambiente. Não grave credenciais reais no repositório.

O arquivo [`application.yaml`](src/main/resources/application.yaml) contém URLs de referência utilizadas neste projeto. A implementação lê especificamente a propriedade `pentaho.api.liquidacao.url` para a consulta de valores liquidados.

## Métodos implementados

`SigefesProvider` implementa os seguintes métodos do contrato:

| Método | Consulta realizada | Retorno |
| --- | --- | --- |
| `getBudgetUnitList()` | GET no endpoint de unidades (`pentaho.api.uo.url`) | Campo `resultset` |
| `getBudgetPlanList(String codUo)` | GET no endpoint de planos, acrescentando `codUo` à URL | Campo `resultset` |
| `getInstrumentsList(String codUo, long startYear, long endYear)` | GET no template de contratos com unidade e período | Campo `resultset` |
| `getLiquidatedValueByBudgetPlan(String codPo, String codUo)` | GET no endpoint de liquidação com plano e unidade | Resposta JSON da consulta |
| `getResourceSourceList()` | GET no endpoint de fontes de recurso (`pentaho.api.fontes.url`) | Campo `resultset` |

As consultas usam `RestTemplate` e bloqueiam até a conclusão da chamada assíncrona. Falhas de configuração do cliente ou da requisição são encapsuladas em `GlobalException`.

## Endpoints do Pentaho

| Operação | Configuração | Método HTTP |
| --- | --- | --- |
| Unidades orçamentárias | `pentaho.api.uo.url` | `GET` |
| Planos orçamentários | `pentaho.api.po.url` + código da UO | `GET` |
| Instrumentos e contratos | `pentaho.api.contratos.url` | `GET` |
| Valores liquidados | `pentaho.api.liquidacao.url` | `GET` |
| Fontes de recurso | `pentaho.api.fontes.url` | `GET` |

## Autenticação e tratamento de erros

As requisições ao Pentaho usam HTTP Basic com `pentahoBI.userId` e `pentahoBI.password`. O `RestTemplate` também é criado com suporte a certificados autoassinados e sem verificação do hostname, conforme a implementação atual do plugin; a configuração de rede e de certificados deve ser avaliada no ambiente de execução.

Exceções durante a criação do cliente HTTP, autenticação, comunicação ou interpretação do JSON são registradas e lançadas como `GlobalException`. O tratamento HTTP final depende da aplicação consumidora; no OpenPMO API, os controllers convertem falhas das consultas em respostas HTTP `500`.

## Build e testes

No Windows:

```powershell
.\gradlew.bat clean test build
```

No Linux ou macOS:

```bash
./gradlew clean test build
```

## Publicação local

Para publicar o artefato no Maven Local:

```powershell
.\gradlew.bat publishToMavenLocal
```

Para compilar o plugin com uma versão local ainda não publicada do contrato, publique primeiro o projeto `openpmo-plugin-sigef-interface` no Maven Local.

## Licença

Este projeto é distribuído sob a licença GNU General Public License v3.0. Consulte o arquivo [LICENSE](LICENSE).
