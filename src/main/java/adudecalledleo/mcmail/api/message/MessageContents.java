package adudecalledleo.mcmail.api.message;

import com.google.common.collect.ImmutableList;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class MessageContents {
    public static final class Builder {
        private final Text title;
        private final ImmutableList.Builder<Text> bodyBuilder;
        private final ImmutableList.Builder<ItemStack> inventoryBuilder;

        private Builder(Text title) {
            this.title = title;
            bodyBuilder = ImmutableList.builder();
            inventoryBuilder = ImmutableList.builder();
        }

        public @NotNull Builder addBodyLine(@NotNull Text line) {
            bodyBuilder.add(line);
            return this;
        }

        public @NotNull Builder addBodyLines(@NotNull Text... lines) {
            bodyBuilder.add(lines);
            return this;
        }

        public @NotNull Builder addBodyLines(@NotNull Iterable<Text> lines) {
            bodyBuilder.addAll(lines);
            return this;
        }

        public @NotNull Builder addInventoryStack(@NotNull ItemStack stack) {
            inventoryBuilder.add(stack);
            return this;
        }

        public @NotNull Builder addInventoryStacks(@NotNull ItemStack... stacks) {
            inventoryBuilder.add(stacks);
            return this;
        }

        public @NotNull Builder addInventoryStacks(@NotNull Iterable<ItemStack> stacks) {
            inventoryBuilder.addAll(stacks);
            return this;
        }

        public @NotNull MessageContents build() {
            return new MessageContents(title, bodyBuilder.build(), inventoryBuilder.build());
        }
    }

    public static Builder builder(@NotNull Text title) {
        return new Builder(title);
    }

    public static MessageContents fromTag(CompoundTag tag) {
        if (tag == null)
            return null;
        if (!tag.contains("title", NbtType.STRING) || !tag.contains("body", NbtType.LIST)
                || !tag.contains("inventory", NbtType.LIST))
            return null;
        Text title = Text.Serializer.fromJson(tag.getString("title"));
        ImmutableList.Builder<Text> bodyBuilder = ImmutableList.builder();
        ListTag bodyList = tag.getList("body", NbtType.STRING);
        for (int i = 0; i < bodyList.size(); i++) {
            Text line = Text.Serializer.fromJson(bodyList.getString(i));
            if (line == null)
                line = LiteralText.EMPTY;
            bodyBuilder.add(line);
        }
        ImmutableList.Builder<ItemStack> inventoryBuilder = ImmutableList.builder();
        ListTag inventoryList = tag.getList("inventory", NbtType.COMPOUND);
        for (int i = 0; i < inventoryList.size(); i++)
            inventoryBuilder.add(ItemStack.fromTag(inventoryList.getCompound(i)));
        return new MessageContents(title, bodyBuilder.build(), inventoryBuilder.build());
    }

    private final Text title;
    private final List<Text> body;
    private final List<ItemStack> inventory;

    private MessageContents(Text title, List<Text> body, List<ItemStack> inventory) {
        this.title = title;
        this.body = body;
        this.inventory = inventory;
    }

    public Text getTitle() {
        return title;
    }

    public List<Text> getBody() {
        return body;
    }

    public List<ItemStack> getInventory() {
        return inventory;
    }

    public @NotNull CompoundTag toTag(@NotNull CompoundTag tag) {
        tag.putString("title", Text.Serializer.toJson(title));
        ListTag bodyList = new ListTag();
        for (Text line : body)
            bodyList.add(StringTag.of(Text.Serializer.toJson(line)));
        tag.put("body", bodyList);
        ListTag inventoryList = new ListTag();
        for (ItemStack stack : inventory)
            inventoryList.add(stack.toTag(new CompoundTag()));
        tag.put("inventory", inventoryList);
        return tag;
    }
}
