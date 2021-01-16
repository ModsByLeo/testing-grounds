package adudecalledleo.globalclipboard;

import org.jetbrains.annotations.NotNull;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;

public final class ClipboardHolder {
    private ClipboardHolder() { }

    private static Clipboard clipboard;
    private static DataFlavor bestTextFlavor;

    public static void initialize() {
        clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        if (clipboard == null)
            throw new RuntimeException("Seems like this mod can't run on your platform. Sorry! Please uninstall the mod.");
        bestTextFlavor = DataFlavor.selectBestTextFlavor(clipboard.getAvailableDataFlavors());
        if (bestTextFlavor == null)
            throw new RuntimeException("Apparently we couldn't find a data flavor for text? This should never show up.");
    }

    public static @NotNull Clipboard getClipboard() {
        return clipboard;
    }

    public static @NotNull DataFlavor getBestTextFlavor() {
        return bestTextFlavor;
    }
}
