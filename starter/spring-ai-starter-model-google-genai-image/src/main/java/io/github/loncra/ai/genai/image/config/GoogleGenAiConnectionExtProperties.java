package io.github.loncra.ai.genai.image.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.model.google.genai.autoconfigure.chat.GoogleGenAiConnectionProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(GoogleGenAiConnectionProperties.CONFIG_PREFIX)
public class GoogleGenAiConnectionExtProperties {

    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleGenAiConnectionExtProperties.class);

    /**
     * 可选：自定义 HTTP 根地址（如国内渠道商兼容 Gemini 的网关）。为空时使用 google-genai SDK 默认的 Google 端点。
     * 示例：https://example.com
     */
    private String baseUrl;

    private Http http = new Http();

    public GoogleGenAiConnectionExtProperties() {
    }

    public Http getHttp() {
        return this.http;
    }

    public void setHttp(Http http) {
        this.http = http != null ? http : new Http();
    }

    public String getBaseUrl() {
        return this.baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * 附加到 google-genai {@link com.google.genai.types.HttpOptions} 的配置（如第三方网关的 {@code Authorization} 头）。
     *
     * @author maurice.chen
     */
    public static class Http {

        /**
         * 为 true 且使用 {@link GoogleGenAiConnectionProperties#getApiKey()} 时：在 {@link com.google.genai.types.HttpOptions} 中附加
         * {@code Authorization: Bearer &lt;api-key&gt;}。google-genai 构建 {@link com.google.genai.Client} 时仍须设置 {@code apiKey()}，
         * SDK 可能仍会发送 {@code x-goog-api-key}；若渠道仅需 Bearer，请与渠道确认是否可忽略多余头。
         */
        private boolean useAuthorizationBearer;

        /**
         * 附加 HTTP 头（会与 {@link #useAuthorizationBearer} 生成的 {@code Authorization} 合并；若已显式配置 {@code Authorization} 则不再自动追加 Bearer）。
         */
        private Map<String, String> headers;

        public boolean isUseAuthorizationBearer() {
            return this.useAuthorizationBearer;
        }

        public void setUseAuthorizationBearer(boolean useAuthorizationBearer) {
            this.useAuthorizationBearer = useAuthorizationBearer;
        }

        public Map<String, String> getHeaders() {
            return this.headers;
        }

        public void setHeaders(Map<String, String> headers) {
            this.headers = headers;
        }

    }
}
