package adudecalledleo.testinggrounds.command;

import adudecalledleo.lionutils.item.ItemStackBuilder;
import adudecalledleo.mcmail.api.Mailbox;
import adudecalledleo.mcmail.api.MailboxIdentifier;
import adudecalledleo.mcmail.api.MailboxProvider;
import adudecalledleo.mcmail.api.message.Message;
import adudecalledleo.mcmail.api.message.MessageContents;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

class MailboxCommand {
    private MailboxCommand() { }

    public static int execute(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerCommandSource src = ctx.getSource();
        ServerPlayerEntity player = src.getPlayer();
        UUID playerUuid = player.getUuid();

        MailboxProvider mProv = MailboxProvider.get();
        MailboxIdentifier mId = MailboxIdentifier.of(player.getServerWorld().getRegistryKey(), player.getBlockPos());
        Mailbox mailbox = mProv.getOrCreateMailbox(mId, playerUuid);

        {
            MessageContents contents = MessageContents.builder(new LiteralText("Test message!"))
                    .addBodyLine(new LiteralText("This is a simple test message."))
                    .addBodyLine(new LiteralText("Pretty slick, ain't it?").styled(style -> style.withItalic(true)))
                    .addInventoryStack(new ItemStack(Items.STICK, 32))
                    .addInventoryStack(ItemStackBuilder.create()
                            .setItem(Items.NETHERITE_SWORD)
                            .addMaxEnchantment(Enchantments.SHARPNESS)
                            .addMaxEnchantment(Enchantments.LOOTING)
                            .unbreakable()
                            .setCustomName(new LiteralText("Slayah"))
                            .build())
                    .build();
            Optional<Text> err = mailbox.send(playerUuid, contents);
            if (err.isPresent()) {
                src.sendError(new LiteralText("Failed to perform test send: ").append(err.get()));
                return 0;
            }
        }

        {
            List<Message> messages = mailbox.getMessages();
            if (messages.size() != 1) {
                src.sendError(new LiteralText("Failed to perform test send: " +
                        "message count is " + messages.size() + " but it should be 1"));
                return 0;
            }
            Message message = messages.get(0);
            src.sendFeedback(new LiteralText("Title: ").append(message.getContents().getTitle()), false);
            src.sendFeedback(new LiteralText("Contents: "), false);
            for (Text line : message.getContents().getBody())
                src.sendFeedback(line, false);
            src.sendFeedback(new LiteralText("Contents END"), false);
            MutableText invText = new LiteralText("Inventory: ");
            List<ItemStack> inventory = message.getContents().getInventory();
            for (int i = 0; i < inventory.size(); i++) {
                ItemStack stack = inventory.get(i);
                invText.append(stack.toHoverableText()).append(new LiteralText(" x" + stack.getCount()));
                if (i != inventory.size() - 1)
                    invText.append(new LiteralText(", "));
            }
            src.sendFeedback(invText, false);
            message.delete();
        }

        mProv.removeMailbox(mId);

        return SINGLE_SUCCESS;
    }
}
