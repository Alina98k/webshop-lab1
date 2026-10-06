package com.example.webshop.ui;

/** Överföringsobjekt för användarinformation, utan lösenord. */
public final class UserInfo {
    private final Long id;
    private final String username;
    private final String fullName;

    public UserInfo(Long id, String username, String fullName) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
}
