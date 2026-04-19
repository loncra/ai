package io.github.loncra.ai.genai.image.config;

import com.google.auth.http.AuthHttpConstants;
import com.google.genai.Client;
import com.google.genai.types.HttpOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.model.google.genai.autoconfigure.chat.GoogleGenAiConnectionProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpHeaders;

import java.util.LinkedHashMap;
import java.util.Map;

@ConfigurationProperties(GoogleGenAiConnectionProperties.CONFIG_PREFIX)
public class GoogleGenAiConnectionExtProperties extends GoogleGenAiConnectionProperties {

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
     * 合并 {@link HttpOptions}：自定义根地址、附加头，以及 {@code use-authorization-bearer} 时追加 {@code Authorization: Bearer}。
     * <p>
     * 说明：SDK 在设置了 {@link Client.Builder#apiKey(String)} 时仍会发送 {@code x-goog-api-key}；
     * 第三方若仅需 {@code Authorization}，通常可忽略多余头；若渠道严格拒绝双鉴权，需与渠道确认或考虑直连 HTTP。
     */
    public void applyHttpOptions(Client.Builder builder) {
        GoogleGenAiConnectionExtProperties.Http http = getHttp();
        Map<String, String> merged = new LinkedHashMap<>();
        if (http != null && http.getHeaders() != null) {
            merged.putAll(http.getHeaders());
        }
        if (http != null && http.isUseAuthorizationBearer() && org.apache.commons.lang3.StringUtils.isNotEmpty(getApiKey())) {
            if (!hasAuthorizationHeader(merged)) {
                merged.put(HttpHeaders.AUTHORIZATION, AuthHttpConstants.BEARER + org.apache.commons.lang3.StringUtils.SPACE + getApiKey().trim());
            }
        }

        boolean hasBaseUrl = org.apache.commons.lang3.StringUtils.isNotEmpty(getBaseUrl());
        boolean hasHeaders = !merged.isEmpty();
        if (!hasBaseUrl && !hasHeaders) {
            return;
        }

        HttpOptions.Builder hb = HttpOptions.builder();
        if (hasBaseUrl) {
            String base = getBaseUrl().trim();
            hb.baseUrl(base);
            LOGGER.info("HTTP base URL：{}", base);
        }
        if (hasHeaders) {
            hb.headers(merged);
            LOGGER.info("客户端已附加 {} 个 HTTP 头（不含敏感值）。", merged.size());
        }
        builder.httpOptions(hb.build());
    }

    private static boolean hasAuthorizationHeader(Map<String, String> headers) {
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
