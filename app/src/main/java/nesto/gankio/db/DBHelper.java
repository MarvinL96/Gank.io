package nesto.gankio.db;

import android.content.ContentValues;
import android.database.Cursor;

import com.google.gson.Gson;
import com.squareup.sqlbrite.BriteDatabase;
import com.squareup.sqlbrite.SqlBrite;

import java.util.ArrayList;

import nesto.gankio.global.C;
import nesto.gankio.model.Data;
import nesto.gankio.network.HttpMethods;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created on 2016/5/14.
 * By nesto
 */
public class DBHelper {
    private Gson gson;
    private BriteDatabase db;
    private ArrayList<Data> favouriteList;
    private int favouriteNum;

    private DBHelper() {
        gson = new Gson();
        favouriteList = new ArrayList<>();
        db = SqlBrite.create().wrapDatabaseHelper(new SQLiteHelper(), Schedulers.io());
    }

    private static class SingletonHolder {
        private static final DBHelper INSTANCE = new DBHelper();
    }

    public static DBHelper getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public Observable<Object> add(final Data data) {
        return Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                favouriteList.add(data);
                BriteDatabase.Transaction transaction = db.newTransaction();
                try {
                    db.insert(C.FAVOURITE_TABLE, makeData(1, data));
                    transaction.markSuccessful();
                } finally {
                    transaction.end();
                    subscriber.onCompleted();
                }
            }
        }).compose(HttpMethods.getInstance().setThreads());
    }

    public Observable<Object> remove(final Data data) {
        return Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                favouriteList.remove(data);
                BriteDatabase.Transaction transaction = db.newTransaction();
                try {
                    //TODO order
                    db.delete(C.FAVOURITE_TABLE, C.ID + " = '" + data.get_id() + "'");
                    transaction.markSuccessful();
                } finally {
                    transaction.end();
                    subscriber.onCompleted();
                }
            }
        }).compose(HttpMethods.getInstance().setThreads());
    }

    public void move(int from, int to) {
        //TODO
    }

    public boolean isExist(Data data) {
        return favouriteList.contains(data);
    }

    public Observable<ArrayList<Data>> getAll() {
        favouriteList.clear();
        return db.createQuery(C.FAVOURITE_TABLE, "SELECT * FROM " + C.FAVOURITE_TABLE +
                " ORDER BY " + C.ORDER)
                .map(new Func1<SqlBrite.Query, ArrayList<Data>>() {
                    @Override
                    public ArrayList<Data> call(SqlBrite.Query query) {
                        Cursor cursor = query.run();
                        if (cursor == null || !cursor.moveToFirst()) {
                            throw new DBException("no result");
                        }
                        favouriteNum = cursor.getCount();
                        do {
                            int name = cursor.getColumnIndex(C.VALUE);
                            favouriteList.add(toData(cursor.getString(name)));
                        } while (cursor.moveToNext());
                        return favouriteList;
                    }
                });
    }

    private ContentValues makeData(int order, Data data) {
        ContentValues values = new ContentValues();
        values.put(C.ID, data.get_id());
        values.put(C.VALUE, toJson(data));
//        values.put(C.ID, AppUtil.getCurrentTime() + order);
//        values.put(C.VALUE, AppUtil.getCurrentTime());
        values.put(C.ORDER, order);
        return values;
    }

    private String toJson(Data data) {
        return gson.toJson(data);
    }

    private Data toData(String json) {
        return gson.fromJson(json, Data.class);
    }

    public ArrayList<Data> getFavouriteList() {
        return favouriteList;
    }
}