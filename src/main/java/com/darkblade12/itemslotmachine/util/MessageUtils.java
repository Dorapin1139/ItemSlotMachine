package com.darkblade12.itemslotmachine.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class MessageUtils {
    private static final Map<ColorCode, ColorCode> SIMILAR_COLORS = new HashMap<>();
    private static final ColorCode[] COLORS;
    private static final Random RANDOM = new Random();
    // アイテムの名前・説明文・本のページ用。Bukkit の String 版 API と同じ形式(§ と、§x§r§r§g§g§b§b の hex 色)
    private static final LegacyComponentSerializer ITEM_SERIALIZER = LegacyComponentSerializer.builder()
                                                                                              .character(ColorCode.COLOR_CHAR)
                                                                                              .hexColors()
                                                                                              .useUnusualXRepeatedCharacterHexFormat()
                                                                                              .build();
    // 看板用。Paper の Sign.getLine/setLine の内部と同じもの
    private static final LegacyComponentSerializer SIGN_SERIALIZER = LegacyComponentSerializer.legacySection();
    private static final String COLOR_CODE_CHARS = "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx";
    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)" + ColorCode.COLOR_CHAR + "[0-9A-FK-ORX]");

    static {
        SIMILAR_COLORS.put(ColorCode.DARK_BLUE, ColorCode.BLUE);
        SIMILAR_COLORS.put(ColorCode.DARK_GREEN, ColorCode.GREEN);
        SIMILAR_COLORS.put(ColorCode.DARK_AQUA, ColorCode.AQUA);
        SIMILAR_COLORS.put(ColorCode.DARK_RED, ColorCode.RED);
        SIMILAR_COLORS.put(ColorCode.DARK_PURPLE, ColorCode.LIGHT_PURPLE);
        SIMILAR_COLORS.put(ColorCode.DARK_GRAY, ColorCode.GRAY);
        SIMILAR_COLORS.put(ColorCode.GOLD, ColorCode.YELLOW);

        COLORS = Arrays.stream(ColorCode.values()).filter(c -> c.isColor() && c != ColorCode.BLACK && c != ColorCode.WHITE)
                       .toArray(ColorCode[]::new);
    }

    private MessageUtils() {
    }

    public static ColorCode randomColorCode() {
        return COLORS[RANDOM.nextInt(COLORS.length)];
    }

    public static ColorCode similarColor(ColorCode color) {
        for (Entry<ColorCode, ColorCode> entry : SIMILAR_COLORS.entrySet()) {
            ColorCode key = entry.getKey();
            ColorCode value = entry.getValue();
            if (key == color) {
                return value;
            } else if (value == color) {
                return key;
            }
        }

        return color;
    }

    public static String[] formatSignLines(String[] lines, int... splitLines) {
        if (lines.length > 4) {
            throw new IllegalArgumentException("Lines cannot have more than 4 elements.");
        }

        for (int index : splitLines) {
            if (index >= 0 && index < lines.length - 1) {
                String line = lines[index];
                if (line.length() > 15) {
                    String[] split = line.split(" ");
                    lines[index] = split[0];
                    lines[index + 1] = split[1];
                }
            }
        }

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            lines[i] = line.length() > 15 ? line.substring(0, 15) : line;
        }

        return lines;
    }

    public static String formatName(Enum<?> enumObj, boolean capitalize) {
        String[] split = enumObj.name().toLowerCase().split("_");
        return Arrays.stream(split).map(s -> capitalize ? capitalize(s) : s).collect(Collectors.joining(" "));
    }

    public static String formatName(Enum<?> enumObj) {
        return formatName(enumObj, false);
    }

    public static String toString(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        String name = meta != null && meta.hasDisplayName() ? fromItemComponent(meta.displayName()) : formatName(item.getType(), true);

        return "\u00A72" + name + " \u00A78\u00D7 \u00A77" + item.getAmount();
    }

    public static String toString(Collection<ItemStack> items) {
        return items.stream().map(MessageUtils::toString).collect(Collectors.joining(ColorCode.GREEN + ", "));
    }

    // § の色コード付き文字列を、アイテムの名前・説明文・本のページ用の Component にする
    // 非推奨の String 版 API(setDisplayName など)は斜体を明示的に外しているので、見た目をそろえるため同じようにする
    public static Component toItemComponent(String text) {
        return ITEM_SERIALIZER.deserialize(text).decoration(TextDecoration.ITALIC, false);
    }

    public static List<Component> toItemComponents(List<String> texts) {
        return texts.stream().map(MessageUtils::toItemComponent).collect(Collectors.toList());
    }

    // アイテムの名前・説明文・本のページを § の色コード付き文字列に戻す。null は空文字にする
    public static String fromItemComponent(Component component) {
        return component == null ? "" : ITEM_SERIALIZER.serialize(component);
    }

    public static List<String> fromItemComponents(List<Component> components) {
        return components.stream().map(MessageUtils::fromItemComponent).collect(Collectors.toList());
    }

    // 看板の行と文字列の変換(Paper の String 版 API と同じ変換)。null は空文字にする
    public static Component toSignComponent(String text) {
        return SIGN_SERIALIZER.deserialize(text);
    }

    public static String fromSignComponent(Component component) {
        return component == null ? "" : SIGN_SERIALIZER.serialize(component);
    }

    // altColorChar の色コード(&a など)を § に置き換える(ChatColor.translateAlternateColorCodes と同じ動き)
    public static String translateAlternateColorCodes(char altColorChar, String text) {
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length - 1; i++) {
            if (chars[i] == altColorChar && COLOR_CODE_CHARS.indexOf(chars[i + 1]) > -1) {
                chars[i] = ColorCode.COLOR_CHAR;
                chars[i + 1] = Character.toLowerCase(chars[i + 1]);
            }
        }
        return new String(chars);
    }

    // § の色コードを取り除く(ChatColor.stripColor と同じ動き)
    public static String stripColor(String text) {
        return text == null ? null : STRIP_COLOR_PATTERN.matcher(text).replaceAll("");
    }

    // 先頭の 1 文字だけをタイトルケースにする(commons-lang の StringUtils.capitalize と同じ動き)
    public static String capitalize(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        return Character.toTitleCase(text.charAt(0)) + text.substring(1);
    }

    // Java のエスケープ(\n、\t、\\、\" など)と \\uXXXX を元の文字に戻す
    // (commons-lang 2 の StringEscapeUtils.unescapeJava と同じ動き。未知のエスケープはバックスラッシュだけを取り除く)
    public static String unescapeJava(String text) {
        if (text == null) {
            return null;
        }

        StringBuilder result = new StringBuilder(text.length());
        StringBuilder unicode = new StringBuilder(4);
        boolean hadSlash = false;
        boolean inUnicode = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (inUnicode) {
                unicode.append(c);
                if (unicode.length() == 4) {
                    try {
                        result.append((char) Integer.parseInt(unicode.toString(), 16));
                    } catch (NumberFormatException ex) {
                        throw new IllegalArgumentException("Unable to parse unicode value: " + unicode, ex);
                    }
                    unicode.setLength(0);
                    inUnicode = false;
                }
                continue;
            }

            if (hadSlash) {
                hadSlash = false;
                switch (c) {
                    case 'r':
                        result.append('\r');
                        break;
                    case 'f':
                        result.append('\f');
                        break;
                    case 't':
                        result.append('\t');
                        break;
                    case 'n':
                        result.append('\n');
                        break;
                    case 'b':
                        result.append('\b');
                        break;
                    case 'u':
                        inUnicode = true;
                        break;
                    default:
                        result.append(c);
                        break;
                }
                continue;
            }

            if (c == '\\') {
                hadSlash = true;
            } else {
                result.append(c);
            }
        }

        if (hadSlash) {
            result.append('\\');
        }

        return result.toString();
    }
}
