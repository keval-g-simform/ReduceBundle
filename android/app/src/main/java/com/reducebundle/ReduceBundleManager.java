package com.reducebundle;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import androidx.annotation.NonNull;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.play.core.assetpacks.AssetPackLocation;
import com.google.android.play.core.assetpacks.AssetPackManager;
import com.google.android.play.core.assetpacks.AssetPackManagerFactory;
import com.google.android.play.core.assetpacks.AssetPackState;
import com.google.android.play.core.assetpacks.AssetPackStateUpdateListener;
import com.google.android.play.core.assetpacks.AssetPackStates;
import com.google.android.play.core.assetpacks.model.AssetPackStatus;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReduceBundleManager extends ReactContextBaseJavaModule {
    String assetPackName = "fast_follow_asset_pack";
    String TAG = "PAD";

    @NonNull
    @Override
    public String getName() {
        return "ReduceBundleManager";
    }

    ReduceBundleManager(ReactApplicationContext context) throws PackageManager.NameNotFoundException {
        super(context);
    }

    Context currentAppContext = getReactApplicationContext().createPackageContext("com.reducebundle", 0);
    AssetPackManager assetPackManager = AssetPackManagerFactory.getInstance(currentAppContext);
    private AssetPackState assetPackState;

    private String getAbsoluteAssetPath(String assetPack) {
        AssetPackLocation assetPackPath = assetPackManager.getPackLocation(assetPack);
        Log.d(TAG, "assetPackPath: " + assetPackPath);

        if (assetPackPath == null) {
            Log.d(TAG, "assetPackPath: null");
            // asset pack is not ready
            return null;
        }

        String assetsPath = assetPackPath.assetsPath();
        Log.d(TAG, "assetsPath: " + assetsPath);

        return assetsPath;
    }

    /**
     * This method is used to Get download information about asset packs
     */
    private void getPackStates(String packName, String folderRefName, String imageName, Promise promise) {
        assetPackManager.getPackStates(Collections.singletonList(packName))
                .addOnCompleteListener(new OnCompleteListener<AssetPackStates>() {
                    @Override
                    public void onComplete(@NonNull Task<AssetPackStates> task) {
                        AssetPackStates assetPackStates;
                        try {
                            assetPackStates = task.getResult();
                            assetPackState = assetPackStates.packStates().get(packName);
                            Log.d(TAG, "assetPackState: " + assetPackState);

                            Log.d(TAG, "status: " + assetPackState.status() +
                                    ", imageName: " + assetPackState.name() +
                                    ", errorCode: " + assetPackState.errorCode() +
                                    ", bytesDownloaded: " + assetPackState.bytesDownloaded() +
                                    ", totalBytesToDownload: " + assetPackState.totalBytesToDownload() +
                                    ", transferProgressPercentage: " + assetPackState.transferProgressPercentage());

                            if (assetPackState != null) {
                                Log.d(TAG, "assetPackState.status(): " + assetPackState.status());
                                registerListener(folderRefName, imageName, promise);
                            }
                        } catch (Exception e) {
                            Log.d("MainActivity", e.getMessage());
                        }
                    }
                });
    }

    private void registerListener(String folderRefName,String imageName, Promise promise) {
        String fastFollowAssetPackPath = getAbsoluteAssetPath(assetPackName);
        if (fastFollowAssetPackPath == null) {
            assetPackManager.registerListener(new AssetPackStateUpdateListener() {
                @Override
                public void onStateUpdate(@NonNull AssetPackState state) {
                    switch (state.status()) {
                        case AssetPackStatus.PENDING:
                            Log.i(TAG, "Pending");
                            break;

                        case AssetPackStatus.DOWNLOADING:
                            long downloaded = state.bytesDownloaded();
                            Log.i(TAG, "downloaded=" + downloaded);
                            long totalSize = state.totalBytesToDownload();
                            Log.i(TAG, "totalSize=" + totalSize);
                            double percent = 100.0 * downloaded / totalSize;

                            Log.i(TAG, "PercentDone=" + String.format("%.2f", percent));
                            break;

                        case AssetPackStatus.TRANSFERRING:
                            // 100% downloaded and assets are being transferred.
                            Log.i(TAG, "TRANSFERRING=");
                            // Notify user to wait until transfer is complete.
                            break;

                        case AssetPackStatus.COMPLETED:
                            Log.i(TAG, "COMPLETED=");
                            // Asset pack is ready to use. Start the Game/App.

                            String assetPath = getAbsoluteAssetPath(assetPackName);
                            Log.d(TAG, "onComplete: YY" + assetPath);
                            assetPackManager.unregisterListener(this); // Unregister the listener once complete
                            promise.resolve("file://" + assetPath + "/images" + folderRefName + "/" + imageName);
                            break;

                        case AssetPackStatus.FAILED:
                            // Request failed. Notify user.
                            Log.i(TAG, "FAILED=" + String.valueOf(state.errorCode()));
                            break;

                        case AssetPackStatus.CANCELED:
                            // Request canceled. Notify user.
                            Log.i(TAG, "CANCELED=");
                            break;

                        case AssetPackStatus.WAITING_FOR_WIFI:
                            // showWifiConfirmationDialog();
                            Log.i(TAG, "WAITING_FOR_WIFI=");
                            break;

                        case AssetPackStatus.NOT_INSTALLED:
                            // Asset pack is not downloaded yet.
                            Log.i(TAG, "NOT_INSTALLED=");
                            break;
                        case AssetPackStatus.UNKNOWN:
                            // The Asset pack state is unknown
                            Log.i(TAG, "UNKNOWN=");
                            break;
                    }

                }
            });

            List<String> assetPackList = new ArrayList<>();
            assetPackList.add(assetPackName);
            Log.d(TAG, "assetPackList: " + assetPackList);
            assetPackManager.fetch(assetPackList);
            Log.d(TAG, "assetPackManager: " + assetPackManager);
        } else {
            Log.d(TAG, "registerListener: else");
        }
    }

    @ReactMethod
    public void loadImageWithTag(String tag, String type, Promise promise) {
        float density = currentAppContext.getResources().getDisplayMetrics().density;
        String imageName = tag + "." + type;

        String folderRefName;
        if (density >= 4.0) {
            folderRefName = "@3x";
        } else if (density >= 3.0) {
            folderRefName = "@3x";
        } else if (density >= 2.0) {
            folderRefName = "@2x";
        } else if (density >= 1.5) {
            folderRefName = "@2x";
        } else {
            folderRefName = "";
        }

        String assetsPath = getAbsoluteAssetPath(assetPackName);
        Log.d(TAG, "assetPackLocation: " + assetsPath);

        if (assetsPath == null) {
            getPackStates(assetPackName, folderRefName, imageName, promise);
        } else {
            Log.d(TAG, "Else part assetsPath: " + assetsPath + "/images" + folderRefName + "/" + imageName);
            promise.resolve("file://" + assetsPath + "/images" + folderRefName + "/"+ imageName);
        }
    }
}
