package com.darkblade12.itemslotmachine.slotmachine;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Locale;
import java.util.regex.Pattern;

public final class SoundInfo {
    private static final Pattern FORMAT = Pattern.compile("(?i)[a-z_]+(-\\d+(\\.\\d+)?){2}(-(true|false))?");
    private final Sound sound;
    private final float volume;
    private final float pitch;
    private final boolean broadcast;

    public SoundInfo(Sound sound, float volume, float pitch, boolean broadcast) {
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
        this.broadcast = broadcast;
    }

    public static SoundInfo fromString(String text) throws IllegalArgumentException {
        if (!FORMAT.matcher(text).matches()) {
            throw new IllegalArgumentException("Invalid sound data format.");
        }

        String[] data = text.split("-");
        Sound sound;
        try {
            sound = getSound(data[0]);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid sound name.");
        }

        float volume;
        try {
            volume = Float.parseFloat(data[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid volume number.");
        }

        float pitch;
        try {
            pitch = Float.parseFloat(data[2]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid pitch number.");
        }

        boolean broadcast = data.length <= 3 || Boolean.parseBoolean(data[3]);
        return new SoundInfo(sound, volume, pitch, broadcast);
    }

    // 設定ファイルの効果音名(BLOCK_NOTE_BLOCK_PLING など)は Sound の定数名なので、同じ名前の定数を取り出す
    // (Sound.valueOf は削除予定。レジストリのキー block.note_block.pling とは形が違うため、キーでは引けない)
    private static Sound getSound(String name) throws ReflectiveOperationException {
        Field field = Sound.class.getField(name.toUpperCase(Locale.ROOT));
        if (!Modifier.isStatic(field.getModifiers()) || field.getType() != Sound.class) {
            throw new NoSuchFieldException(name);
        }
        return (Sound) field.get(null);
    }

    public void play(Location location) {
        World world = location.getWorld();
        if (world == null) {
            throw new IllegalArgumentException("World of location cannot be null.");
        }

        world.playSound(location, sound, volume, pitch);
    }

    public void play(Player player, Location location) {
        player.playSound(location, sound, volume, pitch);
    }

    public void play(Location location, float pitchScale) {
        World world = location.getWorld();
        if (world == null) {
            throw new IllegalArgumentException("World of location cannot be null.");
        }

        world.playSound(location, sound, volume, scaledPitch(pitchScale));
    }

    public void play(Player player, Location location, float pitchScale) {
        player.playSound(location, sound, volume, scaledPitch(pitchScale));
    }

    private float scaledPitch(float pitchScale) {
        float scaled = pitch * pitchScale;
        if (scaled < 0.5f) {
            return 0.5f;
        }
        if (scaled > 2.0f) {
            return 2.0f;
        }
        return scaled;
    }

    public boolean isBroadcast() {
        return broadcast;
    }
}
