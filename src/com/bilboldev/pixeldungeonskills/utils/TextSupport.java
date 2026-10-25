package com.bilboldev.pixeldungeonskills.utils;

import android.graphics.Typeface;

import com.bilboldev.noosa.BitmapText;
import com.bilboldev.noosa.Game;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.messages.Languages;
import com.bilboldev.pixeldungeonskills.messages.Messages;

import java.io.File;
import java.util.regex.Pattern;

public final class TextSupport {

	private static final Pattern HANGUL_PATTERN = Pattern.compile("\\p{InHangul_Syllables}");
	private static final Pattern JAPANESE_PATTERN = Pattern.compile("\\p{InHiragana}|\\p{InKatakana}");
	private static final Pattern CJK_PATTERN = Pattern.compile("\\p{InCJK_Unified_Ideographs}|\\p{InCJK_Symbols_and_Punctuation}|\\p{InHalfwidth_and_Fullwidth_Forms}");

	private static final Pattern SPLITTER = Pattern.compile(
			"(?<=\\n)|(?=\\n)|(?<=_)|(?=_)|(?<=\\*\\*)|(?=\\*\\*)|"
					+ "(?<=\\p{InHiragana})|(?=\\p{InHiragana})|"
					+ "(?<=\\p{InKatakana})|(?=\\p{InKatakana})|"
					+ "(?<=\\p{InCJK_Unified_Ideographs})|(?=\\p{InCJK_Unified_Ideographs})|"
					+ "(?<=\\p{InCJK_Symbols_and_Punctuation})|(?=\\p{InCJK_Symbols_and_Punctuation})|"
					+ "(?<=\\p{InHalfwidth_and_Fullwidth_Forms})|(?=\\p{InHalfwidth_and_Fullwidth_Forms})");

	private static final Pattern SPLITTER_MULTILINE = Pattern.compile(
			"(?<= )|(?= )|(?<=\\n)|(?=\\n)|(?<=_)|(?=_)|(?<=\\*\\*)|(?=\\*\\*)|"
					+ "(?<=\\p{InHiragana})|(?=\\p{InHiragana})|"
					+ "(?<=\\p{InKatakana})|(?=\\p{InKatakana})|"
					+ "(?<=\\p{InCJK_Unified_Ideographs})|(?=\\p{InCJK_Unified_Ideographs})|"
					+ "(?<=\\p{InCJK_Symbols_and_Punctuation})|(?=\\p{InCJK_Symbols_and_Punctuation})|"
					+ "(?<=\\p{InHalfwidth_and_Fullwidth_Forms})|(?=\\p{InHalfwidth_and_Fullwidth_Forms})");

	private static final TypefaceInfo DEFAULT_TYPEFACE = new TypefaceInfo(Typeface.DEFAULT, "default");

	private static String basicFontAsset = Assets.PIXEL_FONT_TTF;

	private static TypefaceInfo basicTypeface;
	private static TypefaceInfo asianFallbackTypeface;
	private static TypefaceInfo koreanTypeface;
	private static TypefaceInfo chineseSimplifiedTypeface;
	private static TypefaceInfo chineseTraditionalTypeface;
	private static TypefaceInfo japaneseTypeface;
	private static boolean initialized;

	private TextSupport() {
	}

	public static synchronized void setBasicFontAsset(String assetPath) {
		String nextAsset = assetPath == null ? Assets.PIXEL_FONT_TTF : assetPath;
		if (!nextAsset.equals(basicFontAsset)) {
			basicFontAsset = nextAsset;
			initialized = false;
		}
		ensureInitialized();
	}

