package com.wangdaye.mysplash.photo.presenter;

import android.content.Context;

import com.wangdaye.mysplash.common.data.entity.unsplash.LikePhotoResult;
import com.wangdaye.mysplash.common.data.entity.unsplash.Photo;
import com.wangdaye.mysplash.common.data.service.PhotoInfoService;
import com.wangdaye.mysplash.common.data.service.PhotoService;
import com.wangdaye.mysplash.common.i.model.PhotoInfoModel;
import com.wangdaye.mysplash.common.i.presenter.PhotoInfoPresenter;
import com.wangdaye.mysplash.common.i.view.PhotoInfoView;
import com.wangdaye.mysplash.common.ui.adapter.PhotoInfoAdapter;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Photo info implementor.
 * */

public class PhotoInfoImplementor
        implements PhotoInfoPresenter {

    private PhotoInfoModel model;
    private PhotoInfoView view;

    private OnRequestPhotoDetailsListener requestPhotoListener;

    public PhotoInfoImplementor(PhotoInfoModel model, PhotoInfoView view) {
        this.model = model;
        this.view = view;
    }

    @Override
    public void requestPhoto(Context context) {
        requestPhotoListener = new OnRequestPhotoDetailsListener();
        // model.getPhotoInfoService().requestAPhoto(model.getPhoto().id, requestPhotoListener);
        model.getPhotoService().requestAPhoto(model.getPhoto().id, requestPhotoListener);
    }

    @Override
    public void setLikeForAPhoto(Context context) {
        Photo photo = model.getPhoto();
        photo.settingLike = true;
        model.setPhoto(photo);
        model.getPhotoService().setLikeForAPhoto(
                model.getPhoto().id,
                !model.getPhoto().liked_by_user,
                new OnSetLikeForAPhotoListener());
    }

    @Override
    public void cancelRequest() {
        if (requestPhotoListener != null) {
            requestPhotoListener.cancel();
        }
        // model.getPhotoInfoService().cancel();
        model.getPhotoService().cancel();
    }

    @Override
    public void touchMenuItem(int itemId) {
        view.touchMenuItem(itemId);
    }

    @Override
    public Photo getPhoto() {
        return model.getPhoto();
    }

    @Override
    public void setPhoto(Photo photo) {
        model.setPhoto(photo);
    }

    @Override
    public PhotoInfoAdapter getAdapter() {
        return model.getAdapter();
    }

    @Override
    public boolean isFailed() {
        return model.isFailed();
    }

    // interface.

    // on request single photo requestPhotoListener.

    private class OnRequestPhotoDetailsListener
            implements PhotoInfoService.OnRequestSinglePhotoListener {

        private boolean canceled;

        OnRequestPhotoDetailsListener() {
            this.canceled = false;
        }

        public void cancel() {
            this.canceled = true;
        }

        @Override
        public void onRequestSinglePhotoSuccess(Call<Photo> call, Response<Photo> response) {
            if (canceled) {
                return;
            }
            if (response.isSuccessful() && response.body() != null) {
                Photo photo = response.body();
                photo.complete = true;
                model.setPhoto(photo);
                model.setFailed(false);
                view.requestPhotoSuccess(photo);
            } else {
                model.setFailed(true);
                view.requestPhotoFailed();
            }
        }

        @Override
        public void onRequestSinglePhotoFailed(Call<Photo> call, Throwable t) {
            if (canceled) {
                return;
            }
            model.setFailed(true);
            view.requestPhotoFailed();
        }
    }

    // on set like requestPhotoListener.

    private class OnSetLikeForAPhotoListener implements PhotoService.OnSetLikeListener {

        private boolean canceled;

        OnSetLikeForAPhotoListener() {
            this.canceled = false;
        }

        public void cancel() {
            this.canceled = true;
        }

        @Override
        public void onSetLikeSuccess(Call<LikePhotoResult> call, Response<LikePhotoResult> response) {
            if (canceled) {
                return;
            }
            Photo photo = model.getPhoto();
            photo.settingLike = false;
            if (response.isSuccessful() && response.body() != null) {
                photo.liked_by_user = response.body().photo.liked_by_user;
            }
            model.setPhoto(photo);
            view.setLikeForAPhotoCompleted();
        }

        @Override
        public void onSetLikeFailed(Call<LikePhotoResult> call, Throwable t) {
            if (canceled) {
                return;
            }
            Photo photo = model.getPhoto();
            photo.settingLike = false;
            model.setPhoto(photo);
            view.setLikeForAPhotoCompleted();
        }
    }
}
