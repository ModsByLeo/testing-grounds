package adudecalledleo.testinggrounds.item;

import adudecalledleo.mcmail.api.Letter;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

public class LetterItem extends Item {
    public LetterItem(Settings settings) {
        super(settings);
    }

    public static void saveLetter(Letter letter, ItemStack to) {
        CompoundTag tag = to.getOrCreateTag();
        tag.put("letter", letter.serialize());
    }

    public static Letter loadLetter(ItemStack from) {
        if (!from.hasTag())
            return Letter.EMPTY;
        CompoundTag tag = from.getTag();
        if (tag == null || !tag.contains("letter", NbtType.COMPOUND))
            return Letter.EMPTY;
        return Letter.deserialize(tag.getCompound("letter"));
    }
}
