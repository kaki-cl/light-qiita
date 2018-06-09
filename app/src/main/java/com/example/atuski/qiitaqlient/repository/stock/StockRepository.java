package com.example.atuski.qiitaqlient.repository.stock;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.atuski.qiitaqlient.R;
import com.example.atuski.qiitaqlient.model.Followee;
import com.example.atuski.qiitaqlient.model.Stock;
import com.example.atuski.qiitaqlient.api.QiitaClient;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;

/**
 * Created by atuski on 2018/04/24.
 */

public class StockRepository {

    private static StockRepository sInstance;

    private QiitaClient qiitaClient;

    private Context context;

    private StockRepository(Context context) {

        this.context = context;
        qiitaClient = QiitaClient.getInstance();
    }

    public static StockRepository getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new StockRepository(context);
        }
        return sInstance;
    }

    public Observable<List<Stock>> searchStockItems(String userId) {

        return qiitaClient.qiitaService.getStockItems(userId, 1, 20)
                .map((stockList) -> {

//                    for (Article r : articleSearchResult) {
//                        r.setQueryId(queryId);
//                    }
                    // 検索結果を保存
//                    localDataSource.insertArticles(articleSearchResult);
                    return stockList;
                });
    }

    public Completable stockArticle(String articleId) {

        SharedPreferences data = context.getSharedPreferences(context.getResources().getString(R.string.USER_INFO), Context.MODE_PRIVATE);
        String authHeaderValue = data.getString(context.getResources().getString(R.string.AUTHORIZATION_HEADER_VALUE), null);
        if (authHeaderValue == null) {
            Log.v("stockArticle", "authHeader null");
            //todo Completableを返す
//            BehaviorSubject<Void> behaviorSubject = BehaviorSubject.create();
//            return behaviorSubject;
        } else {
            Log.v("stockArticle", authHeaderValue);
        }

        return qiitaClient.qiitaService
                .stockArticle(articleId, authHeaderValue)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
