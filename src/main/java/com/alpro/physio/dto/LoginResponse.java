package com.alpro.physio.dto;

public class LoginResponse {
    private String token;
    private String userId;
    private String fullname;
    private String email;

    public LoginResponse(String token, String userId, String fullname, String email) {
        this.token = token;
        this.userId = userId;
        this.fullname = fullname;
        this.email = email;
    }

    // getters and setters
    public String getToken() { 
        return token; 
    }

    public void setToken(String token) { 
        this.token = token; 
    }
    
    public String getUserId() { 
        return userId; 
    }
    
    public void setUserId(String userId) { 
        this.userId = userId; 
    }
    
    public String getFullname() { 
        return fullname; 
    }

    public void setFullname(String fullname) { 
        this.fullname = fullname; 
    }

    public String getEmail() { 
        return email; 
    }

    public void setEmail(String email) { 
        this.email = email; 
    }
}