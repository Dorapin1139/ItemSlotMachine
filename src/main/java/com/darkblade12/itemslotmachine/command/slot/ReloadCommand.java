package com.darkblade12.itemslotmachine.command.slot;

import com.darkblade12.itemslotmachine.ItemSlotMachine;
import com.darkblade12.itemslotmachine.Permission;
import com.darkblade12.itemslotmachine.plugin.Message;
import com.darkblade12.itemslotmachine.plugin.command.CommandBase;
import com.darkblade12.itemslotmachine.slotmachine.SlotMachine;
import com.darkblade12.itemslotmachine.slotmachine.SlotMachineException;
import com.darkblade12.itemslotmachine.slotmachine.SlotMachineManager;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class ReloadCommand extends CommandBase<ItemSlotMachine> {
    public ReloadCommand() {
        super("reload", Permission.COMMAND_SLOT_RELOAD, "[name]");
    }

    @Override
    public void execute(ItemSlotMachine plugin, CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            long startTime = System.currentTimeMillis();
            if (!plugin.onReload()) {
                plugin.sendMessage(sender, Message.COMMAND_SLOT_RELOAD_FAILED);
                return;
            }

            long duration = System.currentTimeMillis() - startTime;
            String version = plugin.getVersion();
            plugin.sendMessage(sender, Message.COMMAND_SLOT_RELOAD_SUCCEEDED, version, duration);
            plugin.getManager(SlotMachineManager.class).sendLoadFailures(sender);
            return;
        }

        String name = args[0];
        SlotMachineManager manager = plugin.getManager(SlotMachineManager.class);
        SlotMachine slot = manager.getSlotMachine(name);
        if (slot == null) {
            loadUnloadedMachine(plugin, sender, manager, name);
            return;
        }
        name = slot.getName();

        try {
            slot.reload();
        } catch (SlotMachineException e) {
            plugin.logException(e, "Failed to reload slot machine %s!", name);
            plugin.sendMessage(sender, Message.COMMAND_SLOT_RELOAD_SINGLE_FAILED, name, SlotMachineManager.failureReason(e));
            return;
        }

        plugin.sendMessage(sender, Message.COMMAND_SLOT_RELOAD_SINGLE_SUCCEEDED, name);
    }

    private void loadUnloadedMachine(ItemSlotMachine plugin, CommandSender sender, SlotMachineManager manager, String name) {
        File file = manager.findMachineFile(name);
        if (file == null) {
            plugin.sendMessage(sender, Message.SLOT_MACHINE_NOT_FOUND, name);
            return;
        }

        try {
            SlotMachine loaded = SlotMachine.fromFile(plugin, file);
            manager.register(loaded);
            manager.forgetLoadFailure(name);
            manager.forgetLoadFailure(loaded.getName());
            plugin.sendMessage(sender, Message.COMMAND_SLOT_RELOAD_SINGLE_SUCCEEDED, loaded.getName());
        } catch (Exception e) {
            plugin.logException(e, "Failed to reload slot machine %s!", name);
            plugin.sendMessage(sender, Message.COMMAND_SLOT_RELOAD_SINGLE_FAILED, name, SlotMachineManager.failureReason(e));
        }
    }

    @Override
    public List<String> getSuggestions(ItemSlotMachine plugin, CommandSender sender, String[] args) {
        if (args.length != 1) {
            return null;
        }
        SlotMachineManager manager = plugin.getManager(SlotMachineManager.class);
        List<String> names = new ArrayList<>(manager.getNames());
        names.addAll(manager.getLoadFailureNames());
        return names;
    }
}
