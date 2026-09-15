package gaydev.yaiden.femboysleeping.belly.data;

public class PregnancyData {

    private boolean pregnant = false;
    private int stage = 0;
    private float bellyGrowth = 0.0F;

    public boolean isPregnant() {
        return pregnant;
    }

    public int getStage() {
        return stage;
    }

    public float getBellyGrowth() {
        return bellyGrowth;
    }

    public void setPregnant(boolean pregnant) {
        this.pregnant = pregnant;
    }

    public void setStage(int stage) {
        this.stage = stage;
    }

    public void setBellyGrowth(float bellyGrowth) {
        this.bellyGrowth = Math.min(bellyGrowth, 4.8F);
    }
}