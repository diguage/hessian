package com.diguage;

/**
 * @author D瓜哥 · https://www.diguage.com/
 */
public class Car {
    private String name;
    private int age;

    public Car() {
    }

    public Car(String name, int age) {
        this.name = name;
        this.age = age;
    }

    @Override
    public String toString() {
        return "Car{" +
                "name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}
