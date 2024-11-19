package entity;

import java.util.ArrayList;

/**
 * Factory for creating CommonUser objects.
 */
public class AnonymousUserFactory implements UserFactory {

    @Override
    public User create(String name, String password, String userID, String birthDate, String fullName, 
                       String email, ArrayList<String> moderating, ArrayList<String> posts) {
        return new CommonUser(name, password, userID, birthDate, fullName, email, moderating, posts);
    }
}
