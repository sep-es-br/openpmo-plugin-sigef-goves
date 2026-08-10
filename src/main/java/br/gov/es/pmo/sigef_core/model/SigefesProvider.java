package br.gov.es.pmo.sigef_core.model;

import br.gov.es.pmo.sigef_core.util.RestTemplateUtils;
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

    @Value("${pentaho.api.userId}")
    private String pentahoUserId;

    @Value("${pentaho.api.password}")
    private String pentahoPassword;
    
    @Override
    public JsonNode getBudgetUnitList(){
        RestTemplate restTemplate = restTemplateUtils.createRestTemplateWithNoSSL();;
    
        restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));

        CompletableFuture<JsonNode> futureResponse = restTemplateUtils.createRequestWithAuth(
                restTemplate,
                uoUrl,
                pentahoUserId,
                pentahoPassword
        );
        return futureResponse.join();
    }
    
    @Override
    public JsonNode getBudgetPlanList(String codUo){
        RestTemplate restTemplate = restTemplateUtils.createRestTemplateWithNoSSL();
    
    restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));

    String url = poUrl + codUo;

      CompletableFuture<JsonNode> futureResponse = restTemplateUtils.createRequestWithAuth(restTemplate,
              url,
              pentahoUserId,
              pentahoPassword
      );
      return futureResponse.join();
    }
    
    public JsonNode getInstrumentsList(String codPO, String codUO, int startYear, int endYear);
    
    public JsonNode getLiquidatedValueByBudgetPlan(String codPO, String codUO);
    
    public JsonNode getResourceSourceList();
    
    
    
}