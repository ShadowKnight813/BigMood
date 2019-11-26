package edu.ualberta.cmput301f19t17.bigmood.database;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit test for User
 *
 * **/
class UserTest {
    private static final String UserName = "Username";
    private static final String firstName = "firstname";
    private static final String lastName = "lastname";

    private User mockUser;

    private User mockUser(){

       return new User (UserName,firstName,lastName);

    }

    @Test
    void getUsername() throws IllegalAccessException, NoSuchFieldException {
        User user = new User("username","firstname","lastname");
        assertEquals(user.getUsername(), "username");
    }

    @Test
    void getFirstName() throws NoSuchFieldException, IllegalAccessException {
        User user = new User("username","firstname","lastname");
        assertEquals(user.getFirstName(), "firstname");
    }

    @Test
    void getLastName() throws NoSuchFieldException, IllegalAccessException {
        User user = new User("username","firstname","lastname");
        assertEquals(user.getLastName(), "lastname");
    }

}