package com.darkblade12.itemslotmachine.slotmachine;

import com.darkblade12.itemslotmachine.ItemSlotMachine;
import com.darkblade12.itemslotmachine.coin.CoinManager;
import com.darkblade12.itemslotmachine.plugin.settings.InvalidValueException;
import com.darkblade12.itemslotmachine.plugin.settings.SettingsBase;
import com.darkblade12.itemslotmachine.slotmachine.combo.Action;
import com.darkblade12.itemslotmachine.slotmachine.combo.ActionType;
import com.darkblade12.itemslotmachine.slotmachine.combo.AmountAction;
import com.darkblade12.itemslotmachine.slotmachine.combo.Combo;
import com.darkblade12.itemslotmachine.slotmachine.combo.ItemAction;
import com.darkblade12.itemslotmachine.util.ItemUtils;
import com.google.common.primitives.Ints;
import com.google.gson.JsonParseException;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class SlotMachineSettings extends SettingsBase<ItemSlotMachine> {
    private static final Pattern MONEY_POT_GROUP_NAME = Pattern.compile("[A-Za-z0-9_-]{1,32}");
    private static final Pattern CAPSULE_TICKET_NAME = Pattern.compile("[0-9A-Za-z_]+");
    private static final int MAX_SYMBOL_WEIGHT = 10000;
    private final File file;
    int coinAmount;
    Material[] symbolTypes;
    boolean allowCreative;
    boolean individualPermission;
    boolean launchFireworks;
    int reelStop;
    int[] reelDelay;
    boolean anticipate;
    int anticipateSpins;
    double winningChance;
    boolean triplePaysPot;
    boolean symbolWeightsActive;
    int[] symbolWeights;
    int symbolWeightTotal;
    int lockTime;
    String[] winCommands;
    SoundInfo[] spinSounds;
    SoundInfo[] winSounds;
    SoundInfo[] loseSounds;
    boolean moneyPotEnabled;
    double moneyPotDefault;
    double moneyPotRaise;
    double moneyPotHouseCut;
    String moneyPotGroup;
    boolean itemPotEnabled;
    ItemStack[] itemPotDefault;
    ItemStack[] itemPotRaise;
    Combo[] combos;
    String capsuleTicketName;

    public SlotMachineSettings(ItemSlotMachine plugin, File file) {
        super(plugin);
        this.file = file;
    }

    public SlotMachineSettings(ItemSlotMachine plugin, String name) {
        this(plugin, new File(plugin.getManager(SlotMachineManager.class).getDataDirectory(), name + ".yml"));
    }

    @Override
    public void load() throws InvalidValueException {
        config = YamlConfiguration.loadConfiguration(file);
        Map<String, ItemStack> customItems = plugin.getManager(CoinManager.class).getCustomItems();
        capsuleTicketName = config.getString("capsule-ticket-name", "infernal");
        if (capsuleTicketName == null || !CAPSULE_TICKET_NAME.matcher(capsuleTicketName).matches()) {
            throw new InvalidValueException("The value of setting {0} is invalid.", "capsule-ticket-name");
        }

        coinAmount = config.getInt(Setting.COIN_AMOUNT.getPath(), 1);
        if (coinAmount < 1) {
            throw new InvalidValueException("The value of setting {0} cannot be lower than 0.", Setting.COIN_AMOUNT);
        }

        loadSymbols();
        allowCreative = config.getBoolean(Setting.ALLOW_CREATIVE.getPath(), true);
        launchFireworks = config.getBoolean(Setting.LAUNCH_FIREWORKS.getPath(), true);
        individualPermission = config.getBoolean(Setting.INDIVIDUAL_PERMISSION.getPath());

        reelStop = config.getInt(Setting.REEL_STOP.getPath());
        List<Integer> reelDelayList = config.getIntegerList(Setting.REEL_DELAY.getPath());
        if (reelDelayList.size() != 3) {
            throw new InvalidValueException("The list size of setting {0} must be 3.", Setting.REEL_DELAY);
        }
        reelDelay = Ints.toArray(reelDelayList);

        anticipate = config.getBoolean(Setting.ANTICIPATE.getPath());
        String anticipatePath = Setting.ANTICIPATE_SPINS.getPath();
        anticipateSpins = config.contains(anticipatePath) ? config.getInt(anticipatePath) : 8;
        if (anticipateSpins < 0 || anticipateSpins > 40) {
            throw new InvalidValueException("The value of setting {0} must be between 0 and 40.", anticipatePath);
        }

        winningChance = config.getDouble(Setting.WINNING_CHANCE.getPath());
        if (winningChance > 100) {
            throw new InvalidValueException("The value of setting {0} cannot be higher than 100.", Setting.WINNING_CHANCE);
        }
        String triplePath = Setting.TRIPLE_PAYS_POT.getPath();
        triplePaysPot = config.contains(triplePath) ? config.getBoolean(triplePath) : true;
        lockTime = config.getInt(Setting.LOCK_TIME.getPath());
        List<String> winCommandsList = config.getStringList(Setting.WIN_COMMANDS.getPath());
        winCommands = new String[winCommandsList.size()];
        for (int i = 0; i < winCommands.length; i++) {
            String command = winCommandsList.get(i);
            if (command.startsWith("/")) {
                if (command.length() == 1) {
                    throw new InvalidValueException("A list value of setting {0} contains the invalid command {1}.",
                                                    Setting.WIN_COMMANDS, command);
                }
                winCommands[i] = command.substring(1);
            } else {
                winCommands[i] = command;
            }
        }

        spinSounds = convertSounds(Setting.SOUNDS_SPIN);
        winSounds = convertSounds(Setting.SOUNDS_WIN);
        loseSounds = convertSounds(Setting.SOUNDS_LOSE);

        moneyPotEnabled = config.getBoolean(Setting.MONEY_POT_ENABLED.getPath());
        if (moneyPotEnabled) {
            moneyPotDefault = config.getDouble(Setting.MONEY_POT_DEFAULT.getPath());
            if (moneyPotDefault < 0) {
                throw new InvalidValueException("The value of setting {0} cannot be lower than 0.", Setting.MONEY_POT_DEFAULT);
            }

            moneyPotRaise = config.getDouble(Setting.MONEY_POT_RAISE.getPath());
            if (moneyPotRaise < 0) {
                throw new InvalidValueException("The value of setting {0} cannot be lower than 0.", Setting.MONEY_POT_RAISE);
            }

            moneyPotHouseCut = config.getDouble(Setting.MONEY_POT_HOUSE_CUT.getPath());
            String groupPath = Setting.MONEY_POT_GROUP.getPath();
            String rawGroup = config.getString(groupPath);
            if (rawGroup != null && !rawGroup.isBlank()) {
                if (!MONEY_POT_GROUP_NAME.matcher(rawGroup).matches()) {
                    throw new InvalidValueException("The value of setting {0} contains the invalid group name {1}.", groupPath, rawGroup);
                }
                moneyPotGroup = rawGroup;
            }
            if (moneyPotHouseCut == 100) {
                throw new InvalidValueException("The percentage value of setting {0} cannot be equal to 100.", Setting.MONEY_POT_HOUSE_CUT);
            } else if (moneyPotHouseCut > 100) {
                throw new InvalidValueException("The percentage value of setting {0} cannot be higher than 100.",
                                                Setting.MONEY_POT_HOUSE_CUT);
            }
        }

        itemPotEnabled = config.getBoolean(Setting.ITEM_POT_ENABLED.getPath());
        if (itemPotEnabled) {
            itemPotDefault = convertItems(Setting.ITEM_POT_DEFAULT, customItems);
            itemPotRaise = convertItems(Setting.ITEM_POT_RAISE, customItems);
        } else {
            itemPotDefault = new ItemStack[0];
            itemPotRaise = new ItemStack[0];
        }

        if (!moneyPotEnabled && !itemPotEnabled) {
            throw new InvalidValueException("At least one pot has to be enabled.");
        }

        ConfigurationSection comboSection = config.getConfigurationSection(Setting.COMBOS.getPath());
        if (comboSection != null) {
            Set<String> comboNames = comboSection.getKeys(false);
            combos = new Combo[comboNames.size()];
            int index = 0;
            for (String name : comboNames) {
                String basePath = String.format("%s.%s.", comboSection.getCurrentPath(), name);
                Material[] pattern = convertMaterials(basePath + "pattern", 3, 3, true);
                List<Action> parsedActions = new ArrayList<>();
                String moneyPath = basePath + "money";
                if (config.contains(moneyPath)) {
                    double money = config.getDouble(moneyPath);
                    if (money <= 0) {
                        throw new InvalidValueException("The value of setting {0} must be higher than 0.", moneyPath);
                    }
                    parsedActions.add(new AmountAction(ActionType.PAY_OUT_MONEY, money));
                }
                String itemsPath = basePath + "items";
                if (config.contains(itemsPath)) {
                    List<String> itemNames = config.getStringList(itemsPath);
                    if (itemNames.isEmpty()) {
                        throw new InvalidValueException("The list size of setting {0} cannot be lower than {1}.", itemsPath, 1);
                    }
                    List<ItemStack> prizeItems = new ArrayList<>(itemNames.size());
                    for (int i = 0; i < itemNames.size(); i++) {
                        String item = itemNames.get(i);
                        try {
                            prizeItems.add(ItemUtils.fromString(item, customItems));
                        } catch (IllegalArgumentException ex) {
                            throw new InvalidValueException("A list value of setting {0} contains the invalid item {1}.", itemsPath, item);
                        } catch (JsonParseException ex) {
                            throw new InvalidValueException("The list value of setting {0} at index {1} could not be parsed.", itemsPath, i + 1);
                        }
                    }
                    parsedActions.add(new ItemAction(ActionType.PAY_OUT_ITEMS, prizeItems));
                }
                String ticketPath = basePath + "capsule-tickets";
                if (config.contains(ticketPath)) {
                    int tickets = config.getInt(ticketPath);
                    if (tickets < 1 || tickets > 64) {
                        throw new InvalidValueException("The value of setting {0} must be between 1 and 64.", ticketPath);
                    }
                    parsedActions.add(new AmountAction(ActionType.GIVE_CAPSULE_TICKETS, tickets));
                }
                String actionPath = basePath + "actions";
                List<String> actionList = config.getStringList(actionPath);
                for (String action : actionList) {
                    try {
                        parsedActions.add(Action.fromString(action, customItems));
                    } catch (IllegalArgumentException ex) {
                        throw new InvalidValueException("A list value of setting {0} contains the invalid action {1}.", actionPath, action);
                    }
                }
                if (parsedActions.isEmpty()) {
                    throw new InvalidValueException("The list size of setting {0} cannot be lower than {1}.", actionPath, 1);
                }

                combos[index++] = new Combo(pattern, parsedActions.toArray(new Action[0]));
            }
        } else {
            combos = new Combo[0];
        }
    }

    private void loadSymbols() throws InvalidValueException {
        String path = Setting.SYMBOL_TYPES.getPath();
        List<String> materialList = config.getStringList(path);
        int size = materialList.size();
        if (size < 2) {
            throw new InvalidValueException("The list size of setting {0} cannot be lower than {1}.", path, 2);
        }

        Material[] materials = new Material[size];
        int[] weights = new int[size];
        Set<Material> seen = new HashSet<>();
        int total = 0;
        int firstWeight = -1;
        boolean weighted = false;
        for (int i = 0; i < size; i++) {
            ParsedSymbol parsed = parseSymbol(path, materialList.get(i));
            if (!seen.add(parsed.material)) {
                throw new InvalidValueException("A list value of setting {0} contains the duplicate material {1}.", path,
                                                parsed.material.name());
            }
            materials[i] = parsed.material;
            weights[i] = parsed.weight;
            total += parsed.weight;
            if (firstWeight == -1) {
                firstWeight = parsed.weight;
            } else if (parsed.weight != firstWeight) {
                weighted = true;
            }
        }

        symbolTypes = materials;
        symbolWeights = weights;
        symbolWeightTotal = total;
        symbolWeightsActive = weighted;
    }

    private ParsedSymbol parseSymbol(String path, String raw) throws InvalidValueException {
        int dash = raw.lastIndexOf('-');
        if (dash > 0 && dash < raw.length() - 1 && isDigits(raw.substring(dash + 1))) {
            int weight;
            try {
                weight = Integer.parseInt(raw.substring(dash + 1));
            } catch (NumberFormatException ex) {
                throw new InvalidValueException("A list value of setting {0} contains the invalid material {1}.", path, raw);
            }
            if (weight < 1 || weight > MAX_SYMBOL_WEIGHT) {
                throw new InvalidValueException("A list value of setting {0} contains the invalid weight {1}.", path, raw);
            }
            Material material = Material.matchMaterial(raw.substring(0, dash));
            if (material == null || material == Material.AIR) {
                throw new InvalidValueException("A list value of setting {0} contains the invalid material {1}.", path, raw);
            }
            return new ParsedSymbol(material, weight);
        }

        Material material = Material.matchMaterial(raw);
        if (material == null || material == Material.AIR) {
            throw new InvalidValueException("A list value of setting {0} contains the invalid material {1}.", path, raw);
        }
        return new ParsedSymbol(material, 1);
    }

    private static boolean isDigits(String text) {
        if (text.isEmpty()) {
            return false;
        }
        for (int i = 0; i < text.length(); i++) {
            if (!Character.isDigit(text.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static final class ParsedSymbol {
        private final Material material;
        private final int weight;

        private ParsedSymbol(Material material, int weight) {
            this.material = material;
            this.weight = weight;
        }
    }

    private Material[] convertMaterials(String path, int minSize, int maxSize, boolean allowAir) throws InvalidValueException {
        List<String> materialList = config.getStringList(path);
        int size = materialList.size();
        if (minSize > 0 && minSize == maxSize && size != minSize) {
            throw new InvalidValueException("The list size of setting {0} must be {1}.", path, minSize);
        } else if (minSize > 0 && size < minSize) {
            throw new InvalidValueException("The list size of setting {0} cannot be lower than {1}.", path, minSize);
        } else if (maxSize > 0 && size > maxSize) {
            throw new InvalidValueException("The list size of setting {0} cannot be higher than {1}.", path, maxSize);
        }

        Material[] materials = new Material[size];
        for (int i = 0; i < materials.length; i++) {
            String name = materialList.get(i);
            materials[i] = name.equals("*") ? Material.AIR : Material.matchMaterial(name);
            if (materials[i] == null || !allowAir && materials[i] == Material.AIR) {
                throw new InvalidValueException("A list value of setting {0} contains the invalid material {1}.", path, name);
            }
        }

        return materials;
    }

    private SoundInfo[] convertSounds(Setting setting) throws InvalidValueException {
        List<String> soundList = config.getStringList(setting.getPath());
        SoundInfo[] sounds = new SoundInfo[soundList.size()];
        for (int i = 0; i < sounds.length; i++) {
            String sound = soundList.get(i);
            try {
                sounds[i] = SoundInfo.fromString(sound);
            } catch (IllegalArgumentException ex) {
                throw new InvalidValueException("A list value of setting {0} contains the invalid sound {1}.", setting, sound);
            }
        }

        return sounds;
    }

    private ItemStack[] convertItems(Setting setting, Map<String, ItemStack> customItems) throws InvalidValueException {
        List<String> itemList = config.getStringList(setting.getPath());
        ItemStack[] items = new ItemStack[itemList.size()];
        for (int i = 0; i < items.length; i++) {
            String item = itemList.get(i);
            try {
                items[i] = ItemUtils.fromString(item, customItems);
            } catch (IllegalArgumentException ex) {
                throw new InvalidValueException("A list value of setting {0} contains the invalid item {1}.", setting, item);
            } catch (JsonParseException ex2) {
                throw new InvalidValueException("The list value of setting {0} at index {1} could not be parsed.", setting, i + 1);
            }
        }

        return items;
    }

    @Override
    public void unload() {
    }

    public void deleteFile() throws IOException {
        if (!file.exists()) {
            return;
        }

        Files.delete(file.toPath());
    }

    public File getFile() {
        return file;
    }

    public int getCoinAmount() {
        return coinAmount;
    }

    public Material[] getSymbolTypes() {
        return symbolTypes.clone();
    }

    public boolean getAllowCreative() {
        return allowCreative;
    }

    public boolean hasIndividualPermission() {
        return individualPermission;
    }

    public boolean isLaunchFireworks() {
        return launchFireworks;
    }

    public int getReelStop() {
        return reelStop;
    }

    public int[] getReelDelay() {
        return reelDelay.clone();
    }

    public double getWinningChance() {
        return winningChance;
    }

    public int getLockTime() {
        return lockTime;
    }

    public String[] getWinCommands() {
        return winCommands.clone();
    }

    public SoundInfo[] getSpinSounds() {
        return spinSounds.clone();
    }

    public SoundInfo[] getWinSounds() {
        return winSounds.clone();
    }

    public SoundInfo[] getLoseSounds() {
        return loseSounds.clone();
    }

    public boolean isMoneyPotEnabled() {
        return moneyPotEnabled;
    }

    public double getMoneyPotDefault() {
        return moneyPotDefault;
    }

    public double getMoneyPotRaise() {
        return moneyPotRaise;
    }

    public double getMoneyPotHouseCut() {
        return moneyPotHouseCut;
    }

    public String getMoneyPotGroup() {
        return moneyPotGroup;
    }

    public boolean isItemPotEnabled() {
        return itemPotEnabled;
    }

    public ItemStack[] getItemPotDefault() {
        return itemPotDefault.clone();
    }

    public ItemStack[] getItemPotRaise() {
        return itemPotRaise.clone();
    }

    public Combo[] getCombos() {
        return combos.clone();
    }
}
