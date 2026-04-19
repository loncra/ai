package io.github.loncra.ai.turbo.seedance.video;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.loncra.ai.turbo.seedance.video.domian.metadata.TurboSeedanceVideoContent;
import io.github.loncra.ai.turbo.seedance.video.enumerate.TurboSeedanceVideoMode;
import io.github.loncra.ai.turbo.seedance.video.enumerate.TurboSeedanceVideoModelType;
import io.github.loncra.framework.commons.enumerate.NameEnum;
import org.springframework.ai.model.ModelOptions;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Turbo 渠道 Seedance 视频生成 {@link ModelOptions}。
 * <p>
 * 字段划分完全按照 {@code src/test/resources/readme.html} 第 2 节：
 * <ul>
 * <li>顶层字段 {@link #model} / {@link #duration} / {@link #mode} / {@link #ratio} / {@link #image} / {@link #images} 直接映射到 {@code multipart/form-data} 字段。</li>
 * <li>嵌套的 {@link Metadata} 对应表单字段 {@code metadata} 中的 JSON 字符串。</li>
 * </ul>
 * 运行时由 {@link TurboSeedanceVideoModel} 合并默认值、推断 {@code mode} 并完成校验。
 *
 * @author maurice.chen
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TurboSeedanceVideoOptions implements ModelOptions, Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 模型名称，与 {@link TurboSeedanceVideoModelType} 对应。
	 */
	private String model;

	/**
	 * 视频时长，单位秒；取值范围 {@code 4-15}。
	 */
	private Integer duration;

	/**
	 * 生成模式；不传时平台会根据素材自动推断（见文档第 3 节）。
	 */
	private TurboSeedanceVideoMode mode;

	/**
	 * 视频宽高比，支持 {@code 21:9 / 16:9 / 4:3 / 1:1 / 3:4 / 9:16}。
	 */
	private String ratio;

	/**
	 * 单张参考图片 URL；用于 {@link TurboSeedanceVideoMode#IMAGE_TO_VIDEO}。
	 */
	private String image;

	/**
	 * 多张参考图片 URL。
	 */
	private List<String> images;

	/**
	 * 扩展元数据，对应表单字段 {@code metadata}，被序列化为 JSON 字符串后再提交。
	 */
	private Metadata metadata;

	public TurboSeedanceVideoOptions() {
	}

	public static Builder builder() {
		return new Builder();
	}

	/**
	 * 以已有配置为模板创建一份深拷贝，用于运行时覆盖而不影响默认值。
	 */
	public static TurboSeedanceVideoOptions fromOptions(TurboSeedanceVideoOptions source) {
		if (source == null) {
			return new TurboSeedanceVideoOptions();
		}
		TurboSeedanceVideoOptions target = new TurboSeedanceVideoOptions();
		target.model = source.model;
		target.duration = source.duration;
		target.mode = source.mode;
		target.ratio = source.ratio;
		target.image = source.image;
		if (source.images != null) {
			target.images = new ArrayList<>(source.images);
		}
		if (source.metadata != null) {
			target.metadata = Metadata.fromMetadata(source.metadata);
		}
		return target;
	}

	public String getModel() {
		return this.model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public Integer getDuration() {
		return this.duration;
	}

	public void setDuration(Integer duration) {
		this.duration = duration;
	}

	public TurboSeedanceVideoMode getMode() {
		return this.mode;
	}

	public void setMode(TurboSeedanceVideoMode mode) {
		this.mode = mode;
	}

	public String getRatio() {
		return this.ratio;
	}

	public void setRatio(String ratio) {
		this.ratio = ratio;
	}

	public String getImage() {
		return this.image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public List<String> getImages() {
		return this.images;
	}

	public void setImages(List<String> images) {
		this.images = images;
	}

	public Metadata getMetadata() {
		return this.metadata;
	}

	public void setMetadata(Metadata metadata) {
		this.metadata = metadata;
	}

	/**
	 * 按文档第 3 节规则推断 {@link #mode}（仅当当前 {@code mode} 为 {@code null} 时生效）。
	 * 判定优先级：
	 * <ol>
	 * <li>有视频或音频参考素材 → {@link TurboSeedanceVideoMode#MULTI_REF}</li>
	 * <li>恰好 2 张图片 → {@link TurboSeedanceVideoMode#FIRST_LAST_FRAME}</li>
	 * <li>多于 1 张图片 → {@link TurboSeedanceVideoMode#MULTI_REF}</li>
	 * <li>仅 1 张图片 → {@link TurboSeedanceVideoMode#IMAGE_TO_VIDEO}</li>
	 * <li>无素材 → {@link TurboSeedanceVideoMode#TEXT_TO_VIDEO}</li>
	 * </ol>
	 *
	 * @return 推断后的模式（若 {@link #mode} 已被显式设置则原样返回）
	 */
	public TurboSeedanceVideoMode inferMode() {
		if (this.mode != null) {
			return this.mode;
		}
		int imageCount = this.image != null ? 1 : 0;
		if (this.images != null) {
			imageCount += this.images.size();
		}
		int videoCount = 0;
		int audioCount = 0;
		if (this.metadata != null && this.metadata.content != null) {
			for (TurboSeedanceVideoContent c : this.metadata.content) {
				if (c == null || c.getType() == null) {
					continue;
				}
				switch (c.getType()) {
					case TurboSeedanceVideoContent.TYPE_IMAGE_URL -> imageCount++;
					case TurboSeedanceVideoContent.TYPE_VIDEO_URL -> videoCount++;
					case TurboSeedanceVideoContent.TYPE_AUDIO_URL -> audioCount++;
					default -> {
					}
				}
			}
		}
		if (videoCount > 0 || audioCount > 0) {
			return TurboSeedanceVideoMode.MULTI_REF;
		}
		if (imageCount == 2) {
			return TurboSeedanceVideoMode.FIRST_LAST_FRAME;
		}
		if (imageCount > 1) {
			return TurboSeedanceVideoMode.MULTI_REF;
		}
		if (imageCount == 1) {
			return TurboSeedanceVideoMode.IMAGE_TO_VIDEO;
		}
		return TurboSeedanceVideoMode.TEXT_TO_VIDEO;
	}

	/**
	 * 按文档第 4 节参数约束做请求前校验；不合法时直接抛 {@link IllegalArgumentException}，避免产生计费失败请求。
	 *
	 * @param prompt 当前 prompt；用于 1-500 字符长度校验
	 */
	public void validate(String prompt) {
		if (prompt == null || prompt.isEmpty()) {
			throw new IllegalArgumentException("prompt 不能为空");
		}
		if (prompt.length() > 500) {
			throw new IllegalArgumentException("prompt 长度需在 1-500 字符之间，当前长度为 " + prompt.length());
		}
		if (this.model == null || this.model.isBlank()) {
			throw new IllegalArgumentException("model 不能为空，仅支持 seedance-2 / seedance-2-fast");
		}
		if (NameEnum.ofEnum(TurboSeedanceVideoModelType.class, this.model, true) == null) {
			throw new IllegalArgumentException("model=" + this.model + " 不在支持列表中，仅支持 seedance-2 / seedance-2-fast");
		}

		if (this.duration != null && (this.duration < 4 || this.duration > 15)) {
			throw new IllegalArgumentException("duration 取值范围 4-15 秒，当前为 " + this.duration);
		}
		String resolvedResolution = this.metadata != null ? this.metadata.resolution : null;

		String resolvedRatio = this.metadata != null && this.metadata.ratio != null ? this.metadata.ratio : this.ratio;
		if ("720p".equalsIgnoreCase(resolvedResolution)
				&& ("3:4".equals(resolvedRatio) || "9:16".equals(resolvedRatio))) {
			throw new IllegalArgumentException("720p 分辨率不支持 3:4 / 9:16 宽高比，请改用 480p");
		}
		int[] counts = countContentAssets();
		int imageContentCount = counts[0];
		int videoContentCount = counts[1];
		int audioContentCount = counts[2];

		if (this.mode == TurboSeedanceVideoMode.FIRST_LAST_FRAME) {
			int imageCount = (this.image != null ? 1 : 0) + (this.images != null ? this.images.size() : 0)
					+ imageContentCount;
			if (imageCount != 2) {
				throw new IllegalArgumentException("first_last_frame 模式要求恰好 2 张图片，当前为 " + imageCount);
			}
		}
		if (this.mode == TurboSeedanceVideoMode.IMAGE_TO_VIDEO) {
			int imageCount = (this.image != null ? 1 : 0) + (this.images != null ? this.images.size() : 0)
					+ imageContentCount;
			if (imageCount < 1) {
				throw new IllegalArgumentException("image_to_video 模式至少需要 1 张图片");
			}
		}
		if (this.mode == TurboSeedanceVideoMode.MULTI_REF) {
			if (imageContentCount > 9) {
				throw new IllegalArgumentException("multi_ref 模式下 image 最多 9 个，当前为 " + imageContentCount);
			}
			if (videoContentCount > 3) {
				throw new IllegalArgumentException("multi_ref 模式下 video 最多 3 个，当前为 " + videoContentCount);
			}
			if (audioContentCount > 3) {
				throw new IllegalArgumentException("multi_ref 模式下 audio 最多 3 个，当前为 " + audioContentCount);
			}
		}
	}

	/**
	 * 按类型统计 {@code metadata.content} 中各类素材数量，返回 {@code [image, video, audio]} 三元组。
	 */
	private int[] countContentAssets() {
		int[] counts = new int[3];
		if (this.metadata == null || this.metadata.content == null) {
			return counts;
		}
		for (TurboSeedanceVideoContent c : this.metadata.content) {
			if (c == null || c.getType() == null) {
				continue;
			}
			switch (c.getType()) {
				case TurboSeedanceVideoContent.TYPE_IMAGE_URL -> counts[0]++;
				case TurboSeedanceVideoContent.TYPE_VIDEO_URL -> counts[1]++;
				case TurboSeedanceVideoContent.TYPE_AUDIO_URL -> counts[2]++;
				default -> {
				}
			}
		}
		return counts;
	}

	/**
	 * {@code metadata} 字段数据类；提交时会被序列化为 JSON 字符串放入表单。
	 */
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class Metadata implements Serializable {

		@Serial
		private static final long serialVersionUID = 1L;

		/**
		 * 视频分辨率，支持 {@code 480p} / {@code 720p}。
		 */
		private String resolution;

		/**
		 * 视频宽高比，若非空会覆盖顶层 {@code ratio}。
		 */
		private String ratio;

		/**
		 * 任务完成后的回调地址。
		 */
		@JsonProperty("webhook_url")
		private String webhookUrl;

		/**
		 * 同 {@link #webhookUrl}，文档兼容写法。
		 */
		@JsonProperty("callback_url")
		private String callbackUrl;

		/**
		 * 多媒体内容数组，统一承载多参考图片/视频/音频；见文档 2.3 / 5.4 节。
		 * <p>
		 * 文档中虽然还提供了 {@code image_urls / video_urls / audio_urls} 三个并列数组，
		 * 但与本字段语义完全重叠，且官方推荐优先使用本字段，因此 SDK 只保留 {@code content} 一条路径。
		 */
		private List<TurboSeedanceVideoContent> content;

		/**
		 * 是否生成配音。
		 */
		@JsonProperty("generate_audio")
		private Boolean generateAudio;

		/**
		 * 是否添加水印。
		 */
		private Boolean watermark;

		/**
		 * 额外扩展字段（保留给渠道端的未来参数）。
		 */
		@JsonProperty("extra")
		private Map<String, Object> extra;

		public Metadata() {
		}

		public static Metadata fromMetadata(Metadata source) {
			if (source == null) {
				return new Metadata();
			}
			Metadata target = new Metadata();
			target.resolution = source.resolution;
			target.ratio = source.ratio;
			target.webhookUrl = source.webhookUrl;
			target.callbackUrl = source.callbackUrl;
			if (source.content != null) {
				target.content = new ArrayList<>(source.content);
			}
			target.generateAudio = source.generateAudio;
			target.watermark = source.watermark;
			if (source.extra != null) {
				target.extra = new LinkedHashMap<>(source.extra);
			}
			return target;
		}

		public String getResolution() {
			return this.resolution;
		}

		public void setResolution(String resolution) {
			this.resolution = resolution;
		}

		public String getRatio() {
			return this.ratio;
		}

		public void setRatio(String ratio) {
			this.ratio = ratio;
		}

		public String getWebhookUrl() {
			return this.webhookUrl;
		}

		public void setWebhookUrl(String webhookUrl) {
			this.webhookUrl = webhookUrl;
		}

		public String getCallbackUrl() {
			return this.callbackUrl;
		}

		public void setCallbackUrl(String callbackUrl) {
			this.callbackUrl = callbackUrl;
		}

		public List<TurboSeedanceVideoContent> getContent() {
			return this.content;
		}

		public void setContent(List<TurboSeedanceVideoContent> content) {
			this.content = content;
		}

		public Boolean getGenerateAudio() {
			return this.generateAudio;
		}

		public void setGenerateAudio(Boolean generateAudio) {
			this.generateAudio = generateAudio;
		}

		public Boolean getWatermark() {
			return this.watermark;
		}

		public void setWatermark(Boolean watermark) {
			this.watermark = watermark;
		}

		public Map<String, Object> getExtra() {
			return this.extra;
		}

		public void setExtra(Map<String, Object> extra) {
			this.extra = extra;
		}

	}

	/**
	 * 链式构造器。
	 */
	public static class Builder {

		private final TurboSeedanceVideoOptions target = new TurboSeedanceVideoOptions();

		public Builder model(String model) {
			this.target.model = model;
			return this;
		}

		public Builder model(TurboSeedanceVideoModelType modelType) {
			this.target.model = modelType != null ? modelType.getName() : null;
			return this;
		}

		public Builder duration(Integer duration) {
			this.target.duration = duration;
			return this;
		}

		public Builder mode(TurboSeedanceVideoMode mode) {
			this.target.mode = mode;
			return this;
		}

		public Builder ratio(String ratio) {
			this.target.ratio = ratio;
			return this;
		}

		public Builder image(String image) {
			this.target.image = image;
			return this;
		}

		public Builder images(List<String> images) {
			this.target.images = images != null ? new ArrayList<>(images) : null;
			return this;
		}

		public Builder metadata(Metadata metadata) {
			this.target.metadata = metadata;
			return this;
		}

		public Builder resolution(String resolution) {
			ensureMetadata().resolution = resolution;
			return this;
		}

		public Builder webhookUrl(String webhookUrl) {
			ensureMetadata().webhookUrl = webhookUrl;
			return this;
		}

		public Builder callbackUrl(String callbackUrl) {
			ensureMetadata().callbackUrl = callbackUrl;
			return this;
		}

		public Builder content(List<TurboSeedanceVideoContent> content) {
			ensureMetadata().content = content != null ? new ArrayList<>(content) : null;
			return this;
		}

		public Builder generateAudio(Boolean generateAudio) {
			ensureMetadata().generateAudio = generateAudio;
			return this;
		}

		public Builder watermark(Boolean watermark) {
			ensureMetadata().watermark = watermark;
			return this;
		}

		private Metadata ensureMetadata() {
			if (this.target.metadata == null) {
				this.target.metadata = new Metadata();
			}
			return this.target.metadata;
		}

		public TurboSeedanceVideoOptions build() {
			return this.target;
		}

	}

}
