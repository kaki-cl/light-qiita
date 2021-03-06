package com.example.atuski.qiitaqlient;

import android.app.Application;

import com.example.atuski.qiitaqlient.repository.QiitaBrowseRepository;

public class QiitaQlientApp extends Application {

    private static QiitaQlientApp app;

    public static QiitaQlientApp getInstance() {
        return app;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        this.app = this;
    }

    public QiitaBrowseRepository getRepository() {
        return QiitaBrowseRepository.getInstance(getApplicationContext());
    }
}
