package common;

/**
 * This common.User class only has the username field in this example.
 * You can add more attributes such as the user's shopping cart items.
 */
public class User {

    private final String username;
    private final Integer id;

    public User(String id, String username) {
        this.username = username;
        this.id = Integer.parseInt(id);
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

}
