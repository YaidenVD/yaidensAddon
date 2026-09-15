package gaydev.yaiden.femboysleeping.belly.data;

public class calculator {
    public float calculateBellyGrowth(float pregnancyProgress) {

    float growth;

    if (pregnancyProgress < 4.0F) {
        growth = (float) Math.floor(pregnancyProgress / 0.25F) * 0.25F;
    } else {
        growth = 4.0F + (float) Math.floor((pregnancyProgress - 4.0F) / 0.2F) * 0.2F;
    }

    return Math.min(growth, 4.8F);
}
}
