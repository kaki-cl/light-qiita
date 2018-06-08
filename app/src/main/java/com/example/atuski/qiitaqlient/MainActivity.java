package com.example.atuski.qiitaqlient;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.atuski.qiitaqlient.api.SyncronizePostUserQlient;
import com.example.atuski.qiitaqlient.model.Followee;
import com.example.atuski.qiitaqlient.model.SyncronizePostUserResult;
import com.example.atuski.qiitaqlient.model.UserInfo;
import com.example.atuski.qiitaqlient.repository.followee.FolloweeRepository;
import com.example.atuski.qiitaqlient.ui.searchhistory.SearchHistoryFragment;
import com.example.atuski.qiitaqlient.ui.toolbar.ToolbarFragment;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;

public class MainActivity extends AppCompatActivity {

    final BehaviorSubject<String> loginStatus = BehaviorSubject.createDefault("init");

    private UserInfo loginUserInfo;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v("MainActivityonCreate", "onCreate");

        String token = FirebaseInstanceId.getInstance().getToken();
        Log.v("MainActivity", "FirebaseInstanceId");
        if (token != null) {
            Log.v("MainActivity", token);
        } else {
            Log.v("MainActivity", "null");
        }
//        FirebaseMessaging.getInstance().subscribeToTopic("mytopic");


        setContentView(R.layout.main_activity);

        if (savedInstanceState == null) {
            loadUserAccount();
        } else {
            loadLocalUserInfo();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.v("MainActivityonCreate", "onCreateOptionsMenu");
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main, menu);

        if (loginUserInfo.isLogin) {
            menu.findItem(R.id.login).setVisible(false);

        } else {
            menu.findItem(R.id.logout).setVisible(false);
            menu.findItem(R.id.syncronizePostUser).setVisible(false);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.search_history:
                SearchHistoryFragment searchHistoryFragment = new SearchHistoryFragment();
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, searchHistoryFragment)
                        .addToBackStack(null)
                        .commit();
                break;

            case R.id.login:
                String mClientId = "dfd44c0b8c380894cac1ea43ff4b815a2661e461";
                String mScope = "read_qiita write_qiita";
                String mState = "bb17785d811bb1913ef54b0a7657de780defaa2d";//todo to be random
                String uri = "https://qiita.com/api/v2/oauth/authorize?" +
                        "client_id=" + mClientId +
                        "&scope=" + mScope +
                        "&state=" + mState;
                Intent browseIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                startActivity(browseIntent);
                break;

            case R.id.logout:
                deleteLocalUserInfo();
                Log.v("deleteLocalUserInfo", "deleteLocalUserInfo");
                Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(mainIntent);
                break;

            case R.id.syncronizePostUser:

