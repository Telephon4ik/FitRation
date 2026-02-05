package com.fitration.models;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

public class User implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String uid;
    private String email;
    private String name;
    private String role; // "USER" или "COACH"
    private String publicId; // FR-XXXXXX
    private String coachId; // для USER
    private Date createdAt;

    private boolean acceptCoachRequests = true;
    // Профиль пользователя
    private String gender;
    private int age;
    private double weight;
    private double height;
    private String activityLevel;
    private String goal; // LOSE, MAINTAIN, GAIN
    private int dailyCalories;
    private Nutrition nutritionGoals;

    public User() {}

    public User(String uid, String email, String name, String role) {
        this.uid = uid;
        this.email = email;
        this.name = name;
        this.role = role;
        this.createdAt = new Date();
    }

    // Геттеры и сеттеры
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public boolean isAcceptCoachRequests() { return acceptCoachRequests; }
    public void setAcceptCoachRequests(boolean acceptCoachRequests) {
        this.acceptCoachRequests = acceptCoachRequests;
    }
    public String getPublicId() { return publicId; }
    public void setPublicId(String publicId) { this.publicId = publicId; }

    public String getCoachId() { return coachId; }
    public void setCoachId(String coachId) { this.coachId = coachId; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }

    public String getActivityLevel() { return activityLevel; }
    public void setActivityLevel(String activityLevel) { this.activityLevel = activityLevel; }

    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }

    public int getDailyCalories() { return dailyCalories; }
    public void setDailyCalories(int dailyCalories) { this.dailyCalories = dailyCalories; }

    public Nutrition getNutritionGoals() { return nutritionGoals; }
    public void setNutritionGoals(Nutrition nutritionGoals) { this.nutritionGoals = nutritionGoals; }

    public boolean isCoach() {
        return "COACH".equals(role);
    }
}