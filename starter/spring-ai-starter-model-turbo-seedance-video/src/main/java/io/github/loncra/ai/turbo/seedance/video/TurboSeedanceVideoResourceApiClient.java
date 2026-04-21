package io.github.loncra.ai.turbo.seedance.video;

import com.fasterxml.jackson.core.type.TypeReference;
import io.github.loncra.ai.turbo.seedance.video.config.TurboSeedanceVideoProperties;
import io.github.loncra.ai.turbo.seedance.video.domian.body.*;
import io.github.loncra.framework.commons.CastUtils;
import io.github.loncra.framework.commons.RestResult;
import io.github.loncra.framework.commons.exception.ErrorCodeException;
import io.github.loncra.framework.commons.exception.SystemException;
import io.github.loncra.framework.commons.page.Page;
import io.github.loncra.framework.commons.page.PageRequest;
import io.github.loncra.framework.commons.page.TotalPage;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Turbo Seedance 素材库底层 HTTP 客户端。
 *
 * @author maurice.chen
 */
public class TurboSeedanceVideoResourceApiClient {

    private static final Logger logger = LoggerFactory.getLogger(TurboSeedanceVideoResourceApiClient.class);

    private final RestClient restClient;

    private final TurboSeedanceVideoProperties properties;

    public TurboSeedanceVideoResourceApiClient(
            TurboSeedanceVideoProperties properties,
            RestClient.Builder restClientBuilder
    ) {
        Assert.notNull(properties, "properties 不能为 null");
        Assert.hasText(properties.getBaseUrl(), "baseUrl 不能为空");
        this.properties = properties;
        RestClient.Builder builder = restClientBuilder != null ? restClientBuilder : RestClient.builder();
        this.restClient = builder.baseUrl(properties.getBaseUrl())
                .requestFactory(buildRequestFactory(properties.getConnectTimeout(), properties.getReadTimeout()))
                .build();
    }

    public TurboSeedanceVideoResourceAssetGroup createAssetGroup(
            TurboSeedanceVideoResourceAssetGroupRequest request
    ) {
        Assert.notNull(request, "request 不能为 null");
        Assert.hasText(request.getName(), "资源组名称不能为空");
        ResponseEntity<String> response = execute(properties.getAssetGroupPath(), CastUtils.convertValue(request, CastUtils.MAP_TYPE_REFERENCE));
        return SystemException.convertSupplier(() -> CastUtils.getObjectMapper().readValue(response.getBody(), TurboSeedanceVideoResourceAssetGroup.class));
    }

    public TurboSeedanceVideoResourceAssetGroupListResponse listAssetGroups() {
        ResponseEntity<String> response = execute(properties.getAssetGroupPath(), HttpMethod.GET);
        validResponseEntity(response);
        return SystemException.convertSupplier(() -> CastUtils.getObjectMapper().readValue(response.getBody(), TurboSeedanceVideoResourceAssetGroupListResponse.class));
    }

    public TurboSeedanceVideoResourceAsset createAsset(TurboSeedanceVideoResourceAssetRequest request) {
        Assert.notNull(request, "request 不能为 null");
        Assert.hasText(request.getGroupId(), "groupId 不能为空");
        Assert.hasText(request.getUrl(), "url 不能为空");
        ResponseEntity<String> response = execute(properties.getAssetPath(), CastUtils.convertValue(request, CastUtils.MAP_TYPE_REFERENCE));
        return SystemException.convertSupplier(() -> CastUtils.getObjectMapper().readValue(response.getBody(), TurboSeedanceVideoResourceAsset.class));
    }

    public TurboSeedanceVideoResourceAsset getAsset(String officialId) {
        Assert.hasText(officialId, "officialId 不能为空");
        ResponseEntity<String> response = execute(properties.getAssetPath() + "/" + officialId, HttpMethod.GET);
        validResponseEntity(response);
        return SystemException.convertSupplier(() -> CastUtils.getObjectMapper().readValue(response.getBody(), TurboSeedanceVideoResourceAsset.class));
    }