	public static synchronized TypefaceInfo resolveTypefaceInfo(String text) {
		ensureInitialized();

		if (text == null || text.length() == 0) {
			return fallbackTypeface(basicTypeface);
		}

		if (HANGUL_PATTERN.matcher(text).find()) {
			return fallbackTypeface(koreanTypeface);
		}

		if (shouldUseJapaneseTypeface(text)) {
			return fallbackTypeface(japaneseTypeface);
		}

		if (shouldUseChineseTypeface(text)) {
			if (Messages.lang() == Languages.CHI_TRAD) {
				return fallbackTypeface(chineseTraditionalTypeface);
			}
			return fallbackTypeface(chineseSimplifiedTypeface);
		}

		if (containsNonAscii(text)) {
			return fallbackTypeface(asianFallbackTypeface);
		}

		return fallbackTypeface(basicTypeface);
	}

	public static synchronized Typeface getBasicTypeface() {
		ensureInitialized();
		return fallbackTypeface(basicTypeface).typeface;
	}

	public static boolean requiresRenderedGlyphs(BitmapText.Font font, String text) {
		return !canRenderWithBitmapFont(font, text);
	}

	public static boolean canRenderWithBitmapFont(BitmapText.Font font, String text) {
		if (font == null || text == null || text.length() == 0) {
			return true;
		}

		for (int i = 0; i < text.length(); i++) {
			char glyph = text.charAt(i);
			if (glyph == '\n' || glyph == '\r' || glyph == '\t') {
				continue;
			}
			if (font.get(glyph) == null) {
				return false;
			}
		}

		return true;
	}

	public static String[] splitForTextBlock(String text, boolean multiline) {
		if (text == null || text.length() == 0) {
			return new String[0];
		}

		return multiline ? SPLITTER_MULTILINE.split(text) : SPLITTER.split(text);
	}

	public static boolean isNoSpaceWrappingLanguage() {
		Languages language = Messages.lang();
		return language == Languages.CHI_SMPL || language == Languages.CHI_TRAD || language == Languages.JAPANESE;
	}

	private static boolean shouldUseJapaneseTypeface(String text) {
		if (JAPANESE_PATTERN.matcher(text).find()) {
			return true;
		}

		return Messages.lang() == Languages.JAPANESE && CJK_PATTERN.matcher(text).find();
	}

	private static boolean shouldUseChineseTypeface(String text) {
		return CJK_PATTERN.matcher(text).find();
	}

	private static boolean containsNonAscii(String text) {
		for (int i = 0; i < text.length(); i++) {
			if (text.charAt(i) > 0x7F) {
				return true;
			}
		}
		return false;
	}

