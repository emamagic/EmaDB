package com.emamagic;

import com.emamagic.entity.User;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        // Insert or update a user record
        User user = EmaDB.upsert(getUser());
        System.out.println(user);

        // Delete records
        boolean isDeleted = EmaDB.delete(user);
        System.out.println(isDeleted);

        // Read records
        List<User> users = EmaDB.read(User.class);
        System.out.println(users);

        // Close the database connection
        EmaDB.close();
    }

    private static User getUser() {
        var user = new User();
        user.setName("ali");
        user.setEmail("ali@gmail.com");
        user.setAge(22);
        return user;
    }

}