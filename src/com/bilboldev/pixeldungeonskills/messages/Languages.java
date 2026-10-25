package com.bilboldev.pixeldungeonskills.messages;

import java.util.Locale;

public enum Languages {
	ENGLISH("english", "English", "en"),
	CHI_SMPL("简体中文", "Chinese (Simplified)", "zh"),
	KOREAN("한국어", "Korean", "ko"),
	RUSSIAN("русский", "Russian", "ru"),
	SPANISH("español", "Spanish", "es"),
	PORTUGUESE("português", "Portuguese", "pt"),
	FRENCH("français", "French", "fr"),
	GERMAN("deutsch", "German", "de"),
	CHI_TRAD("繁體中文", "Chinese (Traditional)", "zh-hant"),
	JAPANESE("日本語", "Japanese", "ja"),
	POLISH("polski", "Polish", "pl"),
	ITALIAN("italiano", "Italian", "it"),
	TURKISH("türkçe", "Turkish", "tr"),
	VIETNAMESE("tiếng việt", "Vietnamese", "vi"),
	UKRAINIAN("українська", "Ukrainian", "uk"),
	INDONESIAN("indonesia", "Indonesian", "in"),
	CZECH("čeština", "Czech", "cs"),
	DUTCH("nederlands", "Dutch", "nl"),
	SWEDISH("svenska", "Swedish", "sv"),
	HUNGARIAN("magyar", "Hungarian", "hu"),
	GREEK("ελληνικά", "Greek", "el"),
	BELARUSIAN("беларуская", "Belarusian", "be"),
	ESPERANTO("esperanto", "Esperanto", "eo");

	private final String nativeName;
	private final String displayName;
	private final String code;

	Languages(String nativeName, String displayName, String code) {
		this.nativeName = nativeName;
		this.displayName = displayName;
		this.code = code;
	}

	public String nativeName() {
		return nativeName;
	}

	public String displayName() {
		return displayName;
	}

	public String code() {
		return code;
	}

	public Locale locale() {
		if (this == ENGLISH) {
			return Locale.ENGLISH;
		}
		return Locale.forLanguageTag(code);
	}

	public static Languages matchLocale(Locale locale) {
		if (locale == null) {
			return ENGLISH;
		}

		String tag = locale.toLanguageTag().toLowerCase(Locale.ENGLISH);
		if (tag.startsWith("zh-hant") || tag.contains("-hant") || tag.contains("_hant")) {
			return CHI_TRAD;
		}

		return matchCode(locale.getLanguage());
	}

	public static Languages matchCode(String code) {
		if (code == null || code.length() == 0) {
			return ENGLISH;
		}

		String normalized = code.toLowerCase(Locale.ENGLISH);
		if (normalized.equals("id")) {
			normalized = "in";
		}

		for (Languages language : values()) {
			if (language.code.equals(normalized)) {
				return language;
			}
		}

		return ENGLISH;
	}
}
