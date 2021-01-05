package adudecalledleo.testinggrounds.command;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.literal;

public final class ModCommands {
    public static void register() {
        CommandRegistrationCallback.EVENT.register(ModCommands::initCommands);
    }

    private static void initCommands(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        dispatcher.register(literal("mailbox").executes(MailboxCommand::execute));
        dispatcher.register(literal("signprompt").executes(SignPromptCommand::execute));
        dispatcher.register(literal("menu").executes(MenuCommand::execute));
        MarkdownCommand.register(dispatcher);
    }
}
