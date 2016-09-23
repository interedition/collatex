package eu.interedition.collatex.xmltokenizer;

import java.util.Map;

public class XMLUtil {
    public static String getMilestoneTag(String name, Map<String, String> attributes) {
        return openingTagBuilder(name, attributes).append("/>").toString();
    }

    public static String getOpenTag(String name, Map<String, String> attributes) {
        return openingTagBuilder(name, attributes).append(">").toString();
    }

    public static String getCloseTag(String name) {
        return "</" + name + ">";
    }

    private static StringBuilder openingTagBuilder(String name, Map<String, String> attributes) {
        StringBuilder builder = new StringBuilder("<").append(name);
        appendAttributes(builder, attributes);
        return builder;
    }

    public static void appendAttributes(StringBuilder builder, Map<String, String> attributes) {
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            builder.append(' ').append(entry.getKey()).append('=');
            builder.append('"');
            appendAttributeValue(builder, entry.getValue());
            builder.append('"');
        }
    }

    private static void appendAttributeValue(StringBuilder builder, String value) {
        int n = value.length();
        for (int i = 0; i < n; i++) {
            char c = value.charAt(i);
            switch (c) {
            case '<':
                builder.append("&lt;");
                break;
            case '>':
                builder.append("&gt;");
                break;
            case '&':
                builder.append("&amp;");
                break;
            default:
                builder.append(c);
                break;
            }
        }
    }

}
