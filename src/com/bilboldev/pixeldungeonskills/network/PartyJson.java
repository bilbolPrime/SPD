package com.bilboldev.pixeldungeonskills.network;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;

final class PartyJson {
    private final Reader input;
    private final int limit;
    private final Budget budget;
    private int position;
    private int next = -2;

    private PartyJson(Reader input, int limit, Budget budget) {
        this.input = input;
        this.limit = limit;
        this.budget = budget;
    }

    static JSONObject parse(String text, int limit) throws IOException, JSONException {
        if (text == null || text.length() > limit) throw new IOException("Message too large");
        return new PartyJson(new StringReader(text), limit, new Budget(limit, Integer.MAX_VALUE)).read();
    }

    static JSONObject snapshot(Reader input) throws IOException, JSONException {
        return new PartyJson(input, PartySnapshot.MAX_BYTES, new Budget()).read();
    }

    private JSONObject read() throws IOException, JSONException {
        Object value = value(0);
        space();
        if (!(value instanceof JSONObject) || peek() != -1) throw invalid();
        return (JSONObject)value;
    }

    private Object value(int depth) throws IOException, JSONException {
        budget.value(depth);
        space();
        int ch = peek();
        if (ch == -1) throw invalid();
        if (ch == '"') return string(budget.stringLimit);
        if (ch == '{') {
            consume();
            budget.spend(128);
            JSONObject object = new JSONObject();
            if (take('}')) return object;
            do {
                space();
                String key = string(160);
                if (object.has(key) || object.length() >= 4096 || !take(':')) throw invalid();
                budget.spend(64);
                object.put(key, value(depth + 1));
                if (take('}')) return object;
            } while (take(','));
            throw invalid();
        }
        if (ch == '[') {
            consume();
            budget.spend(128);
            JSONArray array = new JSONArray();
            if (take(']')) return array;
            do {
                if (array.length() >= 16384) throw invalid();
                array.put(value(depth + 1));
                if (take(']')) return array;
            } while (take(','));
            throw invalid();
        }
        if (ch == 't') { literal("true"); return Boolean.TRUE; }
        if (ch == 'f') { literal("false"); return Boolean.FALSE; }
        if (ch == 'n') { literal("null"); return JSONObject.NULL; }
        return number();
    }

    private String string(int maximum) throws IOException {
        if (consume() != '"') throw invalid();
        budget.spend(64);
        StringBuilder result = new StringBuilder();
        boolean highSurrogate = false;
        while (peek() != -1) {
            char ch = (char)consume();
            if (ch == '"') {
                if (highSurrogate) throw invalid();
                return result.toString();
            }
            if (ch < 32) throw invalid();
            if (ch == '\\') {
                if (peek() == -1) throw invalid();
                ch = (char)consume();
                switch (ch) {
                    case '"': case '\\': case '/': break;
                    case 'b': ch = '\b'; break;
                    case 'f': ch = '\f'; break;
                    case 'n': ch = '\n'; break;
                    case 'r': ch = '\r'; break;
                    case 't': ch = '\t'; break;
                    case 'u':
                        int code = 0;
                        for (int i = 0; i < 4; i++) {
                            int digit = consume();
                            int hex = digit >= '0' && digit <= '9' ? digit - '0'
                                    : digit >= 'a' && digit <= 'f' ? digit - 'a' + 10
                                    : digit >= 'A' && digit <= 'F' ? digit - 'A' + 10 : -1;
                            if (hex < 0) throw invalid();
                            code = code * 16 + hex;
                        }
                        ch = (char)code;
                        break;
                    default: throw invalid();
                }
            }
            if (highSurrogate != Character.isLowSurrogate(ch)) throw invalid();
            highSurrogate = Character.isHighSurrogate(ch);
            if (result.length() >= maximum) throw invalid();
            budget.spend(2);
            result.append(ch);
        }
        throw invalid();
    }

    private Number number() throws IOException {
        StringBuilder number = new StringBuilder();
        if (peek() == '-') number.append((char)consume());
        if (peek() == '0') number.append((char)consume());
        else {
            if (peek() < '1' || peek() > '9') throw invalid();
            digits(number);
        }
        boolean decimal = false;
        if (peek() == '.') {
            decimal = true;
            number.append((char)consume());
            digits(number);
        }
        if (peek() == 'e' || peek() == 'E') {
            decimal = true;
            number.append((char)consume());
            if (peek() == '+' || peek() == '-') number.append((char)consume());
            digits(number);
        }
        if (number.length() > 32) throw invalid();
        try {
            if (!decimal) return Long.parseLong(number.toString());
            double value = Double.parseDouble(number.toString());
            if (Double.isNaN(value) || Double.isInfinite(value)) throw invalid();
            return value;
        } catch (NumberFormatException invalid) {
            throw invalid();
        }
    }

    private void digits(StringBuilder number) throws IOException {
        int start = number.length();
        while (peek() >= '0' && peek() <= '9') {
            if (number.length() >= 32) throw invalid();
            number.append((char)consume());
        }
        if (start == number.length()) throw invalid();
    }

    private void literal(String word) throws IOException {
        for (int i = 0; i < word.length(); i++) {
            if (consume() != word.charAt(i)) throw invalid();
        }
    }

    private boolean take(char ch) throws IOException {
        space();
        if (peek() != ch) return false;
        consume();
        return true;
    }

    private void space() throws IOException {
        while (peek() == ' ' || peek() == '\n' || peek() == '\r' || peek() == '\t') consume();
    }

    private int peek() throws IOException {
        if (next == -2) {
            next = input.read();
            if (next != -1 && ++position > limit) throw new IOException("Message too large");
        }
        return next;
    }

    private int consume() throws IOException {
        int ch = peek();
        next = -2;
        return ch;
    }

    static JSONObject copy(JSONObject source) throws IOException, JSONException {
        return (JSONObject)copy(source, new Budget(), 0);
    }

    private static Object copy(Object source, Budget budget, int depth) throws IOException, JSONException {
        budget.value(depth);
        if (source instanceof JSONObject) {
            JSONObject object = (JSONObject)source;
            if (object.length() > 4096) throw invalid();
            budget.spend(128);
            JSONObject result = new JSONObject();
            Iterator<String> keys = object.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                if (key.length() > 160) throw invalid();
                budget.spend(128 + 2 * key.length());
                result.put(key, copy(object.get(key), budget, depth + 1));
            }
            return result;
        }
        if (source instanceof JSONArray) {
            JSONArray array = (JSONArray)source;
            if (array.length() > 16384) throw invalid();
            budget.spend(128);
            JSONArray result = new JSONArray();
            for (int i = 0; i < array.length(); i++) result.put(copy(array.get(i), budget, depth + 1));
            return result;
        }
        if (source instanceof String) {
            String text = (String)source;
            if (text.length() > budget.stringLimit) throw invalid();
            budget.spend(64 + 2 * text.length());
        }
        return source;
    }

    private static final class Budget {
        final int stringLimit;
        int remaining, values;

        Budget() {

            this(4096, 8 * 1024 * 1024);
        }

        Budget(int stringLimit, int remaining) {
            this.stringLimit = stringLimit;
            this.remaining = remaining;
        }

        void value(int depth) throws IOException {
            if (depth > 64 || ++values > 300000) throw invalid();
            spend(32);
        }

        void spend(int bytes) throws IOException {
            remaining -= bytes;
            if (remaining < 0) throw new IOException("Party JSON uses too much memory");
        }
    }

    private static IOException invalid() {
        return new IOException("Invalid party JSON");
    }
}
