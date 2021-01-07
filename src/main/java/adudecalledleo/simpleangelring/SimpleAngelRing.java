package adudecalledleo.simpleangelring;

import io.github.ladysnake.pal.AbilitySource;
import io.github.ladysnake.pal.Pal;
import io.github.ladysnake.pal.VanillaAbilities;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class SimpleAngelRing implements ModInitializer {
    public static final String MOD_ID = "simpleangelring";
    public static final String MOD_NAME = "Simple Angel Ring";

    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    public static final class AngelRingItem extends Item {
        public AngelRingItem(Settings settings) {
            super(settings);
        }

        @Override
        public boolean hasGlint(ItemStack stack) {
            return true;
        }

        @Environment(EnvType.CLIENT)
        @Override
        public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
            tooltip.add(new TranslatableText(getTranslationKey() + ".tooltip[0]")
                    .styled(style -> style.withColor(Formatting.DARK_GRAY).withItalic(true)));
            tooltip.add(new TranslatableText(getTranslationKey() + ".tooltip[1]")
                    .styled(style -> style.withColor(Formatting.DARK_GRAY).withItalic(true)));
        }
    }

    public static final AngelRingItem ITEM = new AngelRingItem(new Item.Settings()
            .fireproof() // this thing is (probably) gonna be expensive!
            .rarity(Rarity.EPIC)
            .group(ItemGroup.TRANSPORTATION));

    public static final AbilitySource ABILITY_SOURCE = Pal.getAbilitySource(MOD_ID, "angel_ring");

    @Override
    public void onInitialize() {
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "angel_ring"), ITEM);
        ServerTickEvents.START_WORLD_TICK.register(this::onStartWorldTick);
        LOGGER.info("Angel Rings: So easy, a Spider could do it. [Simple Angel Ring has initialized!]");
    }

    private void onStartWorldTick(ServerWorld world) {
        for (ServerPlayerEntity player : world.getPlayers()) {
            if (player.isCreative() || player.isSpectator())
                continue;
            boolean hasItem = false;
            ItemStack offHandStack = player.inventory.offHand.get(0);
            if (offHandStack.getItem() == ITEM)
                hasItem = true;
            else {
                for (ItemStack stack : player.inventory.main) {
                    if (stack.getItem() == ITEM) {
                        hasItem = true;
                        break;
                    }
                }
            }
            if (hasItem)
                Pal.grantAbility(player, VanillaAbilities.ALLOW_FLYING, ABILITY_SOURCE);
            else
                Pal.revokeAbility(player, VanillaAbilities.ALLOW_FLYING, ABILITY_SOURCE);
        }
    }
}
