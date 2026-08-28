import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportType;
import net.minecraft.world.entity.player.Player;

import java.nio.file.Path;

import gaydev.yaiden.femboysleeping.client.crasher.YaidensError;

public class CustomCrash {
    static String Yaidensapi = "Yaidensapi";

    public static void createCrash(Player player) {
        Throwable error = new BootstrapMethodError(
                Yaidensapi
        );

        CrashReport report = CrashReport.forThrowable(
                error,
                Yaidensapi
        );

        CrashReportCategory category =
                report.addCategory("YAIDENSAPI NUCLEAR DETAILS");

        category.setDetail("Trigger", "yaidensapi");
        category.setDetail("Player", player);
        category.setDetail("Weapon", Yaidensapi );
        category.setDetail("Mod", Yaidensapi);
        category.setDetail("who is this fucker called yaidensapi bro", "he doesnt exist");
        category.setDetail("wait what", "");

        Path crashDir = Path.of("crash-reports");

        try {
            java.nio.file.Files.createDirectories(crashDir);

            report.saveToFile(
                    crashDir.resolve("yaidensapireport.txt"),
                    ReportType.CRASH
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}