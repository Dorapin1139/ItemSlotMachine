package com.darkblade12.itemslotmachine.slotmachine;

import com.darkblade12.itemslotmachine.ItemSlotMachine;
import com.darkblade12.itemslotmachine.Permission;
import com.darkblade12.itemslotmachine.coin.CoinManager;
import com.darkblade12.itemslotmachine.design.DesignIncompleteException;
import com.darkblade12.itemslotmachine.nameable.Nameable;
import com.darkblade12.itemslotmachine.nameable.NameableComparator;
import com.darkblade12.itemslotmachine.plugin.Manager;
import com.darkblade12.itemslotmachine.plugin.Message;
import com.darkblade12.itemslotmachine.plugin.settings.InvalidValueException;
import com.darkblade12.itemslotmachine.util.FileUtils;
import com.google.gson.JsonParseException;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSignOpenEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class SlotMachineManager extends Manager<ItemSlotMachine> {
    private final List<SlotMachine> slots;
    private final Map<String, MoneyPotGroup> moneyGroups;
    private final List<LoadFailure> loadFailures;
    private NameableComparator<SlotMachine> comparator;

    public SlotMachineManager(ItemSlotMachine plugin) {
        super(plugin, new File(plugin.getDataFolder(), "slot machines"));
        slots = new ArrayList<>();
        moneyGroups = new HashMap<>();
        loadFailures = new ArrayList<>();
    }

    @Override
    protected void onEnable() {
        comparator = new NameableComparator<>(plugin.getSettings().getSlotMachineNamePattern());
        loadSlotMachines();
    }

    @Override
    protected void onDisable() {
        for (SlotMachine slot : slots) {
            slot.stop(true);
        }
    }

    public void loadSlotMachines() {
        slots.clear();
        moneyGroups.clear();
        loadFailures.clear();

        for (File file : FileUtils.getFiles(dataDirectory, SlotMachine.FILE_EXTENSION)) {
            try {
                slots.add(SlotMachine.fromFile(plugin, file));
            } catch (JsonParseException | InvalidValueException | IOException | DesignIncompleteException e) {
                loadFailures.add(new LoadFailure(machineLabel(file), failureReason(e)));
                plugin.logException(e, "Failed to load slot machine file %s!", file.getName());
            }
        }

        int count = slots.size();
        plugin.logInfo(count + " slot machine" + (count == 1 ? "" : "s") + " loaded.");
        bindMoneyGroups();
    }

    public void register(SlotMachine slot) {
        slots.add(slot);
        bindMoneyGroup(slot);
    }

    public void bindMoneyGroup(SlotMachine slot) {
        String groupName = slot.getSettings().getMoneyPotGroup();
        if (groupName == null) {
            slot.attachMoneyGroup(null);
            return;
        }

        MoneyPotGroup group = moneyGroups.get(groupName);
        if (group == null) {
            group = MoneyPotGroup.open(plugin, moneyPotFile(groupName), groupName, slot.getStoredMoneyPot(), slot.getName());
            if (group != null) {
                moneyGroups.put(groupName, group);
            }
        }
        if (group == null) {
            slot.attachMoneyGroup(null);
            return;
        }
        slot.attachMoneyGroup(group);
    }

    public void syncGroupMoney(String groupName, double money, SlotMachine source) {
        for (SlotMachine slot : slots) {
            if (slot == source || !groupName.equals(slot.getSettings().getMoneyPotGroup())) {
                continue;
            }
            slot.mirrorMoney(money);
        }
    }

    private void bindMoneyGroups() {
        Map<String, List<SlotMachine>> grouped = new HashMap<>();
        for (SlotMachine slot : slots) {
            String groupName = slot.getSettings().getMoneyPotGroup();
            if (groupName == null) {
                slot.attachMoneyGroup(null);
                continue;
            }
            grouped.computeIfAbsent(groupName, key -> new ArrayList<>()).add(slot);
        }

        for (Map.Entry<String, List<SlotMachine>> entry : grouped.entrySet()) {
            String groupName = entry.getKey();
            List<SlotMachine> members = entry.getValue();
            double seed = members.get(0).getStoredMoneyPot();
            String source = members.get(0).getName();
            for (int i = 1; i < members.size(); i++) {
                SlotMachine member = members.get(i);
                if (member.getStoredMoneyPot() > seed) {
                    seed = member.getStoredMoneyPot();
                    source = member.getName();
                }
            }

            MoneyPotGroup group = MoneyPotGroup.open(plugin, moneyPotFile(groupName), groupName, seed, source);
            if (group == null) {
                for (SlotMachine member : members) {
                    member.attachMoneyGroup(null);
                }
                continue;
            }
            moneyGroups.put(groupName, group);
            for (SlotMachine member : members) {
                member.attachMoneyGroup(group);
            }
        }
    }

    private File moneyPotFile(String groupName) {
        return new File(plugin.getDataFolder(), "money-pots" + File.separator + groupName + ".json");
    }

    public void unregister(SlotMachine slot) throws IOException {
        slot.delete();
        slots.remove(slot);
    }

    public String generateName() {
        return Nameable.generateName(getFileNames(true), plugin.getSettings().getSlotMachineNamePattern());
    }

    public List<String> getFileNames(boolean stripExtension) {
        return FileUtils.getFileNames(dataDirectory, stripExtension, SlotMachine.FILE_EXTENSION);
    }

    public List<String> getFileNames() {
        return getFileNames(false);
    }

    public List<String> getNames() {
        return slots.stream().sorted(comparator).map(SlotMachine::getName).collect(Collectors.toList());
    }

    public List<SlotMachine> getSlotMachines() {
        return slots.stream().sorted(comparator).collect(Collectors.toList());
    }

    public SlotMachine getSlotMachine(String name) {
        return slots.stream().filter(s -> s.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public SlotMachine getSlotMachine(Location location) {
        return slots.stream().filter(s -> s.isInsideRegion(location)).findFirst().orElse(null);
    }

    private SlotMachine getInteractedSlotMachine(Location location) {
        return slots.stream().filter(s -> s.isInteraction(location)).findFirst().orElse(null);
    }

    public boolean hasSlotMachine(String name) {
        return slots.stream().anyMatch(s -> s.getName().equalsIgnoreCase(name));
    }

    public File findMachineFile(String name) {
        String target = name + SlotMachine.FILE_EXTENSION;
        for (String fileName : getFileNames()) {
            if (fileName.equalsIgnoreCase(target)) {
                return new File(dataDirectory, fileName);
            }
        }
        return null;
    }

    public List<String> getLoadFailureNames() {
        return loadFailures.stream().map(failure -> failure.name).collect(Collectors.toList());
    }

    public void forgetLoadFailure(String name) {
        loadFailures.removeIf(failure -> failure.name.equalsIgnoreCase(name));
    }

    public void sendLoadFailures(CommandSender sender) {
        if (loadFailures.isEmpty()) {
            return;
        }
        plugin.sendMessage(sender, Message.COMMAND_SLOT_RELOAD_MACHINES_FAILED, loadFailures.size());
        for (LoadFailure failure : loadFailures) {
            plugin.sendMessage(sender, Message.COMMAND_SLOT_RELOAD_MACHINE_FAILED, failure.name, failure.reason);
        }
    }

    public static String failureReason(Throwable throwable) {
        Throwable cause = throwable.getCause();
        if (cause != null && cause.getMessage() != null && !cause.getMessage().isEmpty()) {
            return cause.getMessage();
        }
        if (throwable.getMessage() != null && !throwable.getMessage().isEmpty()) {
            return throwable.getMessage();
        }
        return throwable.getClass().getSimpleName();
    }

    private static String machineLabel(File file) {
        String name = file.getName();
        String extension = SlotMachine.FILE_EXTENSION;
        if (name.toLowerCase().endsWith(extension)) {
            return name.substring(0, name.length() - extension.length());
        }
        return name;
    }

    private static final class LoadFailure {
        private final String name;
        private final String reason;

        private LoadFailure(String name, String reason) {
            this.name = name;
            this.reason = reason;
        }
    }

    private int getSpinningCount(Player player) {
        return (int) slots.stream().filter(s -> s.isSpinning() && s.isUser(player)).count();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onHangingPlace(HangingPlaceEvent event) {
        Player player = event.getPlayer();
        SlotMachine slot = getSlotMachine(event.getBlock().getLocation());
        if (player == null || slot == null || slot.hasModifyPermission(player)) {
            return;
        }

        event.setCancelled(true);
        plugin.sendMessage(player, Message.SLOT_MACHINE_MODIFY_NOT_ALLOWED, slot.getName());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onHangingBreak(HangingBreakEvent event) {
        SlotMachine slot = getSlotMachine(event.getEntity().getLocation());
        if (slot == null) {
            return;
        } else if (!(event instanceof HangingBreakByEntityEvent)) {
            event.setCancelled(true);
            return;
        }

        Entity remover = ((HangingBreakByEntityEvent) event).getRemover();
        if (!(remover instanceof Player)) {
            event.setCancelled(true);
            return;
        }

        Player player = (Player) remover;
        if (slot.hasModifyPermission(player)) {
            return;
        }

        event.setCancelled(true);
        plugin.sendMessage(player, Message.SLOT_MACHINE_MODIFY_NOT_ALLOWED, slot.getName());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        SlotMachine slot = getSlotMachine(event.getBlock().getLocation());
        if (slot == null || slot.hasModifyPermission(player)) {
            return;
        }

        event.setCancelled(true);
        plugin.sendMessage(player, Message.SLOT_MACHINE_MODIFY_NOT_ALLOWED, slot.getName());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        SlotMachine slot = getSlotMachine(event.getBlock().getLocation());
        if (slot == null) {
            slot = getSlotMachine(event.getBlockAgainst().getLocation());
        }

        if (slot == null || slot.hasModifyPermission(player)) {
            return;
        }

        event.setCancelled(true);
        plugin.sendMessage(player, Message.SLOT_MACHINE_MODIFY_NOT_ALLOWED, slot.getName());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();
        if (!(entity instanceof Hanging)) {
            return;
        }

        SlotMachine slot = getSlotMachine(entity.getLocation());
        if (slot == null || slot.hasModifyPermission(player)) {
            return;
        }

        event.setCancelled(true);
        plugin.sendMessage(player, Message.SLOT_MACHINE_MODIFY_NOT_ALLOWED, slot.getName());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Hanging)) {
            return;
        }

        SlotMachine slot = getSlotMachine(entity.getLocation());
        if (slot == null) {
            return;
        } else if (!(event instanceof EntityDamageByEntityEvent)) {
            event.setCancelled(true);
            return;
        }

        Entity damager = ((EntityDamageByEntityEvent) event).getDamager();
        if (!(damager instanceof Player)) {
            event.setCancelled(true);
            return;
        }

        Player player = (Player) damager;
        if (slot.hasModifyPermission(player)) {
            return;
        }

        event.setCancelled(true);
        plugin.sendMessage(player, Message.SLOT_MACHINE_MODIFY_NOT_ALLOWED, slot.getName());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null || event.getHand() == EquipmentSlot.OFF_HAND) {
            return;
        }

        Player player = event.getPlayer();
        Location clickedLoc = clickedBlock.getLocation();
        SlotMachine slot;
        switch (event.getAction()) {
            case LEFT_CLICK_BLOCK:
                slot = getInteractedSlotMachine(clickedLoc);
                if (slot == null || !slot.isStoppable(player)) {
                    return;
                }

                event.setCancelled(true);
                slot.stop();
                break;
            case RIGHT_CLICK_BLOCK:
                ItemStack hand = player.getInventory().getItemInMainHand();
                slot = getSlotMachine(clickedLoc);
                if (slot == null) {
                    return;
                }
                String name = slot.getName();

                if (hand.getType() == Material.WATER_BUCKET || hand.getType() == Material.LAVA_BUCKET) {
                    if (!slot.hasModifyPermission(player)) {
                        event.setCancelled(true);
                        plugin.sendMessage(player, Message.SLOT_MACHINE_MODIFY_NOT_ALLOWED, name);
                    }
                    return;
                }

                // 看板の文字は updateSign() が上書きするので、染料・墨・ミツロウも権限に関係なく全員止める
                if (isSignModifier(hand.getType()) && slot.isPotSign(clickedLoc)) {
                    event.setCancelled(true);
                    return;
                }

                CoinManager coinManager = plugin.getManager(CoinManager.class);
                boolean holdingUseItem = !hand.getType().isBlock() || hand.getType() == Material.AIR;
                boolean holdingCoin = coinManager.isCoin(hand);
                if (holdingUseItem && !holdingCoin && Permission.SLOT_INSPECT.test(player)) {
                    plugin.sendMessage(player, Message.SLOT_MACHINE_INSPECTED, name);
                    return;
                } else if (!slot.isInteraction(clickedLoc) || !holdingCoin) {
                    return;
                }

                event.setCancelled(true);
                if (!slot.hasUsePermission(player)) {
                    plugin.sendMessage(player, Message.SLOT_MACHINE_USE_NOT_ALLOWED, name);
                    return;
                }

                if (slot.isBroken()) {
                    plugin.sendMessage(player, Message.SLOT_MACHINE_BROKEN, name);
                    return;
                }

                if (slot.isSpinning()) {
                    plugin.sendMessage(player, Message.SLOT_MACHINE_STILL_SPINNING);
                    return;
                }

                if (slot.getSettings().lockTime > 0 && !slot.isLockExpired() && !slot.isUser(player)) {
                    plugin.sendMessage(player, Message.SLOT_MACHINE_LOCKED, slot.getUserName(), slot.getRemainingLockTime());
                    return;
                }

                if (player.getGameMode() == GameMode.CREATIVE && !slot.getSettings().allowCreative) {
                    plugin.sendMessage(player, Message.SLOT_MACHINE_NO_CREATIVE);
                    return;
                }

                if (!slot.hasEnoughCoins(player)) {
                    String singular = plugin.formatMessage(Message.WORD_COIN_SINGULAR);
                    String plural = plugin.formatMessage(Message.WORD_COIN_PLURAL);
                    int required = slot.getSettings().coinAmount;
                    String requiredCoins = required == 1 ? singular : plural;
                    int current = hand.getAmount();
                    String currentCoins = current == 1 ? singular : plural;
                    plugin.sendMessage(player, Message.SLOT_MACHINE_NOT_ENOUGH_COINS, required, requiredCoins, current,
                                       currentCoins);
                    return;
                }

                int useLimit = plugin.getSettings().getSlotMachineUseLimit();
                if (useLimit > 0 && getSpinningCount(player) + 1 > useLimit) {
                    plugin.sendMessage(player, Message.SLOT_MACHINE_USE_LIMITED, useLimit);
                    return;
                }

                slot.spin(player);
                break;
            default:
                break;
        }
    }

    private static boolean isSignModifier(Material type) {
        return type == Material.INK_SAC || type == Material.GLOW_INK_SAC || type == Material.HONEYCOMB
            || type.name().endsWith("_DYE");
    }

    // 看板の文字は updateSign() が上書きするので、編集画面は権限に関係なく全員に開かせない
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerSignOpen(PlayerSignOpenEvent event) {
        Location location = event.getSign().getLocation();
        if (slots.stream().anyMatch(s -> s.isPotSign(location))) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        for (SlotMachine slot : slots) {
            if (slot.isUser(player)) {
                slot.stop(true);
            }
        }
    }
}
