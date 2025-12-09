package uk.nhs.prm.deductions.pdsadaptor.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.MessageDigestPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ssm.SsmClient;
import uk.nhs.prm.deductions.pdsadaptor.service.ReadSSMParameter;

import java.util.Map;

@Configuration
@EnableWebSecurity
@Slf4j
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${environment}")
    private String environment;

    public PasswordEncoder passwordEncoder = new MessageDigestPasswordEncoder("SHA-256");

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        log.info("setting up client auth in configureGlobal");
        if (!environment.equals("local") && !environment.equals("int-test")) {
            ReadSSMParameter ssmService = new ReadSSMParameter(createSsmClient());
            Map<String, String> userMap = ssmService.getApiKeys(environment);

            userMap.forEach((parameterName, apiKey) -> {
                try {
                    String username = getUsernameFromParameter(parameterName);
                    auth.inMemoryAuthentication()
                        .passwordEncoder(passwordEncoder)
                        .withUser(username)
                        .password(passwordEncoder.encode(apiKey))
                            .roles("USER");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } else {
            //this for local usage only
            auth.inMemoryAuthentication()
                .passwordEncoder(passwordEncoder)
                .withUser("admin")
                .password(passwordEncoder.encode("admin"))
                    .roles("USER");
        }
        log.info("completed configureGlobal");
    }

    private SsmClient createSsmClient() {
        Region region = Region.EU_WEST_2;
        SsmClient ssmClient = SsmClient.builder()
                .region(region)
                .build();
        return ssmClient;
    }

    private String getUsernameFromParameter(String parameterName) {
        int indexOfSlashBeforeUsername = parameterName.lastIndexOf("/");
        return parameterName.substring(indexOfSlashBeforeUsername + 1);
    }

    private static final String[] AUTH_WHITELIST = {
            // -- Swagger UI v2
            "/v2/api-docs",
            "/swagger-resources",
            "/swagger-resources/**",
            "/configuration/ui",
            "/configuration/security",
            "/swagger-ui.html",
            "/swagger/**",
            "/webjars/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/actuator/health"
            // other public endpoints of your API may be appended to this array
    };

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and().
                httpBasic().and().
                authorizeRequests().
                antMatchers(AUTH_WHITELIST).permitAll().
                antMatchers("/**").authenticated();  // require authentication for any endpoint that's not whitelisted
    }

}
