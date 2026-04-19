package io.github.loncra.ai.turbo.seedance.video;

import io.github.loncra.ai.turbo.seedance.video.config.TurboSeedanceVideoProperties;
import org.springframework.ai.retry.autoconfigure.SpringAiRetryAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Google Gemini Nano Banana {@link org.springframework.ai.image.ImageModel} 的自动配置。
 *
 * @author maurice.chen
 */
@Configuration
@AutoConfiguration(after = SpringAiRetryAutoConfiguration.class)
@EnableConfigurationProperties(TurboSeedanceVideoProperties.class)
public class TurboSeedanceVideoAutoConfiguration {

    @Bean
    public TurboSeedanceVideoModel turboSeedanceVideoModel(TurboSeedanceVideoProperties properties) {
        return new TurboSeedanceVideoModel();
    }

}
