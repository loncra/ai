package io.github.loncra.ai.turbo.seedance.video;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.loncra.ai.turbo.seedance.video.config.TurboSeedanceVideoProperties;
import org.springframework.ai.retry.autoconfigure.SpringAiRetryAutoConfiguration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestClient;

/**
 * Turbo 渠道 Seedance 视频 {@link TurboSeedanceVideoModel} 的自动配置。
 *
 * @author maurice.chen
 */
@AutoConfiguration(after = SpringAiRetryAutoConfiguration.class)
@ConditionalOnClass(TurboSeedanceVideoModel.class)
@ConditionalOnProperty(prefix = "spring.ai.turbo.seedance.video", name = "enabled", havingValue = "true",
		matchIfMissing = true)
@EnableConfigurationProperties(TurboSeedanceVideoProperties.class)
public class TurboSeedanceVideoAutoConfiguration {

	/**
	 * 注册 {@link TurboSeedanceVideoApiClient}；复用容器中已有的 {@link ObjectMapper} 与 {@link RestClient.Builder}，
	 * 若都不存在则使用默认实现。
	 */
	@Bean
	@ConditionalOnMissingBean
	public TurboSeedanceVideoApiClient turboSeedanceVideoApiClient(TurboSeedanceVideoProperties properties,
			ObjectProvider<ObjectMapper> objectMapperProvider,
			ObjectProvider<RestClient.Builder> restClientBuilderProvider) {
		ObjectMapper objectMapper = objectMapperProvider.getIfAvailable(ObjectMapper::new);
		RestClient.Builder builder = restClientBuilderProvider.getIfAvailable(RestClient::builder);
		TurboSeedanceVideoProperties.Http http = properties.getHttp() != null ? properties.getHttp()
				: new TurboSeedanceVideoProperties.Http();
		return new TurboSeedanceVideoApiClient(properties.getApiKey(), properties.getBaseUrl(),
				properties.getSubmitPath(), properties.getQueryPath(), properties.getContentPath(),
				properties.getConnectTimeout(), properties.getReadTimeout(), http.isUseAuthorizationBearer(),
				http.getHeaders(), builder, objectMapper);
	}

	@Bean
	@ConditionalOnMissingBean
	public TurboSeedanceVideoModel turboSeedanceVideoModel(TurboSeedanceVideoApiClient apiClient,
			TurboSeedanceVideoProperties properties, ObjectProvider<RetryTemplate> retryTemplateProvider) {
		TurboSeedanceVideoOptions defaultOptions = properties.toOptions();
		RetryTemplate retryTemplate = retryTemplateProvider.getIfAvailable(RetryTemplate::new);
		return new TurboSeedanceVideoModel(apiClient, defaultOptions, retryTemplate);
	}

}
