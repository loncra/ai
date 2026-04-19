package io.github.loncra.ai.genai.image;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.genai.Client;
import io.github.loncra.ai.genai.image.config.GoogleGenAiConnectionExtProperties;
import io.github.loncra.ai.genai.image.config.GoogleGenAiImageProperties;
import io.micrometer.observation.ObservationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.model.SpringAIModelProperties;
import org.springframework.ai.model.SpringAIModels;
import org.springframework.ai.model.google.genai.autoconfigure.chat.GoogleGenAiChatAutoConfiguration;
import org.springframework.ai.model.google.genai.autoconfigure.chat.GoogleGenAiConnectionProperties;
import org.springframework.ai.retry.autoconfigure.SpringAiRetryAutoConfiguration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * Google Gemini Nano Banana {@link org.springframework.ai.image.ImageModel} 的自动配置。
 *
 * @author maurice.chen
 */
@Configuration
@AutoConfiguration(before = GoogleGenAiChatAutoConfiguration.class, after = SpringAiRetryAutoConfiguration.class)
@ConditionalOnClass({Client.class, GoogleGenAiImageModel.class})
@ConditionalOnProperty(name = SpringAIModelProperties.IMAGE_MODEL, havingValue = SpringAIModels.GOOGLE_GEN_AI,
        matchIfMissing = true)
@EnableConfigurationProperties({GoogleGenAiImageProperties.class, GoogleGenAiConnectionExtProperties.class})
public class GoogleGenAiImageAutoConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleGenAiImageAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    public Client googleGenAiClient(GoogleGenAiConnectionExtProperties properties) throws IOException {
        Client.Builder builder = Client.builder();

        boolean hasApiKey = StringUtils.hasText(properties.getApiKey());
        boolean hasProject = StringUtils.hasText(properties.getProjectId());
        boolean hasLocation = StringUtils.hasText(properties.getLocation());
        boolean hasVertexConfig = hasProject && hasLocation;

        // Ambiguity Guard: Professional logging
        if (hasApiKey && hasVertexConfig) {
            if (properties.isVertexAi()) {
                LOGGER.info(
                        "Both API Key and Vertex AI config detected. Vertex AI mode is explicitly enabled; the API key will be ignored.");
            }
            else {
                LOGGER.warn("Both API Key and Vertex AI config detected. Defaulting to Gemini Developer API (API Key). "
                        + "To use Vertex AI instead, set 'spring.ai.google.genai.vertex-ai=true'.");
            }
        }

        // Mode Selection with Fail-Fast Validation
        if (properties.isVertexAi()) {
            if (!hasVertexConfig) {
                throw new IllegalStateException(
                        "Vertex AI mode requires both 'project-id' and 'location' to be configured.");
            }
            configureVertexAi(builder, properties);
        }
        else if (hasApiKey) {
            builder.apiKey(properties.getApiKey());
        }
        else if (hasVertexConfig) {
            LOGGER.debug("Project ID and Location detected. Defaulting to Vertex AI mode.");
            configureVertexAi(builder, properties);
        }
        else {
            throw new IllegalStateException("Incomplete Google GenAI configuration: Provide 'api-key' for Gemini API "
                    + "or 'project-id' and 'location' for Vertex AI.");
        }
        properties.applyHttpOptions(builder);
        return builder.build();
    }

    private void configureVertexAi(Client.Builder builder, GoogleGenAiConnectionProperties props) throws IOException {
        Assert.hasText(props.getProjectId(), "Google GenAI project-id must be set for Vertex AI mode!");
        Assert.hasText(props.getLocation(), "Google GenAI location must be set for Vertex AI mode!");

        builder.project(props.getProjectId()).location(props.getLocation()).vertexAI(true);

        if (props.getCredentialsUri() != null) {
            try (var is = props.getCredentialsUri().getInputStream()) {
                builder.credentials(GoogleCredentials.fromStream(is));
            }
        }
    }

    /**
     * 注册 Nano Banana 图像 {@link org.springframework.ai.image.ImageModel} 实现。
     */
    @Bean
    @ConditionalOnMissingBean
    public GoogleGenAiImageModel googleGenAiImageModel(
            Client client,
            GoogleGenAiImageProperties properties,
            RetryTemplate retryTemplate,
            ObjectProvider<ObservationRegistry> observationRegistry
    ) {
        return new GoogleGenAiImageModel(
                client, properties.toImageOptions(), retryTemplate,
                observationRegistry.getIfAvailable(() -> ObservationRegistry.NOOP)
        );
    }

}
