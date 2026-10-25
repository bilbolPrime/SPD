package com.bilboldev.pixeldungeonskills.messages;

import com.bilboldev.noosa.Game;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.IllegalFormatException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Messages {

	private static final String ROOT_PACKAGE = "com.bilboldev.pixeldungeonskills.";
	private static final String EXACT_MATCH_FILE = "messages/exact-match.tsv";
	private static final String LEADING_PLACEHOLDER_PREFIX = "%arg%";
	private static final Pattern FORMAT_PLACEHOLDER_PATTERN = Pattern.compile("%(?:(\\d+)\\$)?[-#+0,(]*\\d*(?:\\.\\d+)?[sdioxXeEgGfcbBhHn%]");
	private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
	private static final Pattern ARG_INDEX_PATTERN = Pattern.compile("%(\\d+)\\$");
	private static final Pattern TRAILING_LEVEL_SUFFIX_PATTERN = Pattern.compile("^(.*?)(\\s\\+\\d+)$");
	private static final String[] BUNDLE_FILES = new String[] {
		"messages/actors/actors",
		"messages/items/items",
		"messages/journal/journal",
		"messages/levels/levels",
		"messages/misc/misc",
		"messages/plants/plants",
		"messages/scenes/scenes",
		"messages/ui/ui",
		"messages/windows/windows",
		"messages/custom",
		"messages/custom_generated",
		"messages/party",
		"messages/reborn",
        "messages/reborn_bosses",
        "messages/reborn_rooms",
        "messages/gauntlet"
	};

	public static final String NO_TEXT_FOUND = "!!!NO TEXT FOUND!!!";

	private static final Properties properties = new Properties();
	private static final Properties english = new Properties();
	private static final HashMap<String, String> CLASS_KEY_ALIASES = new HashMap<String, String>();
	private static final HashMap<String, String> exactTextKeys = new HashMap<String, String>();
	private static final HashMap<Character, ArrayList<ExactPatternEntry>> exactPatternKeys = new HashMap<Character, ArrayList<ExactPatternEntry>>();
	private static final ArrayList<ExactPatternEntry> leadingPatternKeys = new ArrayList<ExactPatternEntry>();
	private static final HashMap<String, String> bundleTextKeys = new HashMap<String, String>();
	private static final HashMap<Character, ArrayList<ExactPatternEntry>> bundlePatternKeys = new HashMap<Character, ArrayList<ExactPatternEntry>>();
	private static final ArrayList<ExactPatternEntry> leadingBundlePatternKeys = new ArrayList<ExactPatternEntry>();
	private static final HashMap<String, String> translationCache = new HashMap<String, String>();
	private static final Object[] NO_ARGS = new Object[0];
	private static Languages language = Languages.ENGLISH;
	private static Locale locale = Locale.ENGLISH;

	static {
		CLASS_KEY_ALIASES.put("actors.mobs.elemental", "actors.mobs.elemental$fireelemental");
		CLASS_KEY_ALIASES.put("actors.mobs.king", "actors.mobs.dwarfking");
		CLASS_KEY_ALIASES.put("actors.mobs.npcs.bee", "actors.mobs.bee");
		CLASS_KEY_ALIASES.put("actors.mobs.yog", "actors.mobs.yogdzewa");
		CLASS_KEY_ALIASES.put("actors.mobs.yog$burningfist", "actors.mobs.yogfist$burningfist");
		CLASS_KEY_ALIASES.put("actors.mobs.yog$larva", "actors.mobs.yogdzewa$larva");
		CLASS_KEY_ALIASES.put("actors.mobs.yog$rottingfist", "actors.mobs.yogfist$rottingfist");
		CLASS_KEY_ALIASES.put("items.bags.potionholder", "items.bags.potionbandolier");
		CLASS_KEY_ALIASES.put("items.bags.wandholster", "items.bags.magicalholster");
		CLASS_KEY_ALIASES.put("items.food.overpricedration", "items.food.smallration");
		CLASS_KEY_ALIASES.put("plants.online.onlinesungrass", "plants.sungrass");
		CLASS_KEY_ALIASES.put("plants.online.onlinesungrass$seed", "plants.sungrass$seed");
	}

	public static Languages lang() {
		return language;
	}

	public static Locale locale() {
		return locale;
	}

	public static void setup(Languages nextLanguage) {
		language = nextLanguage == null ? Languages.ENGLISH : nextLanguage;
		locale = language.locale();
		properties.clear();
		english.clear();
		exactTextKeys.clear();
		exactPatternKeys.clear();
		leadingPatternKeys.clear();
		bundleTextKeys.clear();
		bundlePatternKeys.clear();
		leadingBundlePatternKeys.clear();
		translationCache.clear();

		for (String bundleFile : BUNDLE_FILES) {
			loadBundle(bundleFile + ".properties", true);
			if (language != Languages.ENGLISH) {
				loadBundle(bundleFile + "_" + language.code() + ".properties", false);
				if (language == Languages.INDONESIAN) {
					loadBundle(bundleFile + "_id.properties", false);
				}
			}
		}

		loadExactMatchIndex(EXACT_MATCH_FILE);
	}

	public static String get(String key, Object... args) {
		key = normalizeKey(key);
		String value = getValue(key);
		return args.length > 0 ? format(value, args) : value;
	}

	public static String get(Object object, String key, Object... args) {
		return get(object.getClass(), key, args);
	}

	public static String key(Class<?> type, String field) {
		for (Class<?> current = type; current != null; current = current.getSuperclass()) {
			String key = normalizeKey(classKey(current, field));
			if (english.containsKey(key)) return key;
			String alias = aliasKey(key);
			if (alias != null && english.containsKey(alias)) return alias;
		}
		throw new IllegalArgumentException("Missing message key: " + classKey(type, field));
	}

	public static String get(Class<?> type, String key, Object... args) {
		Class<?> current = type;
		while (current != null) {
			String fullKey = normalizeKey(classKey(current, key));
			String value = properties.getProperty(fullKey);
			if (value == null) {
				String aliasKey = aliasKey(fullKey);
				if (aliasKey != null) {
					value = properties.getProperty(aliasKey);
					if (value != null) fullKey = aliasKey;
				}
			}
			if (value != null) {
				return args.length > 0 ? format(value, args) : value;
			}
			current = current.getSuperclass();
		}

		return NO_TEXT_FOUND;
	}

	public static String format(String format, Object... args) {
		return maybeTranslateInternal(format, args == null ? NO_ARGS : args);
	}

	public static String maybeTranslate(String value) {
		return maybeTranslateInternal(value, NO_ARGS);
	}

	public static String maybeTranslate(String value, Object... args) {
		return maybeTranslateInternal(value, args == null ? NO_ARGS : args);
	}

	public static String capitalize(String value) {
		if (value == null || value.length() == 0) {
			return value;
		}
		return value.substring(0, 1).toUpperCase(locale) + value.substring(1);
	}

	public static String upperCase(String value) {
		return value.toUpperCase(locale);
	}

	public static String lowerCase(String value) {
		return value.toLowerCase(locale);
	}

	private static void loadBundle(String assetPath, boolean indexBundleValues) {
		InputStream inputStream = null;
		try {
			inputStream = openAsset(assetPath);
			if (inputStream == null) {
				return;
			}

			Properties loaded = new Properties();
			loaded.load(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
			for (String key : loaded.stringPropertyNames()) {
				String normalizedKey = normalizeKey(key);
				String propertyValue = decodeEscapedText(loaded.getProperty(key));
				properties.setProperty(normalizedKey, propertyValue);
				if (indexBundleValues) {
					english.setProperty(normalizedKey, propertyValue);
					indexBundleValue(normalizedKey, propertyValue);
				}
			}
		} catch (IOException ignored) {
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException ignored) {
				}
			}
		}
	}

	private static void loadExactMatchIndex(String assetPath) {
		InputStream inputStream = null;
		BufferedReader reader = null;
		try {
			inputStream = openAsset(assetPath);
			if (inputStream == null) {
				return;
			}

			reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.length() == 0 || line.startsWith("#")) {
					continue;
				}

				String[] parts = line.split("\\t", 3);
				if (parts.length < 2) {
					continue;
				}

				String normalized = normalizeText(decodeEscapedText(parts[0]));
				String key = parts[1];
				exactTextKeys.put(normalized, key);

				if (parts.length < 3 || parts[2].length() == 0) {
					continue;
				}

				String template = decodeEscapedText(URLDecoder.decode(parts[2], "UTF-8"));
				if (!FORMAT_PLACEHOLDER_PATTERN.matcher(template).find()) {
					continue;
				}

				if (!hasMeaningfulLiteral(template)) {
					continue;
				}

				ExactPatternEntry entry = new ExactPatternEntry(key, template);
				if (normalized.startsWith(LEADING_PLACEHOLDER_PREFIX)) {
					leadingPatternKeys.add(entry);
				} else {
					char firstChar = normalized.charAt(0);
					ArrayList<ExactPatternEntry> entries = exactPatternKeys.get(firstChar);
					if (entries == null) {
						entries = new ArrayList<ExactPatternEntry>();
						exactPatternKeys.put(firstChar, entries);
					}
					entries.add(entry);
				}
			}
		} catch (IOException ignored) {
		} catch (IllegalArgumentException ignored) {
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException ignored) {
				}
			}
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException ignored) {
				}
			}
		}
	}

	private static void indexBundleValue(String key, String value) {
		if (value == null || value.length() == 0) {
			return;
		}

		String normalized = normalizeText(value);
		if (normalized.length() == 0) {
			return;
		}

		if (!FORMAT_PLACEHOLDER_PATTERN.matcher(value).find()) {
			if (!bundleTextKeys.containsKey(normalized)) {
				bundleTextKeys.put(normalized, key);
			}
			return;
		}

		ExactPatternEntry entry = new ExactPatternEntry(key, value);
		if (!hasMeaningfulLiteral(value)) {
			return;
		}
		if (normalized.startsWith(LEADING_PLACEHOLDER_PREFIX)) {
			leadingBundlePatternKeys.add(entry);
			return;
		}

		char firstChar = normalized.charAt(0);
		ArrayList<ExactPatternEntry> entries = bundlePatternKeys.get(firstChar);
		if (entries == null) {
			entries = new ArrayList<ExactPatternEntry>();
			bundlePatternKeys.put(firstChar, entries);
		}
		entries.add(entry);
	}

	private static InputStream openAsset(String assetPath) throws IOException {
		if (Game.instance != null && Game.instance.getAssets() != null) {
			try {
				return Game.instance.getAssets().open(assetPath);
			} catch (IOException ignored) {
			}
		}

		ClassLoader classLoader = Messages.class.getClassLoader();
		if (classLoader == null) {
			return null;
		}

		return classLoader.getResourceAsStream(assetPath);
	}

	private static String getValue(String key) {
		String value = properties.getProperty(key);
		return value == null ? NO_TEXT_FOUND : value;
	}

	private static String maybeTranslateInternal(String value, Object[] args) {
		if (value == null) {
			return null;
		}

		if (args.length == 0 && translationCache.containsKey(value)) {
			return translationCache.get(value);
		}

		TranslationMatch match = findTranslationMatch(value);
		String translated;
		if (match == null) {
			String suffixAdjusted = args.length == 0 ? maybeTranslateTrailingLevelSuffix(value) : null;
			translated = suffixAdjusted != null ? suffixAdjusted : (args.length > 0 ? formatRaw(value, args) : value);
		} else {
			String localized = properties.getProperty(normalizeKey(match.key));
			if (localized == null) {
				translated = args.length > 0 ? formatRaw(value, args) : value;
			} else if (args.length > 0) {
				translated = formatRaw(localized, args);
			} else if (match.capturedArgs != null) {
				translated = substituteRawFormat(localized, match.capturedArgs);
			} else {
				translated = localized;
			}
		}

		if (args.length == 0) {
			translationCache.put(value, translated);
		}
		return translated;
	}

	public static int argumentCount(String key) {
		String template = english.getProperty(key);
		if (template == null) return -1;
		Matcher matcher = FORMAT_PLACEHOLDER_PATTERN.matcher(template);
		int count = 0, next = 0;
		while (matcher.find()) {
			String token = matcher.group();
			if (token.equals("%%") || token.equals("%n")) continue;
			int index = matcher.group(1) == null ? next++ : Integer.parseInt(matcher.group(1)) - 1;
			count = Math.max(count, index + 1);
		}
		return count;
	}

	public static String template(String key, boolean inEnglish) {
		return (inEnglish ? english : properties).getProperty(key);
	}

	private static String maybeTranslateTrailingLevelSuffix(String value) {
		Matcher matcher = TRAILING_LEVEL_SUFFIX_PATTERN.matcher(value);
		if (!matcher.matches()) {
			return null;
		}

		String baseValue = matcher.group(1);
		String suffix = matcher.group(2);
		if (baseValue == null || baseValue.length() == 0 || suffix == null || suffix.length() == 0) {
			return null;
		}

		return maybeTranslateInternal(baseValue, NO_ARGS) + suffix;
	}

	private static TranslationMatch findTranslationMatch(String value) {
		String normalized = normalizeText(value);
		if (normalized.length() == 0) {
			return null;
		}

		String directKey = exactTextKeys.get(normalized);
		if (directKey != null) {
			return new TranslationMatch(directKey, null);
		}

		String bundleDirectKey = bundleTextKeys.get(normalized);
		if (bundleDirectKey != null) {
			return new TranslationMatch(bundleDirectKey, null);
		}

		if (FORMAT_PLACEHOLDER_PATTERN.matcher(value).find()) {
			return null;
		}

		PatternMatchResult bestMatch = null;
		bestMatch = findPatternMatch(bestMatch, exactPatternKeys.get(normalized.charAt(0)), value);
		bestMatch = findPatternMatch(bestMatch, bundlePatternKeys.get(normalized.charAt(0)), value);
		bestMatch = findPatternMatch(bestMatch, leadingPatternKeys, value);
		bestMatch = findPatternMatch(bestMatch, leadingBundlePatternKeys, value);
		return bestMatch == null ? null : bestMatch.match;
	}

	private static PatternMatchResult findPatternMatch(PatternMatchResult currentBest, ArrayList<ExactPatternEntry> entries, String value) {
		if (entries == null) {
			return currentBest;
		}

		PatternMatchResult bestMatch = currentBest;
		for (ExactPatternEntry entry : entries) {
			TranslationMatch match = entry.match(value);
			if (match != null) {
				if (bestMatch == null || entry.isMoreSpecificThan(bestMatch.entry)) {
					bestMatch = new PatternMatchResult(entry, match);
				}
			}
		}

		return bestMatch;
	}

	private static class PatternMatchResult {
		private final ExactPatternEntry entry;
		private final TranslationMatch match;

		private PatternMatchResult(ExactPatternEntry entry, TranslationMatch match) {
			this.entry = entry;
			this.match = match;
		}
	}

	private static String substituteRawFormat(String format, String[] args) {
		StringBuilder builder = new StringBuilder();
		Matcher matcher = FORMAT_PLACEHOLDER_PATTERN.matcher(format);
		int last = 0;
		int nextArg = 0;

		while (matcher.find()) {
			builder.append(format, last, matcher.start());
			String token = matcher.group();

			if ("%%".equals(token)) {
				builder.append('%');
			} else if ("%n".equals(token)) {
				builder.append('\n');
			} else {
				int argIndex = argumentIndex(token, nextArg);
				if (argIndex >= 0 && argIndex < args.length) {
					builder.append(args[argIndex]);
				} else {
					builder.append(token);
				}

				if (token.indexOf('$') < 0) {
					nextArg++;
				}
			}

			last = matcher.end();
		}

		builder.append(format.substring(last));
		return builder.toString();
	}

	private static int argumentIndex(String token, int defaultIndex) {
		Matcher matcher = ARG_INDEX_PATTERN.matcher(token);
		if (matcher.find()) {
			return Integer.parseInt(matcher.group(1)) - 1;
		}
		return defaultIndex;
	}

	private static String formatRaw(String format, Object[] args) {
		try {
			return String.format(locale, format, args);
		} catch (IllegalFormatException e) {
			return format;
		}
	}

	private static String decodeEscapedText(String value) {
		if (value == null || value.indexOf('\\') < 0) {
			return value;
		}

		StringBuilder builder = new StringBuilder(value.length());
		boolean escaping = false;
		for (int i = 0; i < value.length(); i++) {
			char current = value.charAt(i);
			if (!escaping) {
				if (current == '\\') {
					escaping = true;
				} else {
					builder.append(current);
				}
				continue;
			}

			switch (current) {
			case 'n':
				builder.append('\n');
				break;
			case 'r':
				builder.append('\r');
				break;
			case 't':
				builder.append('\t');
				break;
			case '\\':
				builder.append('\\');
				break;
			default:
				builder.append('\\').append(current);
				break;
			}
			escaping = false;
		}

		if (escaping) {
			builder.append('\\');
		}

		return builder.toString();
	}

	private static String classKey(Class<?> type, String key) {
		String className = type.getName();
		if (className.startsWith(ROOT_PACKAGE)) {
			className = className.substring(ROOT_PACKAGE.length());
		}
		return className + "." + key;
	}

	private static String aliasKey(String fullKey) {
		for (String sourcePrefix : CLASS_KEY_ALIASES.keySet()) {
			if (fullKey.startsWith(sourcePrefix + ".")) {
				return CLASS_KEY_ALIASES.get(sourcePrefix) + fullKey.substring(sourcePrefix.length());
			}
		}
		return null;
	}

	private static String normalizeKey(String key) {
		return key.toLowerCase(Locale.ENGLISH);
	}

	private static String normalizeText(String value) {
		String normalized = value.trim();
		normalized = normalized.replace('\u2019', '\'').replace('\u201C', '"').replace('\u201D', '"');
		normalized = FORMAT_PLACEHOLDER_PATTERN.matcher(normalized).replaceAll("%ARG%");
		normalized = WHITESPACE_PATTERN.matcher(normalized).replaceAll(" ");
		return normalized.toLowerCase(Locale.ENGLISH);
	}

	private static boolean hasMeaningfulLiteral(String template) {
		String literal = FORMAT_PLACEHOLDER_PATTERN.matcher(template).replaceAll("");
		literal = WHITESPACE_PATTERN.matcher(literal).replaceAll("");
		return literal.length() > 0;
	}

	private static class TranslationMatch {
		private final String key;
		private final String[] capturedArgs;
		private final int literalLength;

		private TranslationMatch(String key, String[] capturedArgs) {
			this(key, capturedArgs, Integer.MAX_VALUE);
		}

		private TranslationMatch(String key, String[] capturedArgs, int literalLength) {
			this.key = key;
			this.capturedArgs = capturedArgs;
			this.literalLength = literalLength;
		}
	}

	private static class ExactPatternEntry {
		private final String key;
		private final Pattern pattern;
		private final int literalLength;
		private final int templateLength;
		private final int[] argumentIndexes;
		private final int argumentCount;

		private ExactPatternEntry(String key, String template) {
			this.key = key;
			this.pattern = Pattern.compile(templatePattern(template), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.DOTALL);
			this.literalLength = literalLength(template);
			this.templateLength = template.length();
			ArrayList<Integer> indexes = new ArrayList<Integer>();
			Matcher placeholders = FORMAT_PLACEHOLDER_PATTERN.matcher(template);
			int nextArg = 0, count = 0;
			while (placeholders.find()) {
				String token = placeholders.group();
				if ("%%".equals(token) || "%n".equals(token)) continue;
				int index = argumentIndex(token, nextArg);
				indexes.add(index);
				count = Math.max(count, index + 1);
				if (token.indexOf('$') < 0) nextArg++;
			}
			argumentIndexes = new int[indexes.size()];
			for (int i = 0; i < argumentIndexes.length; i++) argumentIndexes[i] = indexes.get(i);
			argumentCount = count;
		}

		private TranslationMatch match(String value) {
			Matcher matcher = pattern.matcher(value);
			if (!matcher.matches()) {
				return null;
			}

			String[] args = new String[argumentCount];
			for (int i = 0; i < argumentIndexes.length; i++) {
				int index = argumentIndexes[i];
				String captured = matcher.group(i + 1);
				if (args[index] != null && !args[index].equals(captured)) return null;
				args[index] = captured;
			}
			return new TranslationMatch(key, args, literalLength);
		}

		private boolean isMoreSpecificThan(ExactPatternEntry other) {
			if (other == null) {
				return true;
			}

			if (literalLength != other.literalLength) {
				return literalLength > other.literalLength;
			}

			return templateLength > other.templateLength;
		}

		private static String templatePattern(String template) {
			StringBuilder regex = new StringBuilder("^");
			Matcher matcher = FORMAT_PLACEHOLDER_PATTERN.matcher(template);
			int last = 0;

			while (matcher.find()) {
				regex.append(Pattern.quote(template.substring(last, matcher.start())));
				String token = matcher.group();

				if ("%%".equals(token)) {
					regex.append("%");
				} else if ("%n".equals(token)) {
					regex.append("\\R");
				} else {
					char conversion = token.charAt(token.length() - 1);
					if (conversion == 'd') regex.append("([+-]?\\d+)");
					else if ("eEfgG".indexOf(conversion) >= 0) regex.append("([+-]?\\d+(?:[.,]\\d+)?)");
					else regex.append("(.+?)");
				}

				last = matcher.end();
			}

			regex.append(Pattern.quote(template.substring(last)));
			regex.append('$');
			return regex.toString();
		}

		private static int literalLength(String template) {
			int length = 0;
			Matcher matcher = FORMAT_PLACEHOLDER_PATTERN.matcher(template);
			int last = 0;

			while (matcher.find()) {
				length += template.substring(last, matcher.start()).length();
				last = matcher.end();
			}

			length += template.substring(last).length();
			return length;
		}
	}
}
