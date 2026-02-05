package com.fitration.models;

import java.util.Date;

public class CoachRequest {
    private String id;
    private String coachId;       // publicId тренера
    private String userId;        // UID пользователя
    private String userName;      // Имя пользователя
    private String userEmail;     // Email пользователя
    private String status;        // PENDING, ACCEPTED, REJECTED
    private Date createdAt;
    private String message;
    private String type;          // Для обратной совместимости
    private String coachName;     // Для обратной совместимости

    public CoachRequest() {}

    public CoachRequest(String coachId, String userId, String userName,
                        String userEmail, String message) {
        this.coachId = coachId;
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.message = message;
        this.status = "PENDING";
        this.createdAt = new Date();
    }

    // Геттеры и сеттеры
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCoachId() { return coachId; }
    public void setCoachId(String coachId) { this.coachId = coachId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getStatus() { return status != null ? status : "PENDING"; }
    public void setStatus(String status) { this.status = status; }

    public Date getCreatedAt() { return createdAt != null ? createdAt : new Date(); }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public String getMessage() { return message != null ? message : ""; }
    public void setMessage(String message) { this.message = message; }

    // Для обратной совместимости
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getCoachName() {
        return coachName != null ? coachName : "Тренер " + coachId;
    }
    public void setCoachName(String coachName) { this.coachName = coachName; }
}