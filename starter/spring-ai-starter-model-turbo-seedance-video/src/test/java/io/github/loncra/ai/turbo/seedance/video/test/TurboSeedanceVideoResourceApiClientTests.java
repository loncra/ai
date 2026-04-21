package io.github.loncra.ai.turbo.seedance.video.test;

import com.sun.net.httpserver.HttpExchange;
import io.github.loncra.ai.turbo.seedance.video.TurboSeedanceVideoResourceApiClient;
import io.github.loncra.ai.turbo.seedance.video.domian.body.*;
import io.github.loncra.ai.turbo.seedance.video.enumerate.TurboSeedanceVideoResourceStatus;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class TurboSeedanceVideoResourceApiClientTests {

	@Autowired
	private TurboSeedanceVideoResourceApiClient client;

	@Test
	void assetGroupsList() throws Exception {

		TurboSeedanceVideoResourceAssetGroupRequest request = new TurboSeedanceVideoResourceAssetGroupRequest();
		request.setName("Turbo Seedance Video Resource Library");

		TurboSeedanceVideoResourceAssetGroup response = client.createAssetGroup(request);

		assertNotNull(response);
		assertEquals("Turbo Seedance Video Resource Library", response.getName());

		TurboSeedanceVideoResourceAssetGroupListResponse list = client.listAssetGroups();

		assertNotNull(response);
		assertTrue(list.getValue().stream().anyMatch(s -> Strings.CS.equals(s.getName(), "Turbo Seedance Video Resource Library")));
	}

	/*@Test
	void createAssetPostsJsonAndParsesResponse() throws Exception {
		AtomicReference<RecordedRequest> recorded = new AtomicReference<>();
		TurboSeedanceVideoResourceAssetRequest request = new TurboSeedanceVideoResourceAssetRequest();
		request.setGroupId("ag-2026xyz");
		request.setUrl("https://cdn.example.com/character.png");
		request.setName("Main character");

		TurboSeedanceVideoResourceAsset response = client.createAsset(request);

		assertEquals("POST", recorded.get().method());
		assertEquals("/turbo_data/assets", recorded.get().path());
		assertTrue(recorded.get().body().contains("\"groupId\":\"ag-2026xyz\""));
		assertTrue(recorded.get().body().contains("\"url\":\"https://cdn.example.com/character.png\""));
		assertEquals("asset-2026character", response.getOfficialId());
		assertEquals(TurboSeedanceVideoResourceStatus.PROCESSING, response.getStatus());
	}

	@Test
	void listAssetsBuildsQueryAndParsesWrappedItems() throws Exception {
		AtomicReference<RecordedRequest> recorded = new AtomicReference<>();
		TurboSeedanceVideoResourceAssetListRequest request = new TurboSeedanceVideoResourceAssetListRequest();
		request.setGroupId("ag-2026xyz");
		request.setStatus("Active");
		request.setPageNum(1);
		request.setPageSize(20);

		List<TurboSeedanceVideoResourceAsset> response = client.listAssets(request);

	}

	@Test
	void getAssetUsesOfficialIdPathAndParsesResponse() throws Exception {
		AtomicReference<RecordedRequest> recorded = new AtomicReference<>();
		TurboSeedanceVideoResourceAsset response = client.getAsset("asset-2026character");

		assertEquals("GET", recorded.get().method());
		assertEquals("/turbo_data/assets/asset-2026character", recorded.get().path());
		assertEquals("asset-2026character", response.getOfficialId());
		assertEquals(TurboSeedanceVideoResourceStatus.ACTIVE, response.getStatus());
	}

	@Test
	void deleteAssetUsesOfficialIdPath() throws Exception {
		AtomicReference<RecordedRequest> recorded = new AtomicReference<>();
		client.deleteAsset("asset-2026character");

		assertEquals("DELETE", recorded.get().method());
		assertEquals("/turbo_data/assets/asset-2026character", recorded.get().path());
	}

	@Test
	void batchCreateAssetsPostsJsonToBatchEndpoint() throws Exception {
		AtomicReference<RecordedRequest> recorded = new AtomicReference<>();
		TurboSeedanceVideoResourceBatchAssetItem item1 = new TurboSeedanceVideoResourceBatchAssetItem();
		item1.setUrl("https://cdn.example.com/character.png");
		item1.setName("Main character");
		TurboSeedanceVideoResourceBatchAssetItem item2 = new TurboSeedanceVideoResourceBatchAssetItem();
		item2.setUrl("https://cdn.example.com/product-spin.mp4");
		item2.setName("Turntable video");
		TurboSeedanceVideoResourceBatchAssetRequest request = new TurboSeedanceVideoResourceBatchAssetRequest();
		request.setGroupId("ag-2026xyz");
		request.setAssets(java.util.List.of(item1, item2));

		TurboSeedanceVideoResourceAssetListResponse response = client.batchCreateAssets(request);

		assertEquals("POST", recorded.get().method());
		assertEquals("/turbo_data/assets/batch", recorded.get().path());
		assertTrue(recorded.get().body().contains("\"groupId\":\"ag-2026xyz\""));
		assertTrue(recorded.get().body().contains("\"url\":\"https://cdn.example.com/character.png\""));
		assertTrue(recorded.get().body().contains("\"url\":\"https://cdn.example.com/product-spin.mp4\""));
		assertEquals(2, response.getItems().size());
		assertEquals(2L, response.getTotal());
	}

	@Test
	void batchUploadAssetsFileUsesMultipartBatchPath() throws Exception {
		AtomicReference<RecordedRequest> recorded = new AtomicReference<>();

		ByteArrayResource file = new ByteArrayResource("url,name\nhttps://cdn.example.com/a.png,Main".getBytes(StandardCharsets.UTF_8)) {
			@Override
			public String getFilename() {
				return "assets.csv";
			}
		};

		client.batchUploadAssetsFile("ag-2026xyz", file);

		assertEquals("POST", recorded.get().method());
		assertEquals("/turbo_data/assets/batch", recorded.get().path());
		assertTrue(recorded.get().headers().getFirst(HttpHeaders.CONTENT_TYPE).contains("multipart/form-data"));
		assertTrue(recorded.get().body().contains("ag-2026xyz"));
		assertTrue(recorded.get().body().contains("assets.csv"));
	}

	private static RecordedRequest readRequest(HttpExchange exchange) throws IOException {
		byte[] body = exchange.getRequestBody().readAllBytes();
		return new RecordedRequest(exchange.getRequestMethod(), exchange.getRequestURI().getPath(),
				exchange.getRequestURI().getQuery(), exchange.getRequestHeaders(),
				new String(body, StandardCharsets.UTF_8));
	}

	private static void writeJson(HttpExchange exchange, String body) throws IOException {
		byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
		exchange.getResponseHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");
		exchange.sendResponseHeaders(200, bytes.length);
		exchange.getResponseBody().write(bytes);
		exchange.close();
	}

	private record RecordedRequest(String method, String path, String query, com.sun.net.httpserver.Headers headers,
			String body) {
	}*/

}
