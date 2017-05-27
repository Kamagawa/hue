package me.eugenewang.hue.activities;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import me.eugenewang.hue.R;
import me.eugenewang.hue.data.ColorItem;
import me.eugenewang.hue.data.ColorItems;
import me.eugenewang.hue.utils.Cameras;
import me.eugenewang.hue.views.CameraColorPickerPreview;


public class ColorPickerBaseActivity extends AppCompatActivity
        implements CameraColorPickerPreview.OnColorSelectedListener, View.OnClickListener {


    protected static final String TAG = ColorPickerBaseActivity.class.getSimpleName();
    protected static final String PICKED_COLOR_PROGRESS_PROPERTY_NAME = "pickedColorProgress";
    protected static final String SAVE_COMPLETED_PROGRESS_PROPERTY_NAME = "saveCompletedProgress";
    protected static final long DURATION_CONFIRM_SAVE_MESSAGE = 400;
    protected static final long DELAY_HIDE_CONFIRM_SAVE_MESSAGE = 1400;
    private static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return c;
    }

    protected RelativeLayout mRelativeLayout;
    protected Camera mCamera;
    protected boolean mIsPortrait;
    protected FrameLayout mPreviewContainer;
    protected CameraColorPickerPreview mCameraPreview;
    protected CameraAsyncTask mCameraAsyncTask;
    protected int mSelectedColor;
    protected int mLastPickedColor;
    protected View mPickedColorPreview;
    protected View mPickedColorPreviewAnimated;
    protected ObjectAnimator mPickedColorProgressAnimator;
    protected float mTranslationDeltaX;
    protected float mTranslationDeltaY;
    protected TextView mColorPreviewText;
    protected View mPointerRing;
    protected float mSaveCompletedProgress;
    protected ObjectAnimator mSaveCompletedProgressAnimator;
    protected TextView mConfirmSaveMessage;
    protected Interpolator mConfirmSaveMessageInterpolator;
    protected Runnable mHideConfirmSaveMessage;
    protected boolean mIsFlashOn;
    protected String action = null;
    public static final String OI_COLOR_PICKER = "org.openintents.action.PICK_COLOR";
    public static final String OI_COLOR_DATA = "org.openintents.extra.COLOR";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_picker);

        initPickedColorProgressAnimator();
        initSaveCompletedProgressAnimator();
        initViews();
        initTranslationDeltas();

        Intent intent = getIntent();
        if (intent != null)
            action = intent.getAction();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Setup the camera asynchronously.
        mCameraAsyncTask = new CameraAsyncTask();
        mCameraAsyncTask.execute();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Cancel the Camera AsyncTask.
        mCameraAsyncTask.cancel(true);

        // Release the camera.
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }

        // Remove the camera preview
        if (mCameraPreview != null) {
            mPreviewContainer.removeView(mCameraPreview);
        }
    }

    @Override
    protected void onDestroy() {
        // Remove any pending mHideConfirmSaveMessage.
        mConfirmSaveMessage.removeCallbacks(mHideConfirmSaveMessage);

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isFlashSupported()) {
            getMenuInflater().inflate(R.menu.menu_color_picker, menu);
            final MenuItem flashItem = menu.findItem(R.id.menu_color_picker_action_flash);
            int flashIcon = mIsFlashOn ? R.drawable.ic_action_flash_off : R.drawable.ic_action_flash_on;
            flashItem.setIcon(flashIcon);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        boolean handled;
        switch (itemId) {
            case android.R.id.home:
                finish();
                handled = true;
                break;

            case R.id.menu_color_picker_action_flash:
                toggleFlash();
                handled = true;
                break;

            default:
                handled = super.onOptionsItemSelected(item);
        }

        return handled;
    }

    @Override
    public void onColorSelected(int color) {
        mSelectedColor = color;
        mPointerRing.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
    }

    @Override
    public void onClick(View v) {
        if (v == mCameraPreview) {
            animatePickedColor(mSelectedColor);
        } else if (v.getId() == R.id.activity_color_picker_save_button) {
            if (OI_COLOR_PICKER.equals(action)) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra(OI_COLOR_DATA, mLastPickedColor);
                setResult(RESULT_OK, returnIntent);
                finish();
                return;
            }
            ColorItems.saveColorItem(this, new ColorItem(mLastPickedColor));
        } else if (v == mRelativeLayout) {
            Intent myIntent = new Intent(ColorPickerBaseActivity.this, SearchView.class);
            myIntent.putExtra("URL", mColorPreviewText.getText().subSequence(1, 7));
            startActivity(myIntent);
        }
    }

    /**
     * Initialize the views used in this activity.
     * <p/>
     * Internally find the view by their ids and set the click listeners.
     */

    protected void initViews() {
        mIsPortrait = getResources().getBoolean(R.bool.is_portrait);
        mPreviewContainer = (FrameLayout) findViewById(R.id.activity_color_picker_preview_container);
        mPickedColorPreview = findViewById(R.id.activity_color_picker_color_preview);
        mPickedColorPreviewAnimated = findViewById(R.id.activity_color_picker_animated_preview);
        mColorPreviewText = (TextView) findViewById(R.id.activity_color_picker_color_preview_text);
        mPointerRing = findViewById(R.id.activity_color_picker_pointer_ring);
        mRelativeLayout = (RelativeLayout ) findViewById(R.id.activity_color_picker_bottom_bar);

        mConfirmSaveMessage = (TextView) findViewById(R.id.activity_color_picker_confirm_save_message);
        mHideConfirmSaveMessage = new Runnable() {
            @Override
            public void run() {
                mConfirmSaveMessage.animate()
                        .translationY(-mConfirmSaveMessage.getMeasuredHeight())
                        .setDuration(DURATION_CONFIRM_SAVE_MESSAGE)
                        .start();
            }
        };
        positionConfirmSaveMessage();
        mConfirmSaveMessageInterpolator = new DecelerateInterpolator();

        mLastPickedColor = ColorItems.getLastPickedColor(this);
        applyPreviewColor(mLastPickedColor);

        mRelativeLayout.setOnClickListener(this);
    }

    /**
     * Position mConfirmSaveMessage.
     * <p/>
     * Set the translationY of mConfirmSaveMessage to - mConfirmSaveMessage.getMeasuredHeight() so that it is correctly placed before the first animation.
     */
    protected void positionConfirmSaveMessage() {
        ViewTreeObserver vto = mConfirmSaveMessage.getViewTreeObserver();
        if (vto.isAlive()) {
            vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    ViewTreeObserver vto = mConfirmSaveMessage.getViewTreeObserver();
                    vto.removeOnPreDrawListener(this);
                    mConfirmSaveMessage.setTranslationY(-mConfirmSaveMessage.getMeasuredHeight());
                    return true;
                }
            });
        }
    }

    /**
     * Initialize the deltas used for the translation of the preview of the picked color.
     */
    @SuppressLint("NewApi")
    protected void initTranslationDeltas() {
        ViewTreeObserver vto = mPointerRing.getViewTreeObserver();
        if (vto.isAlive()) {
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    ViewTreeObserver vto = mPointerRing.getViewTreeObserver();
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) {
                        vto.removeGlobalOnLayoutListener(this);
                    } else {
                        vto.removeOnGlobalLayoutListener(this);
                    }

                    final Rect pointerRingRect = new Rect();
                    final Rect colorPreviewAnimatedRect = new Rect();
                    mPickedColorPreviewAnimated.getGlobalVisibleRect(pointerRingRect);
                    mPickedColorPreview.getGlobalVisibleRect(colorPreviewAnimatedRect);

                    mTranslationDeltaX = colorPreviewAnimatedRect.left - pointerRingRect.left;
                    mTranslationDeltaY = colorPreviewAnimatedRect.top - pointerRingRect.top;
                }
            });
        }
    }


    /**
     * Initialize the animator used for the progress of the picked color.
     */
    protected void initPickedColorProgressAnimator() {
        mPickedColorProgressAnimator = ObjectAnimator.ofFloat(this, PICKED_COLOR_PROGRESS_PROPERTY_NAME, 0f, 1f);
        mPickedColorProgressAnimator.setDuration(400);
        mPickedColorProgressAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mPickedColorPreviewAnimated.setVisibility(View.VISIBLE);
                mPickedColorPreviewAnimated.getBackground().setColorFilter(mSelectedColor, PorterDuff.Mode.SRC_ATOP);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                ColorItems.saveLastPickedColor(ColorPickerBaseActivity.this, mLastPickedColor);
                applyPreviewColor(mLastPickedColor);
                mPickedColorPreviewAnimated.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mPickedColorPreviewAnimated.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    /**
     * Check if the device's camera supports flash.
     *
     * @return Returns true if the device's camera supports flash, false otherwise.
     */
    protected boolean isFlashSupported() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    /**
     * Toggle the device's camera flash.
     * {@link ColorPickerBaseActivity#isFlashSupported()} should be called before using this methods.
     */
    protected void toggleFlash() {
        if (mCamera != null) {
            final Camera.Parameters parameters = mCamera.getParameters();
            final String flashParameter = mIsFlashOn ? Camera.Parameters.FLASH_MODE_OFF : Camera.Parameters.FLASH_MODE_TORCH;
            parameters.setFlashMode(flashParameter);

            // Set the preview callback to null and stop the preview
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();

            // Change the parameters
            mCamera.setParameters(parameters);

            // Restore the preview callback and re-start the preview
            mCamera.setPreviewCallback(mCameraPreview);
            mCamera.startPreview();

            mIsFlashOn = !mIsFlashOn;
            invalidateOptionsMenu();
        }
    }

    /**
     * Initialize the animator used for the progress of the "save completed" state.
     */
    protected void initSaveCompletedProgressAnimator() {
        mSaveCompletedProgressAnimator = ObjectAnimator.ofFloat(this, SAVE_COMPLETED_PROGRESS_PROPERTY_NAME, 1f, 0f);
    }

    /**
     * Apply the preview color.
     * <p/>
     * Display the preview color and its human representation.
     *
     * @param previewColor the preview color to apply.
     */
    protected void applyPreviewColor(int previewColor) {
        mPickedColorPreview.getBackground().setColorFilter(previewColor, PorterDuff.Mode.SRC_ATOP);
        mColorPreviewText.setText(ColorItem.makeHexString(previewColor) + " : " + ColorItem.getColor(previewColor));
    }

    /**
     * Animate the color being picked.
     *
     * @param pickedColor the color being picked.
     */
    protected void animatePickedColor(int pickedColor) {
        mLastPickedColor = pickedColor;
        if (mPickedColorProgressAnimator.isRunning()) {
            mPickedColorProgressAnimator.cancel();
        }
        mPickedColorProgressAnimator.start();
    }

    /**
     * Set the "save completed" state.
     * <p/>
     * True means that the save is completed. The preview color should not be saved again.
     *
     * @param isSaveCompleted the "save completed" state.
     */


    /**
     * Set the progress of the picked color animation.
     * <p/>
     * Used by {@link ColorPickerBaseActivity#mPickedColorProgressAnimator}.
     *
     * @param progress A value in closed range [0,1] representing the progress of the picked color animation.
     */
    protected void setPickedColorProgress(float progress) {
        final float fastOppositeProgress = (float) Math.pow(progress, 0.3f);
        final float translationX = mTranslationDeltaX * progress;
        final float translationY = (float) (mTranslationDeltaY * Math.pow(progress, 2f));

        mPickedColorPreviewAnimated.setTranslationX(translationX);
        mPickedColorPreviewAnimated.setTranslationY(translationY);
        mPickedColorPreviewAnimated.setScaleX(fastOppositeProgress);
        mPickedColorPreviewAnimated.setScaleY(fastOppositeProgress);
    }

    /**
     * Set the progress of the animation of the "save completed" state.
     * <p/>
     * Used by {@link ColorPickerBaseActivity#mSaveCompletedProgressAnimator}.
     *
     * @param progress A value in closed range [0,1] representing the progress of the animation of the "save completed" state.
     */


    /**
     * Async task used to configure and start the camera preview.
     */
    private class CameraAsyncTask extends AsyncTask<Void, Void, Camera> {

        /**
         * The {@link android.view.ViewGroup.LayoutParams} used for adding the preview to its container.
         */
        protected FrameLayout.LayoutParams mPreviewParams;

        @Override
        protected Camera doInBackground(Void... params) {
            Camera camera = getCameraInstance();
            if (camera == null) {
                ColorPickerBaseActivity.this.finish();
            } else {
                //configure Camera parameters
                Camera.Parameters cameraParameters = camera.getParameters();

                //get optimal camera preview size according to the layout used to display it
                Camera.Size bestSize = Cameras.getBestPreviewSize(
                        cameraParameters.getSupportedPreviewSizes()
                        , mPreviewContainer.getWidth()
                        , mPreviewContainer.getHeight()
                        , mIsPortrait);
                //set optimal camera preview
                cameraParameters.setPreviewSize(bestSize.width, bestSize.height);
                camera.setParameters(cameraParameters);

                //set camera orientation to match with current device orientation
                Cameras.setCameraDisplayOrientation(ColorPickerBaseActivity.this, camera);

                //get proportional dimension for the layout used to display preview according to the preview size used
                int[] adaptedDimension = Cameras.getProportionalDimension(
                        bestSize
                        , mPreviewContainer.getWidth()
                        , mPreviewContainer.getHeight()
                        , mIsPortrait);

                //set up params for the layout used to display the preview
                mPreviewParams = new FrameLayout.LayoutParams(adaptedDimension[0], adaptedDimension[1]);
                mPreviewParams.gravity = Gravity.CENTER;
            }
            return camera;
        }

        @Override
        protected void onPostExecute(Camera camera) {
            super.onPostExecute(camera);

            // Check if the task is cancelled before trying to use the camera.
            if (!isCancelled()) {
                mCamera = camera;
                if (mCamera == null) {
                    ColorPickerBaseActivity.this.finish();
                } else {
                    //set up camera preview
                    mCameraPreview = new CameraColorPickerPreview(ColorPickerBaseActivity.this, mCamera);
                    mCameraPreview.setOnColorSelectedListener(ColorPickerBaseActivity.this);
                    mCameraPreview.setOnClickListener(ColorPickerBaseActivity.this);

                    //add camera preview
                    mPreviewContainer.addView(mCameraPreview, 0, mPreviewParams);
                }
            }
        }

        @Override
        protected void onCancelled(Camera camera) {
            super.onCancelled(camera);
            if (camera != null) {
                camera.release();
            }
        }
    }
}
