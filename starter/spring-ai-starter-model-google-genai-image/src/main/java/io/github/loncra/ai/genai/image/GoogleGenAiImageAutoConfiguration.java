package io.github.loncra.ai.genai.image;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.auth.http.AuthHttpConstants;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.genai.Client;
import com.google.genai.JsonSerializable;
import com.google.genai.types.GenerateContentResponseUsageMetadata;
import com.google.genai.types.HttpOptions;
import io.github.loncra.ai.genai.image.config.GoogleGenAiConnectionExtProperties;
import io.github.loncra.ai.genai.image.config.GoogleGenAiImageProperties;
import io.micrometer.observation.ObservationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.model.SpringAIModelProperties;
import org.springframework.ai.model.SpringAIModels;
import org.springframework.ai.model.google.genai.autoconfigure.chat.GoogleGenAiConnectionProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.Serial;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Google Gemini Nano Banana {@link org.springframework.ai.image.ImageModel} 的自动配置。
 *
 * @author maurice.chen
 */
@Configuration
@ConditionalOnClass({Client.class, GoogleGenAiImageModel.class})
@ConditionalOnProperty(name = SpringAIModelProperties.IMAGE_MODEL, havingValue = SpringAIModels.GOOGLE_GEN_AI,
        matchIfMissing = true)
@EnableConfigurationProperties({GoogleGenAiImageProperties.class, GoogleGenAiConnectionExtProperties.class, GoogleGenAiConnectionProperties.class})
public class GoogleGenAiImageAutoConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleGenAiImageAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    public Client googleGenAiClient(
            GoogleGenAiConnectionProperties properties,
            GoogleGenAiConnectionExtProperties extProperties
    ) throws IOException {
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
            } else {
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
        } else if (hasApiKey) {
            builder.apiKey(properties.getApiKey());
        } else if (hasVertexConfig) {
            LOGGER.debug("Project ID and Location detected. Defaulting to Vertex AI mode.");
            configureVertexAi(builder, properties);
        } else {
            throw new IllegalStateException("Incomplete Google GenAI configuration: Provide 'api-key' for Gemini API "
                    + "or 'project-id' and 'location' for Vertex AI.");
        }
        applyHttpOptions(builder, properties, extProperties);
        overrideJsonSerializable();
        return builder.build();
    }

    private static void overrideJsonSerializable() {
        ObjectMapper sdkMapper = JsonSerializable.objectMapper();
        final ObjectMapper baselineBeforeCompat = sdkMapper.copy();
        SimpleModule usageMetadataNullCompat = new SimpleModule("usage-metadata-builder-null-compat");
        usageMetadataNullCompat.setDeserializerModifier(new BeanDeserializerModifier() {
            @Serial
            private static final long serialVersionUID = 4084206737465681812L;

            @Override
            public JsonDeserializer<?> modifyDeserializer(
                    DeserializationConfig config, BeanDescription beanDesc,
                    JsonDeserializer<?> deserializer) {
                Class<?> beanClass = beanDesc.getBeanClass();
                if (beanClass == null || !GenerateContentResponseUsageMetadata.Builder.class.isAssignableFrom(beanClass)) {
                    return deserializer;
                }
                return new JsonDeserializer<>() {
                    @Override
                    public GenerateContentResponseUsageMetadata deserialize(JsonParser p, DeserializationContext ctxt)
                            throws IOException {
                        JsonNode node = p.getCodec().readTree(p);
                        if (node instanceof ObjectNode usageObj) {
                            if (usageObj.get("promptTokensDetails") != null && usageObj.get("promptTokensDetails").isNull()) {
                                usageObj.remove("promptTokensDetails");
                            }
                            if (usageObj.get("toolUsePromptTokensDetails") != null
                                    && usageObj.get("toolUsePromptTokensDetails").isNull()) {
                                usageObj.remove("toolUsePromptTokensDetails");
                            }
                        }
                        return baselineBeforeCompat.treeToValue(node, GenerateContentResponseUsageMetadata.class);
                    }
                };
            }
        });
        sdkMapper.registerModule(usageMetadataNullCompat);
    }

    private void configureVertexAi(
            Client.Builder builder,
            GoogleGenAiConnectionProperties props
    ) throws IOException {
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

    /**
     * 合并 {@link HttpOptions}：自定义根地址、附加头，以及 {@code use-authorization-bearer} 时追加 {@code Authorization: Bearer}。
     * <p>
     * 说明：SDK 在设置了 {@link Client.Builder#apiKey(String)} 时仍会发送 {@code x-goog-api-key}；
     * 第三方若仅需 {@code Authorization}，通常可忽略多余头；若渠道严格拒绝双鉴权，需与渠道确认或考虑直连 HTTP。
     */
    public void applyHttpOptions(
            Client.Builder builder,
            GoogleGenAiConnectionProperties properties,
            GoogleGenAiConnectionExtProperties extProperties
    ) {
        GoogleGenAiConnectionExtProperties.Http http = extProperties.getHttp();
        Map<String, String> merged = new LinkedHashMap<>();
        if (http != null && http.getHeaders() != null) {
            merged.putAll(http.getHeaders());
        }
        if (http != null && http.isUseAuthorizationBearer() && org.apache.commons.lang3.StringUtils.isNotEmpty(properties.getApiKey())) {
            if (!hasAuthorizationHeader(merged)) {
                merged.put(HttpHeaders.AUTHORIZATION, AuthHttpConstants.BEARER + org.apache.commons.lang3.StringUtils.SPACE + properties.getApiKey().trim());
            }
        }

        boolean hasBaseUrl = org.apache.commons.lang3.StringUtils.isNotEmpty(extProperties.getBaseUrl());
        boolean hasHeaders = !merged.isEmpty();
        if (!hasBaseUrl && !hasHeaders) {
            return;
        }

        HttpOptions.Builder hb = HttpOptions.builder();
        if (hasBaseUrl) {
            String base = extProperties.getBaseUrl().trim();
            hb.baseUrl(base);
            LOGGER.info("HTTP base URL：{}", base);
        }
        if (hasHeaders) {
            hb.headers(merged);
            LOGGER.info("客户端已附加 {} 个 HTTP 头（不含敏感值）。", merged.size());
        }
        builder.httpOptions(hb.build());
    }

    private boolean hasAuthorizationHeader(Map<String, String> headers) {
        if (headers == null || headers.isEmpty()) {
            return false;
        }
        for (String k : headers.keySet()) {
            if (k != null && HttpHeaders.AUTHORIZATION.equalsIgnoreCase(k.trim())) {
                return org.apache.commons.lang3.StringUtils.isNotEmpty(headers.get(k));
            }
        }
        return false;
    }

}
