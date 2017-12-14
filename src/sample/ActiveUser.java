package sample;

public class ActiveUser {
    private static ActiveUser user = null;
    private String username;
    private int ID;

    private ActiveUser() {

    }

    public static ActiveUser getInstance() {
        if(user==null){
            user = new ActiveUser();
        }
        return user;
    }

    public String getUsername() {
        return username;
    }

    public int getID() {
        return ID;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setID(int ID) {
        this.ID = ID;
    }
}
