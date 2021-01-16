package adudecalledleo.globalclipboard.mixin;

import adudecalledleo.globalclipboard.ClipboardHolder;
import net.minecraft.client.Keyboard;
import net.minecraft.client.font.TextVisitFactory;
import org.apache.commons.io.IOUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.datatransfer.*;
import java.io.IOException;
import java.io.Reader;

import static adudecalledleo.globalclipboard.Initializer.LOGGER;

@Mixin(Keyboard.class)
public abstract class KeyboardMixin {
    @Inject(method = "getClipboard", at = @At("HEAD"), cancellable = true)
    public void getSystemClipboard(CallbackInfoReturnable<String> cir) {
        String text = "";
        Clipboard cb = ClipboardHolder.getClipboard();
        Transferable transferable = null;
        try {
            transferable = cb.getContents(this);
        } catch (IllegalStateException e) {
            LOGGER.error("Failed to query contents of clipboard", e);
        }
        if (transferable != null) {
            Reader reader = null;
            try {
                reader = ClipboardHolder.getBestTextFlavor().getReaderForText(transferable);
            } catch (UnsupportedFlavorException | IOException e) {
                LOGGER.error("Failed to get clipboard text reader", e);
            }
            if (reader != null) {
                try {
                    text = IOUtils.toString(reader);
                } catch (IOException e) {
                    LOGGER.error("Failed to read text from clipboard", e);
                }
            }
        }
        cir.setReturnValue(TextVisitFactory.validateSurrogates(text));
    }

    @Inject(method = "setClipboard", at = @At("HEAD"), cancellable = true)
    public void setSystemClipboard(String string, CallbackInfo ci) {
        ci.cancel();
        Clipboard cb = ClipboardHolder.getClipboard();
        try {
            cb.setContents(new StringSelection(string), null);
        } catch (IllegalStateException e) {
            LOGGER.error("Failed to set clipboard contents", e);
        }
    }
}
