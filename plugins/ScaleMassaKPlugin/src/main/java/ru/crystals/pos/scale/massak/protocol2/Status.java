package ru.crystals.pos.scale.massak.protocol2;

public class Status {

    private static final int LOW_WEIGHT_STATUS_BIT = 64;
    private static final int TARE_STATUS_BIT = 32;
    private static final int STABILITY_STATUS_BIT = 128;

    private boolean lowWeight = true;
    private boolean tareOnScale = false;
    private boolean weightStable = false;
    private Measure measure = Measure.GRAM;


    public Status(byte[] statusAnswer){
        if (statusAnswer != null && statusAnswer.length == 2) {
            this.lowWeight = ((statusAnswer[0] & LOW_WEIGHT_STATUS_BIT) == LOW_WEIGHT_STATUS_BIT);
            this.tareOnScale = ((statusAnswer[0] & TARE_STATUS_BIT) == TARE_STATUS_BIT);
            this.weightStable = ((statusAnswer[0] & STABILITY_STATUS_BIT) == STABILITY_STATUS_BIT);
            if(statusAnswer[1] == 0x01){
                this.measure = Measure.TENTH_OF_GRAM;
            }
        }
    }

    public boolean isLowWeight() {
        return lowWeight;
    }

    public boolean isTareOnScale() {
        return tareOnScale;
    }

    public boolean isWeightStable() {
        return weightStable;
    }

    public Measure getMeasure() {
        return measure;
    }
}
