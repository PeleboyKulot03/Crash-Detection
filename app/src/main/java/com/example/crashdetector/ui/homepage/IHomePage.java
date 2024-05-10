package com.example.crashdetector.ui.homepage;

import com.example.crashdetector.utils.HomePageModel;

public interface IHomePage {
    void onGetUserData(HomePageModel model);

    void onGetTime(int time);
}
