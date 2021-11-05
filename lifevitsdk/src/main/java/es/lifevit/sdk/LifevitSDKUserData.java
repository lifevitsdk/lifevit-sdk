package es.lifevit.sdk;

import es.lifevit.sdk.utils.Utils;

public class LifevitSDKUserData {

    private Long birthdate;
    private long weight;
    private long height;
    private int gender;
    private Integer age;

    public LifevitSDKUserData(long birthdate, long weight, long height, int gender) {

        this.setBirthdate(birthdate);
        this.setWeight(weight);
        this.setHeight(height);
        this.setGender(gender);

    }

    public LifevitSDKUserData(int age, long weight, long height, int gender) {

        this.setAge(age);
        this.setWeight(weight);
        this.setHeight(height);
        this.setGender(gender);

    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Integer getAge() {

        if (age == null && birthdate != null) {
            return Utils.getAge(birthdate);
        }

        return age;
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


    @Override
    public String toString() {
        return "LifevitSDKUserData{" +
                "birthdate=" + birthdate +
                ", weight=" + weight +
                ", height=" + height +
                ", gender=" + gender +
                ", age=" + age +
                '}';
    }
}
