package nesto.gankio.ui.fragment.normal;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import nesto.gankio.R;
import nesto.gankio.db.DBHelper;
import nesto.gankio.global.C;
import nesto.gankio.global.Intents;
import nesto.gankio.model.Data;
import nesto.gankio.model.DataType;
import nesto.gankio.ui.activity.content.ContentActivity;
import nesto.gankio.util.AppUtil;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created on 2016/5/9.
 * By nesto
 */
public class NormalAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    protected Context context;

    protected ArrayList<Data> list;
    protected boolean hasMore = true;

    public NormalAdapter(Context context) {
        this.context = context;
        this.list = new ArrayList<>();
    }

    public NormalAdapter(Context context, ArrayList<Data> list) {
        this.context = context;
        this.list = list;
    }

    public void setList(ArrayList<Data> list, boolean hasMore) {
        this.list = list;
        this.hasMore = hasMore;
    }

    public ArrayList<Data> getList() {
        return list;
    }

    public void add(ArrayList<Data> list) {
        this.list.addAll(list);
        hasMore = (list.size() == C.LOAD_NUM);
        notifyDataSetChanged();
    }

    public void clearData() {
        list.clear();
        this.hasMore = false;
    }

    public boolean isHasMore() {
        return hasMore;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_card, parent, false);
        return new NormalViewHolder(convertView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        initItemView(position, (NormalViewHolder) viewHolder);
    }

    protected void initItemView(final int position, final NormalViewHolder viewHolder) {
        final Data data = list.get(position);
        viewHolder.title.setVisibility(View.GONE);
        viewHolder.text.setVisibility(View.GONE);
        viewHolder.image.setVisibility(View.GONE);
        data.setFavoured(DBHelper.getInstance().isExist(data));
        setFavourite(data, viewHolder);
        if (data.type().equals(DataType.BENEFIT.toString())) {
            viewHolder.image.setVisibility(View.VISIBLE);
            Picasso.with(context)
                    .load(data.url())
                    .into(viewHolder.image);
        } else {
            viewHolder.title.setVisibility(View.VISIBLE);
            viewHolder.text.setVisibility(View.VISIBLE);
            viewHolder.title.setText(data.desc());
            viewHolder.text.setText(data.who());
            viewHolder.item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClicked(data);
                }
            });
        }
        viewHolder.share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppUtil.onShareClicked(data, context);
            }
        });
        viewHolder.favourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFavouriteClicked(data, viewHolder, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private void onItemClicked(Data data) {
        Intent intent = data.type().equals(DataType.VIDEO.toString()) ?
                new Intent(context, ContentActivity.class).putExtra(Intents.TRANS_DATA, data) :
                //TODO video player
//                new Intent(context, VideoActivity.class).putExtra(Intents.TRANS_DATA, data) :
                new Intent(context, ContentActivity.class).putExtra(Intents.TRANS_DATA, data);
        context.startActivity(intent);
    }

    protected void onFavouriteClicked(Data data, NormalViewHolder viewHolder, int position) {
        if (data.isFavoured()) {
            removeFromFavourite(data, viewHolder, position);
            data.setFavoured(false);
        } else {
            addToFavourite(data, viewHolder, position);
            data.setFavoured(true);
        }
        setFavourite(data, viewHolder);
    }

    protected void addToFavourite(final Data data, final NormalViewHolder viewHolder, int position) {
        DBHelper.getInstance()
                .add(data)
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        data.setFavoured(false);
                        setFavourite(data, viewHolder);
                    }
                })
                .onErrorReturn(new Func1<Throwable, Object>() {
                    @Override
                    public Object call(Throwable throwable) {
                        return null;
                    }
                })
                .subscribe();
    }

    protected void removeFromFavourite(final Data data, final NormalViewHolder viewHolder, int position) {
        DBHelper.getInstance()
                .remove(data)
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        data.setFavoured(false);
                        setFavourite(data, viewHolder);
                    }
                })
                .onErrorReturn(new Func1<Throwable, Object>() {
                    @Override
                    public Object call(Throwable throwable) {
                        return null;
                    }
                })
                .subscribe();
    }

    protected void setFavourite(Data data, NormalViewHolder viewHolder) {
        if (data.isFavoured()) {
            viewHolder.favourite.setImageResource(R.drawable.ic_action_favourited);
        } else {
            viewHolder.favourite.setImageResource(R.drawable.ic_action_favourite);
        }
    }
}
