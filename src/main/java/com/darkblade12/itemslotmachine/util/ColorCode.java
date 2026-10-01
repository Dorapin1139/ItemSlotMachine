package com.darkblade12.itemslotmachine.util;

// 非推奨になった org.bukkit.ChatColor の代わりに使う、§ で始まる色コード
// toString() で ChatColor と同じ文字列(§a など)を返すので、文字列の連結や MessageFormat の引数にそのまま使える
public enum ColorCode {
    BLACK('0'),
    DARK_BLUE('1'),
    DARK_GREEN('2'),
    DARK_AQUA('3'),
    DARK_RED('4'),
    DARK_PURPLE('5'),
    GOLD('6'),
    GRAY('7'),
    DARK_GRAY('8'),
    BLUE('9'),
    GREEN('a'),
    AQUA('b'),
    RED('c'),
    LIGHT_PURPLE('d'),
    YELLOW('e'),
    WHITE('f'),
    RESET('r');

    public static final char COLOR_CHAR = '§';
    private final String text;

    ColorCode(char code) {
        text = new String(new char[] { COLOR_CHAR, code });
    }

    public boolean isColor() {
        return this != RESET;
    }

    @Override
    public String toString() {
        return text;
    }
}
