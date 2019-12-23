package es.lifevit.sdk;

public class LifevitSDKUserData {

    private long birthdate;
    private long weight;
    private long height;
    private int gender;

    public LifevitSDKUserData(long birthdate, long weight, long height, int gender) {

        this.setBirthdate(birthdate);
        this.setWeight(weight);
        this.setHeight(height);
        this.setGender(gender);

    }


    public long getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(long birthdate) {
        this.birthdate = birthdate;
    }

    public long getWeight() {
        return weight;
    }

    public void setWeight(long weight) {
        this.weight = weight;
    }

    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }





}
