package com.example.jingbin.cloudreader.ui.douban;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.ImageView;

import com.example.jingbin.cloudreader.R;
import com.example.jingbin.cloudreader.adapter.FilmDetailActorAdapter;
import com.example.jingbin.cloudreader.adapter.FilmDetailImageAdapter;
import com.example.jingbin.cloudreader.adapter.FilmDetailVideoAdapter;
import com.example.jingbin.cloudreader.adapter.MovieDetailAdapter;
import com.example.jingbin.cloudreader.base.BaseHeaderActivity;
import com.example.jingbin.cloudreader.bean.FilmDetailBasicBean;
import com.example.jingbin.cloudreader.bean.FilmDetailBean;
import com.example.jingbin.cloudreader.bean.MovieDetailBean;
import com.example.jingbin.cloudreader.bean.moviechild.FilmItemBean;
import com.example.jingbin.cloudreader.databinding.ActivityFilmDetailBinding;
import com.example.jingbin.cloudreader.databinding.HeaderFilmDetailBinding;
import com.example.jingbin.cloudreader.http.HttpClient;
import com.example.jingbin.cloudreader.utils.CommonUtils;
import com.example.jingbin.cloudreader.view.webview.WebViewActivity;

import java.util.List;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


/**
 * 时光网电影详情页：
 *
 * @author jingbin 2019-05-14
 */
public class FilmDetailActivity extends BaseHeaderActivity<HeaderFilmDetailBinding, ActivityFilmDetailBinding> {

    private FilmItemBean subjectsBean;
    private String mMoreUrl;
    private String mMovieName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_film_detail);
        if (getIntent() != null) {
            subjectsBean = (FilmItemBean) getIntent().getSerializableExtra("bean");
        }

        initSlideShapeTheme(setHeaderImgUrl(), setHeaderImageView());

        setTitle(subjectsBean.getTCn());
        setSubTitle(subjectsBean.getTEn());
