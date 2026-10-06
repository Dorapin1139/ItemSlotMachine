package com.darkblade12.itemslotmachine.plugin;

import com.darkblade12.itemslotmachine.util.FileUtils;
import com.darkblade12.itemslotmachine.util.MessageUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

public final class MessageManager extends Manager<PluginBase> {
    private static final String FILE_PATTERN = "messages_{0}.json";
    private final Locale[] locales;
    private final Map<Message, MessageFormat> messageCache;
    private MessageFormat missing;

    public MessageManager(PluginBase plugin, Locale... locales) {
        super(plugin);
        this.locales = locales;
        messageCache = new HashMap<>();
    }

    @Override
    public void onEnable() {
        String tag = plugin.getCurrentLocale().toLanguageTag();
        File file = new File(plugin.getDataFolder(), MessageFormat.format(FILE_PATTERN, tag));
        String fileName = file.getName();
        JsonObject messageData = null;
        if (file.exists()) {
            try {
                messageData = FileUtils.readJson(file, JsonElement.class).getAsJsonObject();
            } catch (IOException | JsonParseException e) {
                plugin.logException(e, "Failed to read message file %s!", fileName);
            }
        } else {
            plugin.logWarning("Could not find message file %s! Copying default files...", fileName);
            saveDefaultFiles();
        }

        if (messageData == null) {
            try {
                tag = "en-US";
                String defaultName = MessageFormat.format(FILE_PATTERN, tag);
                messageData = FileUtils.readJson(plugin, defaultName, JsonElement.class).getAsJsonObject();
                plugin.logInfo("Default message files successfully copied.");
            } catch (IOException | JsonParseException e) {
                plugin.logException(e, "Failed to read the default language file!");
                return;
            }
        }

        loadMessages(messageData, tag);
        plugin.logInfo("Messages for locale %s loaded.", tag);
    }

    @Override
    public void onDisable() {
        messageCache.clear();
    }

    public String formatMessage(Message message, Object... args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i] == null) {
                args[i] = "";
            }
        }

        MessageFormat format = messageCache.get(message);
        if (format == null) {
            return missing == null ? "" : missing.format(new Object[] { message.getKey() });
        }

        return format.format(args);
    }

    public boolean hasMessage(Message message) {
        return messageCache.containsKey(message);
    }

    private void saveDefaultFiles() {
        for (Locale locale : locales) {
            String tag = locale.toLanguageTag();
            String fileName = MessageFormat.format(FILE_PATTERN, tag);
            File file = new File(plugin.getDataFolder(), fileName);
            if (file.exists()) {
                continue;
            }

            try {
                plugin.saveResource(fileName, false);
            } catch (Exception e) {
                plugin.logException(e, "Failed to save message file %s!", fileName);
            }
        }
    }

    private void loadMessages(JsonObject messageData, String tag) {
        for (Entry<String, JsonElement> entry : messageData.entrySet()) {
            String key = entry.getKey();
            Message message = Message.fromKey(key);
            if (message == null) {
                plugin.logInfo("Found unknown message %s in messages file.", key);
                continue;
            }

            cacheMessage(message, entry.getValue().getAsString());
        }

        // 既にある言語ファイルは上書きしない。足りないキーだけ jar から補う
        fillMissingMessages(tag);

        if (missing == null) {
            missing = new MessageFormat("Message missing");
        }
    }

    private void fillMissingMessages(String tag) {
        JsonObject primary = readBundledMessages(tag);
        JsonObject english = "en-US".equals(tag) ? primary : readBundledMessages("en-US");
        int filled = 0;
        for (Message message : Message.values()) {
            if (messageCache.containsKey(message)) {
                continue;
            }
            String text = bundledText(primary, message.getKey());
            if (text == null) {
                text = bundledText(english, message.getKey());
            }
            if (text == null) {
                continue;
            }
            cacheMessage(message, text);
            filled++;
        }
        if (filled > 0) {
            plugin.logInfo("Filled %d missing message(s) from the bundled file.", filled);
        }
    }

    private JsonObject readBundledMessages(String tag) {
        String fileName = MessageFormat.format(FILE_PATTERN, tag);
        try {
            return FileUtils.readJson(plugin, fileName, JsonElement.class).getAsJsonObject();
        } catch (IOException | JsonParseException | IllegalStateException e) {
            plugin.logException(e, "Failed to read bundled message file %s!", fileName);
            return null;
        }
    }

    private static String bundledText(JsonObject data, String key) {
        if (data == null || !data.has(key) || !data.get(key).isJsonPrimitive()) {
            return null;
        }
        return data.get(key).getAsString();
    }

    private void cacheMessage(Message message, String text) {
        String converted = MessageUtils.translateAlternateColorCodes('&', MessageUtils.unescapeJava(text));
        MessageFormat format = new MessageFormat(converted);
        messageCache.put(message, format);
        if (message == Message.MESSAGE_MISSING) {
            missing = format;
        }
    }
}
