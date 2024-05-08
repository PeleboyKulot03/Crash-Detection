package com.example.crashdetector.ui.registration;

public interface IRegistrationPage {
    void hasUser(boolean verdict);

    void onCreateNewUser(boolean verdict, String localizedMessage);
}
