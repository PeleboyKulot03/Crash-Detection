package com.example.crashdetector.ui.homepage;

import com.example.crashdetector.utils.HomePageModel;

import java.util.HashMap;

public interface IHomePage {
    void onGetUserData(HomePageModel model);

    void onGetTime(int time);
    void onGetModels(HashMap<String, String> models);
}
