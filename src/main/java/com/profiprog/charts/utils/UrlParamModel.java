package com.profiprog.charts.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Map;
import java.util.TreeMap;

public class UrlParamModel extends TreeMap<String, String> {

    public UrlParamModel() {
    }

    public UrlParamModel(String initValues) {
        parse(initValues);
    }

    public void parse(String initValues) {
        for (String entry : initValues.split("&")) {
            int index = entry.indexOf('=');
            if (index == -1) put(entry, null);
            else {
                put(entry.substring(0, index), decodeValue(entry.substring(index + 1)));
            }
        }
        onModelChange();
    }

    public String toTokenString() {
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, String> entry : entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (excludeParameter(key, value)) continue;
            if (result.length() != 0) result.append('&');
            result.append(key);
            if (value != null) result.append('=').append(encodeValue(value));
        }
        return result.toString();
    }

    protected boolean excludeParameter(String name, String value) {
        return false;
    }

    protected String decodeValue(String value) {
		try {
			return URLDecoder.decode(value, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

    protected String encodeValue(String value) {
		try {
			return URLEncoder.encode(value, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

    @Override
    public void clear() {
        super.clear();
        onModelChange();
    }

    @Override
    public String put(String key, String value) {
        String result = super.put(key, value);
        onModelChange();
        return result;
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> map) {
        super.putAll(map);
        onModelChange();
    }

    @Override
    public String remove(Object key) {
        String result = super.remove(key);
        onModelChange();
        return result;
    }

    public void synchronizeTo(Map<String,String> map) {
        super.clear();
        super.putAll(map);
        onModelChange();
    }

    protected void onModelChange() {
    }
}
