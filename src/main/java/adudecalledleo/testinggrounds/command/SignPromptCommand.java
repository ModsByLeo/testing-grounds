package adudecalledleo.testinggrounds.command;

import adudecalledleo.serversiding.util.SignEditPrompt;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.DyeColor;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

class SignPromptCommand {
    private SignPromptCommand() { }

    public static int execute(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerCommandSource src = ctx.getSource();
        ServerPlayerEntity player = src.getPlayer();

        SignEditPrompt.open(player, player.getBlockPos(), result -> {
            if (result.isSuccessful()) {
                for (int i = 0; i < result.getLineCount(); i++)
                    player.sendMessage(new LiteralText("Line " + (i + 1) + ": ").append(result.getLine(i)), false);
            } else
                System.err.println("rats, it failed");
        }, SignEditPrompt.SignType.OAK, DyeColor.BLACK,
                new LiteralText("Test!"), new LiteralText("Test 2!"),
                new LiteralText("Test the Third!"), new LiteralText("Test IV!"));

        return SINGLE_SUCCESS;
    }
}