                /**
                 * todo
                 * リファクタ
                 * .subscribe()メソッドはもとシンプルに行けなかったっけ？
                 * 関数インターフェイスとは？
                 */
                FolloweeRepository.getInstance()
                        .searchFollowees(loginUserInfo.id)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Observer<List<Followee>>() {
                            @Override
                            public void onSubscribe(Disposable d) {

                            }

                            @Override
                            public void onNext(List<Followee> followees) {

                                for (Followee followee : followees) {
                                    requestSyncronize(followee.id);
                                    Log.v("searchFollowees", "ここまできてる？");
                                    Log.v("searchFollowees", followee.id);
                                }

                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.v("searchFollowees Error", "Error");
                                e.printStackTrace();
                            }

                            @Override
                            public void onComplete() {

                            }
                        });

                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //todo リファクタ
    // 別クラスへ
    private void requestSyncronize(String postUserId) {

        Log.v("requestSyncronize","ここまできてる？");

        HashMap<String, String> postParamters = new HashMap<>();
        postParamters.put("qlientUserId", loginUserInfo.id);
        postParamters.put("postUserId", postUserId);

        SyncronizePostUserQlient.getInstance()
                .requestSyncronizing(postParamters)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<SyncronizePostUserResult>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(SyncronizePostUserResult syncronizePostUserResult) {
                        //todo OKだったら同期しました画面をだす。
                        Log.v("requestSyncronize onNext", "ここまできてる？");
//                        Log.v("SyncronizePostUserQlient test", syncronizePostUserResult.toString());
                        Log.v("syncronizePostUserResult", syncronizePostUserResult.result);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.v("requestSyncronize Error", "Error");
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }


    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @SuppressLint("CheckResult")
    private void loadUserAccount() {

        loginStatus.subscribe((loginStatus) -> {
            switch (loginStatus) {
                case "guestUser":
                    Log.v("loadUserAccount", "guestUser");
                    initViewPagerFragment(false);
                    break;
                case "loginUser":
                    Log.v("loadUserAccount", "loginUser");
                    initViewPagerFragment(true);
                    break;
                default:
                    break;
            }
        });

        // 認証ページから戻ってきたときの処理
        Uri uri = getIntent().getData();
        if (uri != null) {
            fetchUserInfo(uri);
            return;
        }

        if (loadLocalUserInfo()) {
            loginStatus.onNext("loginUser");
        } else {
            loginStatus.onNext("guestUser");
        }
    }

    private void initViewPagerFragment(Boolean isLogin) {

        Bundle bundle = new Bundle();
        bundle.putBoolean(getResources().getString(R.string.IS_LOGIN), isLogin);

        if (isLogin) {
            bundle.putString(getResources().getString(R.string.USER_ID), loginUserInfo.id);
//            bundle.putString(getResources().getString(R.string.USER_NAME), loginUserInfo.name);
            bundle.putString(getResources().getString(R.string.PROFILE_IMAGE_URL), loginUserInfo.profile_image_url);
        }

        String lastQuery = getIntent().getStringExtra(getResources().getString(R.string.LAST_QUERY));
        if (lastQuery != null) {
            bundle.putString(getResources().getString(R.string.LAST_QUERY), lastQuery);
        }

        ToolbarFragment toolbarFragment = new ToolbarFragment();
        toolbarFragment.setArguments(bundle);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, toolbarFragment)
                .addToBackStack(null)
                .commit();
    }

    @SuppressLint("CheckResult")
    private void fetchUserInfo(Uri uri) {

        Log.v("ログイン判定", "ログインチャレンジ");
        // ログインチャレンジ
        QiitaQlientApp
                .getInstance()
                .getSearchRepository()
                .fetchAccessToken(uri.getQueryParameter("code").toString())
                .subscribeOn(Schedulers.io())
                .subscribe((token -> {
                    Log.v("accessToken", token.getToken());
                    QiitaQlientApp.getInstance().getSearchRepository()
                            .fetchUserInfo(token.getToken())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe((userInfo -> {

                                if (userInfo != null) {
                                    saveUserInfo(userInfo);
                                }

                                if (loadLocalUserInfo()) {
                                    loginStatus.onNext("loginUser");
                                } else {
                                    loginStatus.onNext("guestUser");
                                }
                            }));
                }));
    }

    private boolean loadLocalUserInfo() {

        Log.v("loadLocalUserInfo", "loadLocalUserInfo");
        SharedPreferences data = getSharedPreferences(getResources().getString(R.string.USER_INFO), Context.MODE_PRIVATE);
        boolean isLogin = data.getBoolean(getResources().getString(R.string.IS_LOGIN), false);

        if (!isLogin) {
            Log.v("isLogin", "false");

            UserInfo userInfo = new UserInfo();
            userInfo.isLogin = false;
            loginUserInfo = userInfo;
            return false;
        }

        UserInfo userInfo = new UserInfo();
        userInfo.isLogin = isLogin;
        userInfo.id = data.getString(getResources().getString(R.string.USER_ID), null);
        userInfo.profile_image_url = data.getString(getResources().getString(R.string.PROFILE_IMAGE_URL), null);
        loginUserInfo = userInfo;
        return true;
    }


    private void saveUserInfo(UserInfo latestUserInfo) {

        SharedPreferences data = getSharedPreferences(getResources().getString(R.string.USER_INFO), getApplicationContext().MODE_PRIVATE);
        SharedPreferences.Editor editor = data.edit();
        editor.putBoolean(getResources().getString(R.string.IS_LOGIN), true);
        editor.putString(getResources().getString(R.string.USER_ID), latestUserInfo.id);
        editor.putString(getResources().getString(R.string.PROFILE_IMAGE_URL), latestUserInfo.profile_image_url);
        editor.apply();
    }

    private void deleteLocalUserInfo() {
        deleteSharedPreferences(getResources().getString(R.string.USER_INFO));
    }
}