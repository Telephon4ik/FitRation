package com.fitration.models;

import java.util.Date;

public class Meal {
    private String id;
    private String userId;
    private Date date;
    private String mealType;
    private String name;
    private String description;
    private String imageUrl;
    private Nutrition nutrition;
    private boolean custom = false;
    private String spoonacularId;
    private String coachComment;
    private Boolean eaten;
    private Date eatenAt;

    public Meal() {}

    public Meal(String userId, Date date, String mealType) {
        this.userId = userId;
        this.date = date;
        this.mealType = mealType;
    }

    // Геттеры и сеттеры
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public String getMealType() { return mealType; }
    public void setMealType(String mealType) { this.mealType = mealType; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Nutrition getNutrition() { return nutrition; }
    public void setNutrition(Nutrition nutrition) { this.nutrition = nutrition; }
    public boolean isCustom() {
        return custom;
    }

    public void setCustom(boolean custom) {
        this.custom = custom;
    }
    public boolean isCustomMeal() {
        return custom;
    }
    public String getSpoonacularId() { return spoonacularId; }
    public void setSpoonacularId(String spoonacularId) { this.spoonacularId = spoonacularId; }

    public String getCoachComment() { return coachComment; }
    public void setCoachComment(String coachComment) { this.coachComment = coachComment; }
    public Boolean getEaten() {
        return eaten;
    }

    public void setEaten(Boolean eaten) {
        this.eaten = eaten;
        if (eaten != null && eaten) {
            this.eatenAt = new Date();
        } else {
            this.eatenAt = null;
        }
    }

    // Вспомогательный метод (не будет в Firestore)
    public boolean isMealEaten() {
        return eaten != null && eaten;
    }

    public Date getEatenAt() { return eatenAt; }
    public void setEatenAt(Date eatenAt) { this.eatenAt = eatenAt; }
}