//        ImageLoadUtil.showImg(bindingHeaderView.ivOnePhoto,subjectsBean.getImages().getLarge());
        bindingHeaderView.setSubjectsBean(subjectsBean);
        bindingHeaderView.executePendingBindings();

        loadMovieDetail();
    }

    @Override
    protected void setTitleClickMore() {
        WebViewActivity.loadUrl(FilmDetailActivity.this, mMoreUrl, mMovieName);
    }

    @Override
    protected int setHeaderLayout() {
        return R.layout.header_film_detail;
    }

    @Override
    protected String setHeaderImgUrl() {
        if (subjectsBean == null) {
            return "";
        }
        return subjectsBean.getImg();
    }

    @Override
    protected ImageView setHeaderImageView() {
        return bindingHeaderView.imgItemBg;
    }

    private void loadMovieDetail() {
        HttpClient.Builder.getMtimeServer().getFilmDetail(subjectsBean.getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<FilmDetailBasicBean>() {

                    @Override
                    public void onError(Throwable e) {
                        showError();
                    }

                    @Override
                    public void onComplete() {
                        showContentView();
                    }

                    @Override
                    public void onSubscribe(Disposable d) {
                        addSubscription(d);
                    }

                    @Override
                    public void onNext(final FilmDetailBasicBean bean) {
                        if (bean != null) {
                            FilmDetailBasicBean.ReleaseBean release = bean.getRelease();
                            if (release != null) {
//                                String text = release.getDate() + "(" + release.getLocation() + ")";
                                // 上映日期
                                bindingHeaderView.tvOneDate.setText(String.format("上映日期：%s", release.getDate()));
                            }
                            bindingHeaderView.tvOneTime.setText(String.format("片长：%s", bean.getRunTime()));
                            bindingHeaderView.tvOneRatingNumber.setText(String.format("%s人评分", bean.getScoreCount()));
                            bindingContentView.setBean(bean);
                            bindingContentView.executePendingBindings();

//                            mMoreUrl = bean.getUrl();
                            mMoreUrl = bean.getVideo();
                            mMovieName = bean.getTitleCn();

                            transformData(bean);
                        }
                    }
                });
    }

    /**
     * 异步线程转换数据
     */
    private void transformData(final FilmDetailBasicBean bean) {
        if (bean.getActorList() != null && bean.getActorList().size() > 0) {
            FilmDetailBasicBean.DirectorBean director = bean.getDirector();
            if (director != null) {
                FilmDetailBasicBean.ActorListBean listBean = new FilmDetailBasicBean.ActorListBean();
                listBean.setActor(director.getDirectorName());
                listBean.setActorEn(director.getDirectorNameEn());
                listBean.setActorImg(director.getDirectorImg());
                listBean.setRoleName("导演");
                bean.getActorList().add(0, listBean);
            }
            setAdapter(bean.getActorList());
        }
        if (bean.getImages() != null && bean.getImages().size() > 0) {
            setImageAdapter(bean.getImages());
        }
        if (bean.getVideos() != null && bean.getVideos().size() > 0) {
            setVideoAdapter(bean.getVideos());
        }
    }

    /**
     * 设置导演&演员adapter
     */
    private void setAdapter(List<FilmDetailBasicBean.ActorListBean> listBeans) {
        bindingContentView.xrvCast.setVisibility(View.VISIBLE);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(FilmDetailActivity.this);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        bindingContentView.xrvCast.setLayoutManager(mLayoutManager);
        // 需加，不然滑动不流畅
        bindingContentView.xrvCast.setNestedScrollingEnabled(false);
        bindingContentView.xrvCast.setHasFixedSize(false);

        FilmDetailActorAdapter mAdapter = new FilmDetailActorAdapter();
        mAdapter.addAll(listBeans);
        bindingContentView.xrvCast.setAdapter(mAdapter);
        bindingContentView.xrvCast.setFocusable(false);
        bindingContentView.xrvCast.setFocusableInTouchMode(false);
    }

    private void setVideoAdapter(List<FilmDetailBasicBean.VideosBean> listBeans) {
        bindingContentView.xrvVideo.setVisibility(View.VISIBLE);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(FilmDetailActivity.this);
        mLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        bindingContentView.xrvVideo.setLayoutManager(mLayoutManager);
        // 需加，不然滑动不流畅
        bindingContentView.xrvVideo.setNestedScrollingEnabled(false);
        bindingContentView.xrvVideo.setHasFixedSize(false);

        FilmDetailVideoAdapter mAdapter = new FilmDetailVideoAdapter();
        mAdapter.addAll(listBeans);
        bindingContentView.xrvVideo.setAdapter(mAdapter);
        bindingContentView.xrvVideo.setFocusable(false);
        bindingContentView.xrvVideo.setFocusableInTouchMode(false);
    }

    private void setImageAdapter(List<String> listBeans) {
        bindingContentView.xrvImages.setVisibility(View.VISIBLE);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(FilmDetailActivity.this);
        mLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        bindingContentView.xrvImages.setLayoutManager(mLayoutManager);
        // 需加，不然滑动不流畅
        bindingContentView.xrvImages.setNestedScrollingEnabled(false);
        bindingContentView.xrvImages.setHasFixedSize(false);

        FilmDetailImageAdapter mAdapter = new FilmDetailImageAdapter();
        mAdapter.addAll(listBeans);
        bindingContentView.xrvImages.setAdapter(mAdapter);
        bindingContentView.xrvImages.setFocusable(false);
        bindingContentView.xrvImages.setFocusableInTouchMode(false);
    }

    @Override
    protected void onRefresh() {
        loadMovieDetail();
    }

    /**
     * @param context      activity
     * @param positionData bean
     * @param imageView    imageView
     */
    public static void start(Activity context, FilmItemBean positionData, ImageView imageView) {
        Intent intent = new Intent(context, FilmDetailActivity.class);
        intent.putExtra("bean", positionData);
        ActivityOptionsCompat options =
                ActivityOptionsCompat.makeSceneTransitionAnimation(context,
                        imageView, CommonUtils.getString(R.string.transition_movie_img));//与xml文件对应
        ActivityCompat.startActivity(context, intent, options.toBundle());
    }

}