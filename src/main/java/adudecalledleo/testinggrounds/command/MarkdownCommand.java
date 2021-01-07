package adudecalledleo.testinggrounds.command;

import adudecalledleo.craftdown.parse.MarkdownParser;
import adudecalledleo.craftdown.node.Node;
import adudecalledleo.craftdown.render.TextRenderer;
import adudecalledleo.craftdown.util.StyleUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

import static adudecalledleo.testinggrounds.TestingGrounds.LOGGER;
import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

class MarkdownCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("md")
                .then(argument("src", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            ServerPlayerEntity player = ctx.getSource().getPlayer();
                            String src = StringArgumentType.getString(ctx, "src");
                            execute(player, src);
                            return SINGLE_SUCCESS;
                        })
                )
        );
    }

    private static final MarkdownParser PARSER = MarkdownParser.builder().parseLinks(true).build();
    private static final TextRenderer RENDERER = TextRenderer.builder()
            .linkStyleTransformer((url, style) ->
                    StyleUtils.withUnderline(
                            style.withColor(Formatting.BLUE).withHoverEvent(
                                    new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                            new LiteralText("Go to " + url.toString()))),
                            true)
            ).build();

    private static void execute(ServerPlayerEntity player, String src) {
        Node root;
        try {
            root = PARSER.parse(src.replaceAll("\\\\n", "\n"));
        } catch (Exception e) {
            player.sendMessage(new LiteralText("Parse failed :(").styled(style -> style.withColor(Formatting.RED)),
                    false);
            LOGGER.error("parse fucked up", e);
            return;
        }
        List<Text> lines;
        try {
            lines = RENDERER.render(root);
        }  catch (Exception e) {
            player.sendMessage(new LiteralText("Render failed :(").styled(style -> style.withColor(Formatting.RED)),
                    false);
            LOGGER.error("render fucked up", e);
            return;
        }
        player.sendMessage(new LiteralText("Parse and render successful:"), false);
        for (Text line : lines)
            player.sendMessage(line, false);
    }
}
