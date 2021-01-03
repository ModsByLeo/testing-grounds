package adudecalledleo.testinggrounds.command;

import adudecalledleo.lionutils.item.ItemStackBuilder;
import adudecalledleo.serversiding.input.SignPrompt;
import adudecalledleo.serversiding.menu.MenuFactory;
import adudecalledleo.serversiding.menu.simple.SimpleMenuHandler;
import adudecalledleo.serversiding.menu.simple.button.*;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import static adudecalledleo.serversiding.ServerSiding.LOGGER;
import static adudecalledleo.serversiding.menu.MenuHandler.slot;
import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

class MenuCommand {
    private MenuCommand() { }

    private static final class MyMenuHandler extends SimpleMenuHandler {
        private enum TestEnum {
            FOO(Items.RED_DYE,
                    new LiteralText("Foo").styled(style -> style.withColor(Formatting.RED).withBold(true))),
            BAR(Items.GREEN_DYE,
                    new LiteralText("Bar").styled(style -> style.withColor(Formatting.GREEN).withBold(true))),
            BAZ(Items.BLUE_DYE,
                    new LiteralText("Baz").styled(style -> style.withColor(Formatting.BLUE).withBold(true)));

            private final Item icon;
            private final Text name;

            TestEnum(Item icon, Text name) {
                this.icon = icon;
                this.name = name;
            }

            public @NotNull Item getIcon() {
                return icon;
            }

            public @NotNull Text getName() {
                return name;
            }
        }

        private boolean testBool;
        private TestEnum testEnum;
        private int testChoice;

        public MyMenuHandler() {
            super(5, Items.WHITE_STAINED_GLASS_PANE);
            testBool = true;
            testEnum = TestEnum.FOO;
            testChoice = 0;
            addButton(slot(1, 2), Button.of(
                    (menuState) -> {
                        LOGGER.info("{}: stick was clicked!", this);
                        menuState.closeAndDo(player -> SignPrompt.open(player, player.getBlockPos(), result -> {
                            if (result.isSuccessful())
                                player.sendMessage(
                                        new LiteralText("Thanks, ").append(result.getLine(3)).append(new LiteralText("!")),
                                        false);
                            else
                                player.sendMessage(
                                        new LiteralText("Couldn't get your name :(")
                                                .styled(style -> style.withColor(Formatting.RED)),
                                        false);
                        }, SignPrompt.Background.BIRCH,
                                new LiteralText("YAY!"),
                                new LiteralText("Stick clicked!"),
                                new LiteralText("Enter name below:"),
                                LiteralText.EMPTY));
                    },
                    ItemStackBuilder.create()
                            .setItem(Items.STICK)
                            .setCustomName(new LiteralText("Click me!").styled(style -> style.withBold(true).withItalic(false)))
                            .build()));
            addButton(slot(1, 4), ToggleButton.of(
                    ToggleButton.ValueAccessor.of(() -> testBool, (value, menuState) -> testBool = value),
                    ToggleButton.StackProvider.simple(new LiteralText("Toggle Test"),
                            new LiteralText("Test for toggle buttons."),
                            new LiteralText("Does nothing."))));
            addButton(slot(1, 6), EnumButton.of(
                    EnumButton.ValueAccessor.of(TestEnum.class, () -> testEnum, (value, menuState) -> testEnum = value),
                    EnumButton.StackProvider.simple(TestEnum::getIcon, TestEnum::getName, new LiteralText("Enum Test"),
                            new LiteralText("Test for enum buttons."),
                            new LiteralText("Does nothing."))));
            addButton(slot(3, 0), Label.of(Items.OAK_SIGN,
                    new LiteralText("Radio Button Test"),
                    new LiteralText("Test for radio choices!"),
                    new LiteralText("(does nothing)")));
            RadioButtonGroup.of(
                    RadioButtonGroup.ChoiceAccessor.of(() -> testChoice, (value, menuState) -> testChoice = value),
                    RadioButtonGroup.ChoiceInfoProvider.of(value -> new LiteralText("Radio Choice " + (value + 1)),
                            value -> new Text[] {
                                    new LiteralText("Test for radio choices."),
                                    new LiteralText("Does nothing.")
                    })
            ).addChoiceAt(slot(3, 2)).addChoiceAt(slot(3, 4)).addChoiceAt(slot(3, 6))
                    .addTo(this);
        }
    }

    private static final MenuFactory MENU_FACTORY = new MenuFactory(new LiteralText("Testing!"), MyMenuHandler::new);

    public static int execute(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerCommandSource src = ctx.getSource();
        ServerPlayerEntity player = src.getPlayer();

        player.openHandledScreen(MENU_FACTORY);

        return SINGLE_SUCCESS;
    }
}
