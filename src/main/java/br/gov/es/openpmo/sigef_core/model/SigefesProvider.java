package br.gov.es.openpmo.sigef_core.model;

import br.gov.es.openpmo.sigef_core.exception.GlobalException;
import br.gov.es.openpmo.sigef_core.util.RestTemplateUtils;
import com.fasterxml.jackson.databind.JsonNode;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class SigefesProvider implements ISigefProvider {
    
    
    private final RestTemplateUtils restTemplateUtils = new RestTemplateUtils();
    
    private final String RESULTSET_FIELD = "resultset";
    
    @Value("${pentaho.api.contratos.url}")
    private String instrumentsUrl;

    @Value("${pentaho.api.liquidacao.url}")
    private String liquidacaoUrl;

    @Value("${pentaho.api.uo.url}")
    private String uoUrl;

    @Value("${pentaho.api.po.url}")
    private String poUrl;

    @Value("${pentaho.api.fontes.url}")
    private String resourceSourcesUrl;

    @Value("${pentaho.api.userId}")
    private String pentahoUserId;

    @Value("${pentaho.api.password}")
    private String pentahoPassword;
    
    @Override
    public JsonNode getBudgetUnitList() throws Exception{
        RestTemplate restTemplate = restTemplateUtils.createRestTemplateWithNoSSL();
    
        restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));

        CompletableFuture<JsonNode> futureResponse = restTemplateUtils.createRequestWithAuth(
                restTemplate,
                uoUrl,
                pentahoUserId,
                pentahoPassword
        );
        return futureResponse.join().get(RESULTSET_FIELD);
    }
    
    @Override
    public JsonNode getBudgetPlanList(String codUo) throws Exception{
        RestTemplate restTemplate = restTemplateUtils.createRestTemplateWithNoSSL();
    
        restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));

        String url = poUrl + codUo;

      CompletableFuture<JsonNode> futureResponse = restTemplateUtils.createRequestWithAuth(restTemplate,
              url,
              pentahoUserId,
              pentahoPassword
      );
      return futureResponse.join().get(RESULTSET_FIELD);
    }
    
    @Override
    public JsonNode getInstrumentsList(String codUO, long startYear, long endYear) throws Exception{
        RestTemplate restTemplate = restTemplateUtils.createRestTemplateWithNoSSL();
    
        restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));

        String url = String.format(instrumentsUrl, codUO, startYear, endYear);

        CompletableFuture<JsonNode> futureResponse = restTemplateUtils.createRequestWithAuth(restTemplate,
                url,
                pentahoUserId,
                pentahoPassword
        );
      JsonNode response = futureResponse.join();

      return response.get(RESULTSET_FIELD);
    }

    @Override
    public JsonNode getLiquidatedValueByBudgetPlan(String codPO, String codUO) throws Exception {
        RestTemplate restTemplate;
        restTemplate = restTemplateUtils.createRestTemplateWithNoSSL();

        restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));

        String url = liquidacaoUrl + codPO + "&parampCodUo=" + codUO;

          CompletableFuture<JsonNode> futureResponse = restTemplateUtils.createRequestWithAuth(
                  restTemplate,
                  url,
                  pentahoUserId,
                  pentahoPassword
          );
          return futureResponse.join();
    }

    @Override
    public JsonNode getResourceSourceList(){
        final RestTemplate restTemplate;
        try {
            restTemplate = restTemplateUtils.createRestTemplateWithNoSSL();
        } catch (Exception exception) {
            throw new GlobalException("Falha ao configurar a consulta de fontes de recurso", exception);
        }

        restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));

        CompletableFuture<JsonNode> futureResponse = restTemplateUtils.createRequestWithAuth(
                restTemplate,
                resourceSourcesUrl,
                pentahoUserId,
                pentahoPassword
        );
        return futureResponse.join().get(RESULTSET_FIELD);
    }
    
    
    
}
