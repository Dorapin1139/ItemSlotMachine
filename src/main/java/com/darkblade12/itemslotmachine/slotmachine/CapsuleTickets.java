package com.darkblade12.itemslotmachine.slotmachine;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

// 当たったときに、指定したカプセルトイ専用のコード付き券を 1 枚ずつ作る
public final class CapsuleTickets {
    private CapsuleTickets() {
    }

    public static List<ItemStack> issue(Plugin plugin, int count, String capsuleName) {
        if (count < 1) {
            return Collections.emptyList();
        }
        if (capsuleName == null || capsuleName.isEmpty()) {
            plugin.getLogger().warning("Capsule ticket name is missing. Capsule tickets were not given.");
            return Collections.emptyList();
        }
        Plugin capsule = Bukkit.getPluginManager().getPlugin("CapsuleToy");
        if (capsule == null || !capsule.isEnabled()) {
            plugin.getLogger().warning("CapsuleToy is not enabled. Capsule tickets were not given.");
            return Collections.emptyList();
        }

        Method method;
        try {
            method = capsule.getClass().getMethod("createCodedTicket", String.class);
        } catch (NoSuchMethodException e) {
            plugin.getLogger().log(Level.WARNING, "CapsuleToy cannot create a ticket for one capsule toy.", e);
            return Collections.emptyList();
        }

        List<ItemStack> tickets = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            try {
                Object created = method.invoke(capsule, capsuleName);
                if (!(created instanceof ItemStack)) {
                    plugin.getLogger().warning("CapsuleToy did not create a ticket.");
                    break;
                }
                tickets.add((ItemStack) created);
            } catch (ReflectiveOperationException e) {
                plugin.getLogger().log(Level.WARNING, "Could not create a CapsuleToy ticket.", e);
                break;
            }
        }
        return tickets;
    }
}
