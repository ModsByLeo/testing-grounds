package adudecalledleo.serversiding.mixin;

import adudecalledleo.serversiding.impl.SignEditPromptData;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {
    @Shadow public ServerPlayerEntity player;

    @Inject(method = "method_31282", at = @At("HEAD"), cancellable = true)
    public void doSignEditPromptCallback(UpdateSignC2SPacket updateSignC2SPacket, List<String> list, CallbackInfo ci) {
        BlockPos blockPos = updateSignC2SPacket.getPos();
        SignEditPromptData.Entry entry = SignEditPromptData.get(player);
        if (entry == null)
            return;
        if (!blockPos.equals(entry.pos))
            return;
        ci.cancel();
        SignEditPromptData.remove(player);
        entry.succeed(list.stream().map(LiteralText::new).toArray(Text[]::new));
    }

    @Unique
    private void cancelSignEditPrompt() {
        SignEditPromptData.Entry entry = SignEditPromptData.remove(player);
        if (entry != null)
            entry.fail();
    }

    @Inject(method = "onDisconnected", at = @At("HEAD"))
    public void cancelSignEditPromptOnDisconnect(Text reason, CallbackInfo ci) {
        cancelSignEditPrompt();
    }
}