    public Page<TurboSeedanceVideoResourceAsset> findAsset(PageRequest pageRequest, String groupId, String status) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath(properties.getAssetPath());
        if (StringUtils.isNotBlank(groupId)) {
            builder.queryParam("groupId",groupId);
        }
        if (StringUtils.isNotBlank(status)) {
            builder.queryParam("status", status);
        }
        ResponseEntity<String> response = execute(builder.build().encode().toUriString(), HttpMethod.GET);
        validResponseEntity(response);
        Map<String, Object> object = SystemException.convertSupplier(() -> CastUtils.getObjectMapper().readValue(response.getBody(), CastUtils.MAP_TYPE_REFERENCE));
        List<TurboSeedanceVideoResourceAsset> list = CastUtils.convertValue(object.get(RestResult.DEFAULT_DATA_NAME), new TypeReference<>() {});
        return new TotalPage<>(pageRequest, list, NumberUtils.toInt(object.getOrDefault("total", "0").toString()));
    }

    public void deleteAsset(String officialId) {
        Assert.hasText(officialId, "officialId 不能为空");
        ResponseEntity<String> response = execute(properties.getAssetPath() + "/" + officialId, HttpMethod.DELETE);
        validResponseEntity(response);
    }

    private ResponseEntity<String> execute(String url, Map<String, Object> body) {
        var client = createBasicRequestClient(url, HttpMethod.POST).contentType(MediaType.APPLICATION_JSON);
        if (Objects.nonNull(body)) {
            client.body(body);
        }

        ResponseEntity<String> response = client.retrieve().toEntity(String.class);
        validResponseEntity(response);

        return response;
    }

    private ResponseEntity<String> execute(String url, MultiValueMap<String, Object> form) {
        var client = createBasicRequestClient(url, HttpMethod.POST).contentType(MediaType.MULTIPART_FORM_DATA);
        if (Objects.nonNull(form)) {
            client.body(form);
        }

        ResponseEntity<String> response = client.retrieve().toEntity(String.class);
        validResponseEntity(response);

        return response;
    }

    private ResponseEntity<String> execute(String url, HttpMethod method) {
        var client = createBasicRequestClient(url, method);
        ResponseEntity<String> response = client.retrieve().toEntity(String.class);
        validResponseEntity(response);

        return response;
    }

    private RestClient.RequestBodySpec createBasicRequestClient(String url, HttpMethod method) {
        String executeUrl = properties.getBaseUrl() + url;
        return this.restClient.method(method).uri(executeUrl).headers(this::applyAuthHeaders).contentType(MediaType.APPLICATION_JSON);
    }

    private void validResponseEntity(
            ResponseEntity<String> response
    ) {
        HttpStatusCode status = response.getStatusCode();
        ErrorCodeException.isTrue(status.is2xxSuccessful(), "[" + HttpStatus.valueOf(status.value()).getReasonPhrase() + "]", String.valueOf(status.value()));
    }

    private void applyAuthHeaders(HttpHeaders headers) {
        TurboSeedanceVideoProperties.Http http = this.properties.getHttp() != null ? this.properties.getHttp()
                : new TurboSeedanceVideoProperties.Http();
        if (http.isUseAuthorizationBearer() && StringUtils.isNotEmpty(this.properties.getApiKey())
                && !headers.containsKey(HttpHeaders.AUTHORIZATION)) {
            headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + this.properties.getApiKey());
        }
        Map<String, String> extraHeaders = http.getHeaders();
        if (extraHeaders != null) {
            extraHeaders.forEach(headers::set);
        }
    }

    private static ClientHttpRequestFactory buildRequestFactory(
            Duration connectTimeout,
            Duration readTimeout
    ) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        if (connectTimeout != null) {
            factory.setConnectTimeout((int) connectTimeout.toMillis());
        }
        if (readTimeout != null) {
            factory.setReadTimeout((int) readTimeout.toMillis());
        }
        return factory;
    }

}