	private static synchronized void ensureInitialized() {
		if (initialized) {
			return;
		}

		initialized = true;

		basicTypeface = loadAssetTypeface("asset:" + basicFontAsset, basicFontAsset);
		asianFallbackTypeface = loadAssetTypeface("asset:" + Assets.DROID_SANS_TTF, Assets.DROID_SANS_TTF);

		TypefaceInfo cjkJapaneseFromTtc = loadTtcTypeface("/system/fonts/NotoSansCJK-Regular.ttc", 0, "system:noto-cjk-jp");
		TypefaceInfo cjkKoreanFromTtc = loadTtcTypeface("/system/fonts/NotoSansCJK-Regular.ttc", 1, "system:noto-cjk-kr");
		TypefaceInfo cjkChineseSimplifiedFromTtc = loadTtcTypeface("/system/fonts/NotoSansCJK-Regular.ttc", 2, "system:noto-cjk-sc");
		TypefaceInfo cjkChineseTraditionalFromTtc = loadTtcTypeface("/system/fonts/NotoSansCJK-Regular.ttc", 3, "system:noto-cjk-tc");

		koreanTypeface = firstAvailable(
				cjkKoreanFromTtc,
				loadFileTypeface("system:nanum-gothic", "/system/fonts/NanumGothic.ttf"),
				loadFileTypeface("system:noto-kr", "/system/fonts/NotoSansKR-Regular.otf"),
				loadFileTypeface("system:droid-sans-fallback", "/system/fonts/DroidSansFallback.ttf"),
				asianFallbackTypeface,
				basicTypeface);

		chineseSimplifiedTypeface = firstAvailable(
				cjkChineseSimplifiedFromTtc,
				loadFileTypeface("system:noto-sc", "/system/fonts/NotoSansSC-Regular.otf"),
				loadFileTypeface("system:noto-hans", "/system/fonts/NotoSansHans-Regular.otf"),
				loadFileTypeface("system:droid-sans-fallback", "/system/fonts/DroidSansFallback.ttf"),
				asianFallbackTypeface,
				basicTypeface);

		chineseTraditionalTypeface = firstAvailable(
				cjkChineseTraditionalFromTtc,
				loadFileTypeface("system:noto-sc", "/system/fonts/NotoSansSC-Regular.otf"),
				loadFileTypeface("system:noto-hans", "/system/fonts/NotoSansHans-Regular.otf"),
				loadFileTypeface("system:droid-sans-fallback", "/system/fonts/DroidSansFallback.ttf"),
				asianFallbackTypeface,
				basicTypeface);

		japaneseTypeface = firstAvailable(
				cjkJapaneseFromTtc,
				loadFileTypeface("system:noto-jp", "/system/fonts/NotoSansJP-Regular.otf"),
				loadFileTypeface("system:droid-sans-fallback", "/system/fonts/DroidSansFallback.ttf"),
				asianFallbackTypeface,
				basicTypeface);

		basicTypeface = fallbackTypeface(firstAvailable(basicTypeface, asianFallbackTypeface, DEFAULT_TYPEFACE));
		asianFallbackTypeface = fallbackTypeface(firstAvailable(asianFallbackTypeface, DEFAULT_TYPEFACE));
	}

	private static TypefaceInfo fallbackTypeface(TypefaceInfo typeface) {
		if (typeface != null && typeface.typeface != null) {
			return typeface;
		}
		if (asianFallbackTypeface != null && asianFallbackTypeface.typeface != null) {
			return asianFallbackTypeface;
		}
		if (basicTypeface != null && basicTypeface.typeface != null) {
			return basicTypeface;
		}
		return DEFAULT_TYPEFACE;
	}

	private static TypefaceInfo firstAvailable(TypefaceInfo... candidates) {
		for (TypefaceInfo candidate : candidates) {
			if (candidate != null && candidate.typeface != null) {
				return candidate;
			}
		}
		return null;
	}

	private static TypefaceInfo loadAssetTypeface(String cacheKey, String assetPath) {
		try {
			if (Game.instance == null || Game.instance.getAssets() == null) {
				return null;
			}
			return new TypefaceInfo(Typeface.createFromAsset(Game.instance.getAssets(), assetPath), cacheKey);
		} catch (Throwable ignored) {
			return null;
		}
	}

	private static TypefaceInfo loadFileTypeface(String cacheKey, String path) {
		try {
			File fontFile = new File(path);
			if (!fontFile.exists()) {
				return null;
			}
			return new TypefaceInfo(Typeface.createFromFile(path), cacheKey);
		} catch (Throwable ignored) {
			return null;
		}
	}

	private static TypefaceInfo loadTtcTypeface(String path, int ttcIndex, String cacheKey) {
		try {
			File fontFile = new File(path);
			if (!fontFile.exists()) {
				return null;
			}

			if (android.os.Build.VERSION.SDK_INT >= 26) {
				return new TypefaceInfo(new Typeface.Builder(path).setTtcIndex(ttcIndex).build(), cacheKey + ":" + ttcIndex);
			}

			return loadFileTypeface(cacheKey + ":" + ttcIndex, path);
		} catch (Throwable ignored) {
			return null;
		}
	}

	public static final class TypefaceInfo {
		public final Typeface typeface;
		public final String cacheKey;

		public TypefaceInfo(Typeface typeface, String cacheKey) {
			this.typeface = typeface;
			this.cacheKey = cacheKey;
		}
	}
}
