package net.jetluna.auth;

public class AuthSession {
    public enum State {
        NONE,
        AWAITING_EMAIL,
        AWAITING_CODE
    }

    private State state = State.NONE;
    private String tempContact;

    public State getState() { return state; }
    public void setState(State state) { this.state = state; }

    public String getTempContact() { return tempContact; }
    public void setTempContact(String contact) { this.tempContact = contact; }
}