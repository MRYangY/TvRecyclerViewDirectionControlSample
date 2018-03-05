package com.example.tvrecyclerviewdirectioncontrolsample;

import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private ImageView mTopIndicate;
    private ImageView mBottomIndicate;
    private RecyclerView mRcContent;
    private View mCurrentView;

    private MyAdaptor myAdaptor;
    private GridLayoutManager manager;
    private ArrayList<String> mList = new ArrayList<>();

    private Handler mHandler = new Handler(Looper.getMainLooper());

    private int mCurrentIndex = 0;
    private final int COUNT_PER_ROW = 5;
    private boolean isDragging = false;
    private boolean isScrollToBottom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBottomIndicate = findViewById(R.id.dir_bottom);
        mTopIndicate = findViewById(R.id.dir_top);
        mRcContent = findViewById(R.id.rv_content);
        for (int i = 0; i < 40; i++) {
            mList.add(String.valueOf(i));
        }

        // TODO: 2018/3/5  对上下指示图标的初始化显示设置

        myAdaptor = new MyAdaptor();
        myAdaptor.setmIListItemClick(iListItemClickListener);
        manager = new GridLayoutManager(this, 5);
        mRcContent.setLayoutManager(manager);
        mRcContent.addOnScrollListener(mScrollListener);
        mRcContent.addItemDecoration(new MyDecoration());
        mRcContent.setAdapter(myAdaptor);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mList != null) {
            mList.clear();
            mList = null;
        }
    }

    private RecyclerView.OnScrollListener mScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                isDragging = true;
            }
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                if (!isDragging) {
                    JudgeIndicateVisible();
                    if (isScrollToBottom) {
                        //向下按有时会出现失去焦点的情况，用此方法来避免该情况
                        if (myAdaptor == null) return;
                        MyHolder mDownHolder = myAdaptor.mSparseArray.get(mCurrentIndex);
                        if (mDownHolder != null) mDownHolder.mView.requestFocus();
                    }
                }
            }

        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            isScrollToBottom = dy > 0;
        }
    };

    private void JudgeIndicateVisible() {
        if (mHandler != null) {
            mHandler.post(mJudgeIndicateBottomShowRunnable);
            mHandler.post(mJudgeIndicateTopShowRunnable);
        }
    }

    private Runnable mJudgeIndicateBottomShowRunnable = new Runnable() {
        @Override
        public void run() {
            if (myAdaptor == null) return;
            int itemCount = myAdaptor.getItemCount();
            int lastVisibleItemPosition = manager.findLastCompletelyVisibleItemPosition();
            Log.e(TAG, "run: lastVisibleItemPosition=" + lastVisibleItemPosition);
            Log.e(TAG, "run:childCount = " + itemCount);
            if (lastVisibleItemPosition < itemCount - 1) {
                mBottomIndicate.setVisibility(View.VISIBLE);
            } else {
                mBottomIndicate.setVisibility(View.INVISIBLE);
            }
        }
    };

    private Runnable mJudgeIndicateTopShowRunnable = new Runnable() {
        @Override
        public void run() {
            int firstVisibleItemPosition = manager.findFirstCompletelyVisibleItemPosition();
            Log.e(TAG, "run:firstVisibleItemPosition： " + firstVisibleItemPosition);
            if (firstVisibleItemPosition > COUNT_PER_ROW - 1) {
                mTopIndicate.setVisibility(View.VISIBLE);
            } else {
                mTopIndicate.setVisibility(View.INVISIBLE);
            }
        }
    };

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        if (action == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    mCurrentIndex--;
                    if (mCurrentIndex < 0) {
                        mCurrentIndex = 0;
                    }
                    manager.smoothScrollToPosition(mRcContent, null, mCurrentIndex);
                    if (myAdaptor != null && myAdaptor.mSparseArray != null) {
                        MyHolder mLeftHolder = myAdaptor.mSparseArray.get(mCurrentIndex);
                        if (mLeftHolder != null) {
                            mLeftHolder.mView.requestFocus();
                        }
                    }
                    return true;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    if (mList == null) return true;
                    mCurrentIndex++;
                    if (mCurrentIndex > mList.size() - 1) {
                        mCurrentIndex = mList.size() - 1;
                    }
                    manager.smoothScrollToPosition(mRcContent, null, mCurrentIndex);
                    if (myAdaptor != null && myAdaptor.mSparseArray != null) {
                        MyHolder mRightHolder = myAdaptor.mSparseArray.get(mCurrentIndex);
                        if (mRightHolder != null) {
                            mRightHolder.mView.requestFocus();
                        }
                    }
                    return true;
                case KeyEvent.KEYCODE_DPAD_UP:
                    if (mList == null) return true;
                    mCurrentIndex -= COUNT_PER_ROW;
                    if (mCurrentIndex < 0) {
                        mCurrentIndex = 0;
                    }
                    manager.smoothScrollToPosition(mRcContent, null, mCurrentIndex);
                    if (myAdaptor != null && myAdaptor.mSparseArray != null) {
                        MyHolder mUpHolder = myAdaptor.mSparseArray.get(mCurrentIndex);
                        if (mUpHolder != null) {
                            mUpHolder.mView.requestFocus();
                        }
                    }
                    return true;
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    if (mList == null) return true;
                    mCurrentIndex += COUNT_PER_ROW;
                    if (mCurrentIndex > mList.size() - 1) {
                        mCurrentIndex = mList.size() - 1;
                    }
                    manager.smoothScrollToPosition(mRcContent, null, mCurrentIndex);
                    if (myAdaptor != null && myAdaptor.mSparseArray != null) {
                        MyHolder mDownHolder = myAdaptor.mSparseArray.get(mCurrentIndex);
                        if (mDownHolder != null) {
                            mDownHolder.mView.requestFocus();
                        }
                    }
                    return true;
                case KeyEvent.KEYCODE_ENTER:
                case KeyEvent.KEYCODE_DPAD_CENTER:
//                    if (isError) {
//                        WebServiceUtil.getStarDetailInfo(mCurStarPeopleId, mGetDataListener);
//                        setErrorViewVisible(View.GONE);
//                        setLoadingViewVisible(View.VISIBLE);
//                    } else {
                    if (mCurrentView != null) mCurrentView.performClick();
//                    }
                    return true;
                case KeyEvent.KEYCODE_BACK:
                    finish();
                    return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    class MyHolder extends RecyclerView.ViewHolder {
        View mView;

        MyHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }
    }

    class MyAdaptor extends RecyclerView.Adapter<MyHolder> {

        IListItemClick mIListItemClick;
        SparseArray<MyHolder> mSparseArray = new SparseArray<>();

        public void setmIListItemClick(IListItemClick mIListItemClick) {
            this.mIListItemClick = mIListItemClick;
        }

        @NonNull
        @Override
        public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view_layout, parent, false);
            return new MyHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final MyHolder holder, int position) {
            String s = mList.get(position);
            if (!TextUtils.isEmpty(s)) {
                holder.mView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus) {
                            mCurrentView = v;
                            holder.mView.setSelected(true);
                        } else {
                            holder.mView.setSelected(false);
                        }
                    }
                });

                holder.mView.setTag(position);
                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mIListItemClick != null) {
                            mIListItemClick.itemClick(v);
                        }
                    }
                });


                TextView item = holder.mView.findViewById(R.id.tv_item);
                item.setText(s);


                mSparseArray.put(position, holder);

            }
        }

        @Override
        public int getItemCount() {
            return mList == null ? 0 : mList.size();
        }
    }

    class MyDecoration extends RecyclerView.ItemDecoration {
        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            outRect.left = getResources().getDimensionPixelSize(R.dimen.item_padding);
            outRect.right = getResources().getDimensionPixelSize(R.dimen.item_padding);
            outRect.top = getResources().getDimensionPixelSize(R.dimen.item_padding);
            outRect.bottom = getResources().getDimensionPixelSize(R.dimen.item_padding);
        }
    }

    private IListItemClick iListItemClickListener = new IListItemClick() {
        @Override
        public void itemClick(View view) {
            Toast.makeText(MainActivity.this, "点击了" + view.getTag(), Toast.LENGTH_SHORT).show();
        }
    };

    interface IListItemClick {
        void itemClick(View view);
    }
}
