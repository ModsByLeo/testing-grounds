package adudecalledleo.mcmail.api;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class Message {
    @SuppressWarnings("UnusedReturnValue")
    public static final class Builder {
        private final UUID authorUUID;
        private Text title;
        private final ImmutableList.Builder<Text> bodyBuilder;
        private final ImmutableList.Builder<ItemStack> stacksBuilder;

        private Builder(UUID authorUUID) {
            this.authorUUID = authorUUID;
            title = new LiteralText("<no title>");
            bodyBuilder = ImmutableList.builder();
            stacksBuilder = ImmutableList.builder();
        }

        public Builder setTitle(Text title) {
            this.title = title;
            return this;
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

        public Builder addStack(ItemStack stack) {
            stacksBuilder.add(stack);
            return this;
        }

        public Builder addStacks(ItemStack... stacks) {
            stacksBuilder.add(stacks);
            return this;
        }

        public Builder addStacks(Iterable<ItemStack> stacks) {
            stacksBuilder.addAll(stacks);
            return this;
        }

        public Message build() {
            return new Message(authorUUID, title,
                    bodyBuilder.build(),
                    stacksBuilder.build());
        }
    }

    public static Builder builder(UUID authorUUID) {
        Preconditions.checkNotNull(authorUUID, "authorUUID == null!");
        return new Builder(authorUUID);
    }

    public static final Message EMPTY = new Message(new UUID(0, 0), LiteralText.EMPTY,
            Collections.emptyList(), Collections.emptyList());

    private final UUID authorUUID;
    private final Text title;
    private final List<Text> body;
    private final List<ItemStack> stacks;

    private Message(UUID authorUUID, Text title, List<Text> body, List<ItemStack> stacks) {
        this.authorUUID = authorUUID;
        this.title = title;
        this.body = body;
        this.stacks = stacks;
    }

    public UUID getAuthorUUID() {
        return authorUUID;
    }

    public Text getTitle() {
        return title;
    }

    public List<Text> getBody() {
        return body;
    }

    public List<ItemStack> getStacks() {
        return stacks;
    }

    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        tag.putUuid("author_uuid", authorUUID);
        tag.putString("title", Text.Serializer.toJson(title));
        ListTag bodyList = new ListTag();
        for (Text line : body)
            bodyList.add(StringTag.of(Text.Serializer.toJson(line)));
        tag.put("body", bodyList);
        ListTag stacksTag = new ListTag();
        for (ItemStack stack : stacks)
            stacksTag.add(stack.toTag(new CompoundTag()));
        tag.put("stacks", stacksTag);
        return tag;
    }

    public static Message deserialize(CompoundTag source) {
        if (source == null || !source.containsUuid("author_uuid"))
            return EMPTY;
        Builder builder = new Builder(source.getUuid("author_uuid"));
        if (!source.contains("title", NbtType.STRING))
            return EMPTY;
        builder.setTitle(Text.Serializer.fromJson(source.getString("title")));
        ListTag bodyList = source.getList("body", NbtType.STRING);
        if (bodyList == null)
            return EMPTY;
        for (int i = 0; i < bodyList.size(); i++)
            builder.addBodyLine(Text.Serializer.fromJson(bodyList.getString(i)));
        ListTag stacksList = source.getList("stacks", NbtType.COMPOUND);
        if (stacksList == null)
            return EMPTY;
        for (int i = 0; i < stacksList.size(); i++)
            builder.addStack(ItemStack.fromTag(stacksList.getCompound(i)));
        return builder.build();
    }
}
