package io.github.loncra.ai.turbo.seedance.video.domian;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * {@code metadata.content} 数组里的单个多媒体参考素材。
 * <p>
 * 对应文档（{@code src/test/resources/readme.html} 第 2.3 节）：
 * <pre>
 * {
 *   "type": "image_url",
 *   "image_url": { "url": "https://example.com/demo.png" },
 *   "role": "reference_image"
 * }
 * </pre>
 *
 * @author maurice.chen
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TurboSeedanceVideoContent implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 素材类型：{@code text} / {@code image_url} / {@code video_url} / {@code audio_url}。
	 */
	public static final String TYPE_TEXT = "text";

	public static final String TYPE_IMAGE_URL = "image_url";

	public static final String TYPE_VIDEO_URL = "video_url";

	public static final String TYPE_AUDIO_URL = "audio_url";

	private String type;

	private String text;

	@JsonProperty("image_url")
	private UrlHolder imageUrl;

	@JsonProperty("video_url")
	private UrlHolder videoUrl;

	@JsonProperty("audio_url")
	private UrlHolder audioUrl;

	/**
	 * 平台用于识别素材定位的可选角色描述（例如 {@code reference_image} / {@code reference_video}）。
	 */
	private String role;

	public TurboSeedanceVideoContent() {
	}

	public static TurboSeedanceVideoContent text(String text) {
		TurboSeedanceVideoContent c = new TurboSeedanceVideoContent();
		c.type = TYPE_TEXT;
		c.text = text;
		return c;
	}

	public static TurboSeedanceVideoContent imageUrl(String url) {
		return imageUrl(url, null);
	}

	public static TurboSeedanceVideoContent imageUrl(String url, String role) {
		TurboSeedanceVideoContent c = new TurboSeedanceVideoContent();
		c.type = TYPE_IMAGE_URL;
		c.imageUrl = new UrlHolder(url);
		c.role = role;
		return c;
	}

	public static TurboSeedanceVideoContent videoUrl(String url) {
		return videoUrl(url, null);
	}

	public static TurboSeedanceVideoContent videoUrl(String url, String role) {
		TurboSeedanceVideoContent c = new TurboSeedanceVideoContent();
		c.type = TYPE_VIDEO_URL;
		c.videoUrl = new UrlHolder(url);
		c.role = role;
		return c;
	}

	public static TurboSeedanceVideoContent audioUrl(String url) {
		return audioUrl(url, null);
	}

	public static TurboSeedanceVideoContent audioUrl(String url, String role) {
		TurboSeedanceVideoContent c = new TurboSeedanceVideoContent();
		c.type = TYPE_AUDIO_URL;
		c.audioUrl = new UrlHolder(url);
		c.role = role;
		return c;
	}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getText() {
		return this.text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public UrlHolder getImageUrl() {
		return this.imageUrl;
	}

	public void setImageUrl(UrlHolder imageUrl) {
		this.imageUrl = imageUrl;
	}

	public UrlHolder getVideoUrl() {
		return this.videoUrl;
	}

	public void setVideoUrl(UrlHolder videoUrl) {
		this.videoUrl = videoUrl;
	}

	public UrlHolder getAudioUrl() {
		return this.audioUrl;
	}

	public void setAudioUrl(UrlHolder audioUrl) {
		this.audioUrl = audioUrl;
	}

	public String getRole() {
		return this.role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof TurboSeedanceVideoContent that)) {
			return false;
		}
		return Objects.equals(this.type, that.type) && Objects.equals(this.text, that.text)
				&& Objects.equals(this.imageUrl, that.imageUrl) && Objects.equals(this.videoUrl, that.videoUrl)
				&& Objects.equals(this.audioUrl, that.audioUrl) && Objects.equals(this.role, that.role);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.type, this.text, this.imageUrl, this.videoUrl, this.audioUrl, this.role);
	}

	/**
	 * {@code image_url} / {@code video_url} / {@code audio_url} 字段包装对象，对应 <code>{ "url": "..." }</code> 结构。
	 */
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class UrlHolder implements Serializable {

		@Serial
		private static final long serialVersionUID = 1L;

		private String url;

		public UrlHolder() {
		}

		public UrlHolder(String url) {
			this.url = url;
		}

		public String getUrl() {
			return this.url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof UrlHolder that)) {
				return false;
			}
			return Objects.equals(this.url, that.url);
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.url);
		}

	}

}
