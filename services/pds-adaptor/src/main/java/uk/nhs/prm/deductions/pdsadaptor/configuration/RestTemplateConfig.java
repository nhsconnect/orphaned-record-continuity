package uk.nhs.prm.deductions.pdsadaptor.configuration;

import lombok.RequiredArgsConstructor;
import org.apache.http.impl.client.HttpClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
@RequiredArgsConstructor
public class RestTemplateConfig {

    @Bean
    public RestTemplate apacheBasedRestTemplate() {
        return new RestTemplate(apacheHttpClientRequestFactory());
    }

    private HttpComponentsClientHttpRequestFactory apacheHttpClientRequestFactory() {
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setConnectTimeout(11000);
        requestFactory.setReadTimeout(11000);
        requestFactory.setHttpClient(HttpClients.custom().setMaxConnTotal(50).setMaxConnPerRoute(10).build());
        return requestFactory;
    }
}
