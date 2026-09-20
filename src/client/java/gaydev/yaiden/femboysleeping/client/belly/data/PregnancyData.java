package gaydev.yaiden.femboysleeping.client.belly.data;

public class PregnancyData {

    private static boolean pregnant = false;
    private static double stage = 0;
    private static float bellyGrowth = 0.0F;

    public static boolean isPregnant() {
        return pregnant;
    }

    public static double getStage() {
        return stage;
    }

    public static float getBellyGrowth() {
        return bellyGrowth;
    }

    public static void setPregnant(boolean pregnant) {
        PregnancyData.pregnant = pregnant;
    }

    public static void setStage(double stage) {
        PregnancyData.stage = stage;
    }

    public static void setBellyGrowth() {
        PregnancyData.bellyGrowth = Math.min(bellyGrowth, 4.8F);
    }
}