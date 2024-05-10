package com.example.crashdetector.ui.forgotpassword;

public interface IForgotPassword {
    void hasUser(boolean verdict);
    void isSend(boolean verdict, String errorMessage);
}
