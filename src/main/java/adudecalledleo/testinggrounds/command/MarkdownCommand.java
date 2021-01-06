package adudecalledleo.testinggrounds.command;

import adudecalledleo.craftdown.node.Node;
import adudecalledleo.craftdown.node.NodeVisitor;
import adudecalledleo.craftdown.markdown.MarkdownParser;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

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
        NodeDebugger nd = new NodeDebugger();
        LOGGER.info("NodeDebugger START src={}", src);
        root.visit(nd.nodeVisitor);
        LOGGER.info("NodeDebugger END");
        player.sendMessage(new LiteralText("Check the logs!"), false);
    }

    private static final class NodeDebugger {
        private final NodeVisitor nodeVisitor;
        private String indent;

        public NodeDebugger() {
            nodeVisitor = new NodeVisitor(this::visit);
            indent = "";
        }

        private void visit(@NotNull Node node) {
            LOGGER.info("{}{}", indent, node);
            if (node.hasChildren()) {
                LOGGER.info("{}children:", indent);
                String oldIndent = indent;
                indent += " ";
                nodeVisitor.visitChildren(node);
                indent = oldIndent;
                LOGGER.info("{}{} END", indent, node);
            }
        }
    }
}
