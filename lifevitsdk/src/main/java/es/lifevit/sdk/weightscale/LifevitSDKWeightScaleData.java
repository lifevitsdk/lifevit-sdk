package es.lifevit.sdk.weightscale;

public class LifevitSDKWeightScaleData {


    private double weight;
    private String unit;
    private double imc;
    private double fatRawValue;
    private double fatPercentage;
    private double waterRawValue;
    private double waterPercentage;
    private double muscleRawValue;
    private double musclePercentage;
    private double visceralRawValue;
    private double visceralPercentage;
    private double boneRawValue;
    private double bonePercentage;
    private double bmr;
   private double proteinPercentage;
   private double idealWeight;
   private double obesityPercentage;
    private double bodyAge;
    private Double bia = null;

    public Double getBia() {
        return bia;
    }

    public void setBia(Double bia) {
        this.bia = bia;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public double getImc() {
        return imc;
    }

    public void setImc(double imc) {
        this.imc = imc;
    }

    public double getFatRawValue() {
        return fatRawValue;
    }

    public void setFatRawValue(double fatRawValue) {
        this.fatRawValue = fatRawValue;
    }

    public double getFatPercentage() {
        return fatPercentage;
    }

    public void setFatPercentage(double fatPercentage) {
        this.fatPercentage = fatPercentage;
    }

    public void setWaterRawValue(double waterRawValue) {
        this.waterRawValue = waterRawValue;
    }

    public double getWaterRawValue() {
        return waterRawValue;
    }

    public double getWaterPercentage() {
        return waterPercentage;
    }

    public void setWaterPercentage(double waterPercentage) {
        this.waterPercentage = waterPercentage;
    }

    public double getMuscleRawValue() {
        return muscleRawValue;
    }

    public void setMuscleRawValue(double muscleRawValue) {
        this.muscleRawValue = muscleRawValue;
    }

    public double getMusclePercentage() {
        return musclePercentage;
    }

    public void setMusclePercentage(double musclePercentage) {
        this.musclePercentage = musclePercentage;
    }

    public double getVisceralRawValue() {
        return visceralRawValue;
    }

    public void setVisceralRawValue(double visceralRawValue) {
        this.visceralRawValue = visceralRawValue;
    }

    public double getVisceralPercentage() {
        return visceralPercentage;
    }

    public void setVisceralPercentage(double visceralPercentage) {
        this.visceralPercentage = visceralPercentage;
    }

    public double getBoneRawValue() {
        return boneRawValue;
    }

    public void setBoneRawValue(double boneRawValue) {
        this.boneRawValue = boneRawValue;
    }

    public double getBonePercentage() {
        return bonePercentage;
    }

    public void setBonePercentage(double bonePercentage) {
        this.bonePercentage = bonePercentage;
    }

    public double getBmr() {
        return bmr;
    }

    public void setBmr(double bmr) {
        this.bmr = bmr;
    }

    public double getProteinPercentage() {
        return proteinPercentage;
    }

    public void setProteinPercentage(double protein) {
        this.proteinPercentage = protein;
    }

    public double getIdealWeight() {
        return idealWeight;
    }

    public void setIdealWeight(double idealWeight) {
        this.idealWeight = idealWeight;
    }

    public double getObesityPercentage() {
        return obesityPercentage;
    }

    public void setObesityPercentage(double obesityPercentage) {
        this.obesityPercentage = obesityPercentage;
    }

    public double getBodyAge() {
        return bodyAge;
    }

    public void setBodyAge(double bodyAge) {
        this.bodyAge = bodyAge;
    }
}
