package adudecalledleo.mcmail.api.message;

import com.google.common.collect.ImmutableList;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.text.Text;

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

        public Builder addBodyLine(Text line) {
            bodyBuilder.add(line);
            return this;
        }

        public Builder addBodyLines(Text... lines) {
            bodyBuilder.add(lines);
            return this;
        }

        public Builder addBodyLines(Iterable<Text> lines) {
            bodyBuilder.addAll(lines);
            return this;
        }

        public Builder addInventoryStack(ItemStack stack) {
            inventoryBuilder.add(stack);
            return this;
        }

        public Builder addInventoryStacks(ItemStack... stacks) {
            inventoryBuilder.add(stacks);
            return this;
        }

        public Builder addInventoryStacks(Iterable<ItemStack> stacks) {
            inventoryBuilder.addAll(stacks);
            return this;
        }

        public MessageContents build() {
            return new MessageContents(title, bodyBuilder.build(), inventoryBuilder.build());
        }
    }

    public static Builder builder(Text title) {
        return new Builder(title);
    }

    public static MessageContents fromTag(CompoundTag tag) {
        if (!tag.contains("title", NbtType.STRING) || !tag.contains("body", NbtType.LIST)
                || !tag.contains("inventory", NbtType.LIST))
            return null;
        Text title = Text.Serializer.fromJson(tag.getString("title"));
        ImmutableList.Builder<Text> bodyBuilder = ImmutableList.builder();
        ListTag bodyList = tag.getList("body", NbtType.STRING);
        for (int i = 0; i < bodyList.size(); i++)
            //noinspection ConstantConditions
            bodyBuilder.add(Text.Serializer.fromJson(bodyList.getString(i)));
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

    public CompoundTag toTag(CompoundTag tag) {
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
