package cn.glogs.activeauth.iamcore.config;

import cn.glogs.activeauth.iamcore.config.properties.AuthConfiguration;
import cn.glogs.activeauth.iamcore.config.properties.MfaConfiguration;
import com.gitee.starblues.extension.support.SpringDocControllerProcessor;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.GroupedOpenApi;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

/**
 * SpringDoc Configurations
 * https://springdoc.org/
 *
 * @author Okeyja Teung
 * @since 2021-01-11 20:08 +08:00
 */
@Configuration
public class SpringDoc {

    public static final String API_KEY = "apiKey";
    public static final String VERIFICATION_TOKEN_$REF = "v_token";
    public static final String VERIFICATION_TOKEN_ID_$REF = "v_id";

    private final AuthConfiguration authConfiguration;

    public SpringDoc(AuthConfiguration authConfiguration) {
        this.authConfiguration = authConfiguration;
    }

    /**
     * API KEY
     * Header:Authorization
     *
     * @return bean:SecurityScheme
     * @author Okeyja Teung
     * @since 2021-01-11 20:08 +08:00
     */
    public SecurityScheme apiKeySecuritySchema() {
        return new SecurityScheme()
                .name(authConfiguration.getAuthorizationHeaderName()) // authorisation-token Constants.AUTHORISATION_TOKEN
                .description("HTTP header for token on any of your request.")
                .in(SecurityScheme.In.HEADER)
                .type(SecurityScheme.Type.APIKEY);
    }

    /**
     * See what's different between Swagger and SpringDoc:
     * https://swagger.io/blog/api-strategy/difference-between-swagger-and-openapi/
     * https://stackoverflow.com/questions/59291371/migrating-from-springfox-swagger2-to-springdoc-openapi
     *
     * @param mfaConfiguration bean:MfaConfiguration
     * @return bean:OpenAPI
     * @author Okeyja Teung
     * @since 2021-01-11 20:08 +08:00
     */
    @Bean
    public OpenAPI customOpenAPI(MfaConfiguration mfaConfiguration) {
        Parameter vTokenIdHeader = new HeaderParameter().name(mfaConfiguration.getVerificationTokenIdHeader()).in(ParameterIn.HEADER.toString()).schema(new StringSchema());
        Parameter vTokenHeader = new HeaderParameter().name(mfaConfiguration.getVerificationTokenHeader()).in(ParameterIn.HEADER.toString()).schema(new StringSchema());
        return new OpenAPI()
                .components(new Components()
                        .addParameters(VERIFICATION_TOKEN_ID_$REF, vTokenIdHeader)
                        .addParameters(VERIFICATION_TOKEN_$REF, vTokenHeader)
                        .addSecuritySchemes(API_KEY, apiKeySecuritySchema()) // define the apiKey SecuritySchema
                )
                .info(new Info().title("Active Auth IAM Core").description("Identity and Access Management Center of a Managed Microservice System."))
                .security(Collections.singletonList(new SecurityRequirement().addList(API_KEY))); // then apply it. If you don't apply it will not be added to the header in cURL
    }

    @Bean
    public GroupedOpenApi userOpenApi() {
        String paths[] = {"/user-center/**"};
        return GroupedOpenApi.builder().group("user-center").pathsToMatch(paths).build();
    }

    @Bean
    public GroupedOpenApi pluginSpecOpenApi() {
        String paths[] = {"/plugins/**"};
        return GroupedOpenApi.builder().group("plugins").pathsToMatch(paths).build();
    }

    @Bean
    public GroupedOpenApi allSpecOpenApi() {
        String paths[] = {"/**"};
        return GroupedOpenApi.builder().group("all").pathsToMatch(paths).build();
    }

    @Bean
    public SpringDocControllerProcessor springDocControllerProcessor(ApplicationContext applicationContext){
        return new SpringDocControllerProcessor(applicationContext);
    }

    /**
     * Add a global header customizer.
     * Or you have to add $ref on each method of your API controller.
     * <p>
     * https://github.com/springdoc/springdoc-openapi/issues/466
     * https://github.com/springdoc/springdoc-openapi/blob/master/springdoc-openapi-webmvc-core/src/test/java/test/org/springdoc/api/app39/SpringDocTestApp.java
     *
     * @return bean:OpenApiCustomiser
     * @author Okeyja Teung
     * @since 2021-01-11 20:08 +08:00
     */
    @Bean
    public OpenApiCustomiser customerGlobalHeaderOpenApiCustomiser() {
        return openApi -> openApi.getPaths().values().stream().flatMap(pathItem -> pathItem.readOperations().stream())
                .forEach(operation -> operation.addParametersItem(new HeaderParameter().$ref(VERIFICATION_TOKEN_$REF)));
    }
}
