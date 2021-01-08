package adudecalledleo.simpleangelring;

import io.github.ladysnake.pal.AbilitySource;
import io.github.ladysnake.pal.Pal;
import io.github.ladysnake.pal.VanillaAbilities;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.*;
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

        public static boolean isRingEnabled(ItemStack stack) {
            if (stack.isEmpty() || stack.getItem() != ITEM)
                return false;
            boolean ringEnabled = true;
            CompoundTag tag = stack.getTag();
            if (tag != null && tag.contains("enabled", NbtType.BYTE))
                ringEnabled = tag.getBoolean("enabled");
            return ringEnabled;
        }

        @Override
        public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
            ItemStack stack = user.getStackInHand(hand);
            if (!user.isSneaking())
                return TypedActionResult.pass(stack);
            boolean wasRingEnabled = isRingEnabled(stack);
            stack.getOrCreateTag().putBoolean("enabled", !wasRingEnabled);
            user.sendMessage(new TranslatableText(getTranslationKey() + (wasRingEnabled ? ".disabled" : ".enabled")),
                    true);
            if (world.isClient)
                world.playSound(user, user.getX(), user.getY(), user.getZ(),
                        wasRingEnabled ? ModSoundEvents.ANGEL_RING_DISABLED : ModSoundEvents.ANGEL_RING_ENABLED,
                        SoundCategory.PLAYERS, 1, wasRingEnabled ? 1.2F : 1);
            return TypedActionResult.consume(stack);
        }

        @Override
        public boolean hasGlint(ItemStack stack) {
            return isRingEnabled(stack);
        }

        @Environment(EnvType.CLIENT)
        @Override
        public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
            for (int i = 0; i < 3; i++)
                tooltip.add(new TranslatableText(getTranslationKey() + ".tooltip[" + i + "]")
                        .styled(style -> style.withColor(Formatting.DARK_GRAY).withItalic(true)));
            if (!isRingEnabled(stack))
                tooltip.add(new TranslatableText(getTranslationKey() + ".tooltip.disabled")
                        .styled(style -> style.withColor(Formatting.RED).withBold(true)));
        }
    }

    public static final AngelRingItem ITEM = new AngelRingItem(new Item.Settings()
            .maxCount(1)
            .fireproof() // this thing is (probably) gonna be expensive!
            .rarity(Rarity.EPIC)
            .group(ItemGroup.TRANSPORTATION));

    public static final AbilitySource ABILITY_SOURCE = Pal.getAbilitySource(MOD_ID, "angel_ring");

    public static final class ModSoundEvents {
        public static final SoundEvent ANGEL_RING_ENABLED = new SoundEvent(new Identifier(MOD_ID, "angel_ring.enabled"));
        public static final SoundEvent ANGEL_RING_DISABLED = new SoundEvent(new Identifier(MOD_ID, "angel_ring.disabled"));
    }

    @Override
    public void onInitialize() {
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "angel_ring"), ITEM);
        registerSoundEvents(ModSoundEvents.ANGEL_RING_ENABLED, ModSoundEvents.ANGEL_RING_DISABLED);
        ServerTickEvents.START_WORLD_TICK.register(this::onStartWorldTick);
        LOGGER.info("Angel Rings: So easy, a Spider could do it. [Simple Angel Ring has initialized!]");
    }

    private void registerSoundEvents(SoundEvent... soundEvents) {
        for (SoundEvent soundEvent : soundEvents)
            Registry.register(Registry.SOUND_EVENT, soundEvent.getId(), soundEvent);
    }

    private void onStartWorldTick(ServerWorld world) {
        for (ServerPlayerEntity player : world.getPlayers()) {
            if (!player.interactionManager.isSurvivalLike())
                continue;
            ItemStack ringStack = player.inventory.getCursorStack();
            if (ringStack.getItem() != ITEM)
                ringStack = player.inventory.offHand.get(0);
            if (ringStack.getItem() != ITEM) {
                ringStack = ItemStack.EMPTY;
                for (ItemStack stack : player.inventory.main) {
                    if (stack.getItem() == ITEM) {
                        ringStack = stack;
                        break;
                    }
                }
            }
            if (AngelRingItem.isRingEnabled(ringStack))
                Pal.grantAbility(player, VanillaAbilities.ALLOW_FLYING, ABILITY_SOURCE);
            else
                Pal.revokeAbility(player, VanillaAbilities.ALLOW_FLYING, ABILITY_SOURCE);
        }
    }
}
