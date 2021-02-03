package adudecalledleo.serversiding.util;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.text.Text;

import java.util.*;

public final class ItemStackUtils {
    private ItemStackUtils() { }

    /**
     * Removes all enchantments from an {@code ItemStack}
     *
     * @param stack
     *         stack to remove enchantments from
     * @return the given stack
     */
    public static ItemStack removeEnchantments(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null)
            return stack;
        tag.remove("Enchantments");
        return stack;
    }

    /**
     * Adds enchantments to an {@code ItemStack}.
     *
     * @param stack
     *         stack to add enchantments to
     * @param enchantments
     *         enchantments to add
     * @return the given stack
     */
    public static ItemStack addEnchantments(ItemStack stack, Map<Enchantment, Integer> enchantments) {
        if (enchantments == null || enchantments.size() == 0)
            return stack;
        Map<Enchantment, Integer> map = EnchantmentHelper.get(stack);
        map.putAll(enchantments);
        EnchantmentHelper.set(map, stack);
        return stack;
    }

    /**
     * Adds an enchantment to an {@code ItemStack}.
     *
     * @param stack
     *         stack to add enchantment to
     * @param enchantment
     *         enchantment to add
     * @param level
     *         level of enchantment to add
     * @return the given stack
     */
    public static ItemStack addEnchantment(ItemStack stack, Enchantment enchantment, int level) {
        return addEnchantments(stack, EnchantMapBuilder.of(enchantment, level));
    }

    private static ListTag getLoreListTag(ItemStack stack) {
        if (stack.isEmpty())
            return null;
        CompoundTag displayTag = stack.getSubTag("display");
        if (displayTag == null || !displayTag.contains("Lore", /* NbtType.LIST */ 9))
            return null;
        return displayTag.getList("Lore", /* NbtType.STRING */ 8);
    }

    /**
     * Gets an {@code ItemStack}'s lore.
     *
     * @param stack
     *         stack to get lore from
     * @return list of lore lines, or an empty list if the stack didn't have any lore
     */
    public static List<Text> getLore(ItemStack stack) {
        ListTag loreListTag = getLoreListTag(stack);
        if (loreListTag == null)
            return new ArrayList<>();
        final int loreSize = loreListTag.size();
        ArrayList<Text> ret = new ArrayList<>(loreSize);
        for (int i = 0; i < loreSize; i++)
            ret.add(i, Text.Serializer.fromJson(loreListTag.getString(i)));
        return ret;
    }

    /**
     * Removes an {@code ItemStack}'s lore.
     *
     * @param stack
     *         stack to remove lore from
     * @return the given stack
     */
    public static ItemStack removeLore(ItemStack stack) {
        CompoundTag displayTag;
        if ((displayTag = stack.getSubTag("display")) == null)
            return stack;
        displayTag.remove("Lore");
        return stack;
    }

    private static ListTag getOrCreateLoreListTag(ItemStack stack, boolean clear) {
        CompoundTag displayTag = stack.getOrCreateSubTag("display");
        ListTag loreListTag;
        if (displayTag.contains("Lore", /* NbtType.LIST */ 9)) {
            loreListTag = displayTag.getList("Lore", /* NbtType.STRING */ 8);
            if (clear)
                loreListTag.clear();
        } else
            displayTag.put("Lore", loreListTag = new ListTag());
        return loreListTag;
    }

    private static ItemStack addToLoreListTag(ItemStack stack, Collection<Text> lines, boolean clear) {
        ListTag loreListTag = getOrCreateLoreListTag(stack, clear);
        for (Text line : lines)
            loreListTag.add(StringTag.of(Text.Serializer.toJson(line)));
        return stack;
    }

    /**
     * Sets an {@code ItemStack}'s lore.
     *
     * @param stack
     *         stack to set lore of
     * @param lines
     *         lines of lore to set
     * @return the given stack
     */
    public static ItemStack setLore(ItemStack stack, Collection<Text> lines) {
        if (lines.isEmpty())
            return removeLore(stack);
        return addToLoreListTag(stack, lines, true);
    }

    /**
     * Adds to an {@code ItemStack}'s lore.
     *
     * @param stack
     *         stack to add lore to
     * @param lines
     *         lines of lore to add
     * @return the given stack
     */
    public static ItemStack addLore(ItemStack stack, Collection<Text> lines) {
        return addToLoreListTag(stack, lines, false);
    }

    /**
     * Sets an {@code ItemStack}'s lore.
     *
     * @param stack
     *         stack to set lore of
     * @param lines
     *         lines of lore to set
     * @return the given stack
     */
    public static ItemStack setLore(ItemStack stack, Text... lines) {
        return setLore(stack, Arrays.asList(lines));
    }

    /**
     * Adds to an {@code ItemStack}'s lore.
     *
     * @param stack
     *         stack to add lore to
     * @param lines
     *         lines of lore to add
     * @return the given stack
     */
    public static ItemStack addLore(ItemStack stack, Text... lines) {
        return addLore(stack, Arrays.asList(lines));
    }

    private static final ItemStack.TooltipSection[] ALL_HIDE_FLAGS = ItemStack.TooltipSection.values();

    /**
     * Gets the set of hidden tooltip sections of an {@code ItemStack}.
     *
     * @param stack
     *         stack to get hidden tooltip sections of
     * @return the hidden tooltip sections
     */
    public static EnumSet<ItemStack.TooltipSection> getHiddenTooltipSections(ItemStack stack) {
        EnumSet<ItemStack.TooltipSection> flagSet = EnumSet.noneOf(ItemStack.TooltipSection.class);
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("HideFlags", /* NbtType.NUMBER */ 99))
            return flagSet;
        int flags = tag.getInt("HideFlags");
        for (ItemStack.TooltipSection flagEnum : ALL_HIDE_FLAGS) {
            if ((flags & flagEnum.getFlag()) == flags)
                flagSet.add(flagEnum);
        }
        return flagSet;
    }

    private static int evalHideFlags(EnumSet<ItemStack.TooltipSection> hideFlags) {
        int ret = 0;
        for (ItemStack.TooltipSection flag : hideFlags)
            ret |= flag.getFlag();
        return ret;
    }

    /**
     * Sets an {@code ItemStack}'s hidden tooltip sections.
     *
     * @param stack
     *         stack to set hidden tooltip sections of
     * @param tooltipSections
     *         hidden tooltip sections to set
     * @return the given stack
     */
    public static ItemStack setHiddenTooltipSections(ItemStack stack,
            EnumSet<ItemStack.TooltipSection> tooltipSections) {
        stack.getOrCreateTag().putInt("HideFlags", evalHideFlags(tooltipSections));
        return stack;
    }

    /**
     * Hides the specified tooltip sections on the {@code ItemStack}.
     *
     * @param stack
     *         stack to hide the tooltip sections of
     * @param tooltipSections
     *         hidden tooltip sections to hide
     * @return the given stack
     */
    public static ItemStack hideTooltipSections(ItemStack stack, EnumSet<ItemStack.TooltipSection> tooltipSections) {
        if (tooltipSections.isEmpty())
            return stack;
        CompoundTag tag = stack.getOrCreateTag();
        int existingFlags = 0;
        if (tag.contains("HideFlags", /* NbtType.NUMBER */ 99))
            existingFlags = tag.getInt("HideFlags");
        tag.putInt("HideFlags", existingFlags | evalHideFlags(tooltipSections));
        return stack;
    }

    /**
     * Shows the specified tooltip sections on the {@code ItemStack}.
     *
     * @param stack
     *         stack to hide the tooltip sections of
     * @param tooltipSections
     *         hidden tooltip sections to show
     * @return the given stack
     */
    public static ItemStack showTooltipSections(ItemStack stack, EnumSet<ItemStack.TooltipSection> tooltipSections) {
        if (tooltipSections.isEmpty())
            return stack;
        CompoundTag tag = stack.getOrCreateTag();
        int existingFlags = 0;
        if (tag.contains("HideFlags", /* NbtType.NUMBER */ 99))
            existingFlags = tag.getInt("HideFlags");
        existingFlags &= ~evalHideFlags(tooltipSections);
        if (existingFlags == 0)
            tag.remove("HideFlags");
        else
            tag.putInt("HideFlags", existingFlags);
        return stack;
    }

    /**
     * Hides all tooltip sections on the {@code ItemStack}.
     *
     * @param stack
     *         stack to hide the tooltip sections of
     * @return the given stack
     */
    public static ItemStack hideAllTooltipSections(ItemStack stack) {
        return setHiddenTooltipSections(stack, EnumSet.allOf(ItemStack.TooltipSection.class));
    }

    /**
     * Shows all tooltip sections on the {@code ItemStack}.
     *
     * @param stack
     *         stack to show the tooltip sections of
     * @return the given stack
     */
    public static ItemStack showAllTooltipSections(ItemStack stack) {
        CompoundTag tag;
        if ((tag = stack.getTag()) == null)
            return stack;
        tag.remove("HideFlags");
        return stack;
    }

    /**
     * Checks if an {@code ItemStack} is flagged as unbreakable.
     *
     * @param stack
     *         stack to check
     * @return {@code true} if the stack is flagged as unbreakable, {@code false} otherwise
     */
    public static boolean isUnbreakable(ItemStack stack) {
        CompoundTag tag;
        if ((tag = stack.getTag()) == null || !tag.contains("Unbreakable", /* NbtType.BYTE */ 1))
            return false;
        return tag.getBoolean("Unbreakable");
    }

    /**
     * Sets an {@code ItemStack}'s "unbreakable" flag.
     *
     * @param stack
     *         stack to set unbreakable flag of
     * @param unbreakable
     *         value to set unbreakable flag to
     * @return the given stack
     */
    public static ItemStack setUnbreakable(ItemStack stack, boolean unbreakable) {
        CompoundTag tag;
        if (unbreakable) {
            tag = stack.getOrCreateTag();
            tag.putBoolean("Unbreakable", true);
        } else {
            if ((tag = stack.getTag()) == null)
                return stack;
            tag.remove("Unbreakable");
        }
        return stack;
    }
}
