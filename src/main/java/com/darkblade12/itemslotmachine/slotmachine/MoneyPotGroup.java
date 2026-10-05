package com.darkblade12.itemslotmachine.slotmachine;

import com.darkblade12.itemslotmachine.ItemSlotMachine;
import com.darkblade12.itemslotmachine.util.FileUtils;
import com.google.gson.JsonParseException;

import java.io.File;
import java.io.IOException;

public final class MoneyPotGroup {
    private double money;
    private transient String name;
    private transient File file;

    public MoneyPotGroup() {
    }

    public static MoneyPotGroup open(ItemSlotMachine plugin, File file, String name, double seed, String seedSource) {
        if (file.isFile()) {
            try {
                MoneyPotGroup loaded = FileUtils.readJson(file, MoneyPotGroup.class);
                if (loaded == null || !Double.isFinite(loaded.money) || loaded.money < 0) {
                    plugin.logWarning("Money pot group file %s is invalid. Slot machines in this group keep their own pots.",
                                       file.getName());
                    return null;
                }
                loaded.name = name;
                loaded.file = file;
                return loaded;
            } catch (IOException | JsonParseException exception) {
                plugin.logException(exception, "Failed to read money pot group %s. Slot machines in this group keep their own pots.",
                                     name);
                return null;
            }
        }

        MoneyPotGroup created = new MoneyPotGroup();
        created.name = name;
        created.file = file;
        created.money = seed;
        try {
            created.save();
        } catch (IOException exception) {
            plugin.logException(exception, "Failed to create money pot group %s.", name);
            return null;
        }
        plugin.logInfo("Money pot group %s started at %s from slot machine %s.", name, Double.toString(seed), seedSource);
        return created;
    }

    public void save() throws IOException {
        FileUtils.saveJson(file, this);
    }

    public String getName() {
        return name;
    }

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;
    }
}
