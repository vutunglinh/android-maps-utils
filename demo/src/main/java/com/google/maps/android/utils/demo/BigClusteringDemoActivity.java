/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.maps.android.utils.demo;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.algo.GridBasedAlgorithm;
import com.google.maps.android.clustering.algo.NonHierarchicalViewBasedAlgorithm;
import com.google.maps.android.clustering.algo.PreCachingAlgorithmDecorator;
import com.google.maps.android.utils.demo.model.MyItem;

import org.json.JSONException;

import java.io.InputStream;
import java.util.*;

public class BigClusteringDemoActivity extends BaseDemoActivity {
    private ClusterManager<MyItem> mClusterManager;
    private HashMap<Integer, HashSet<MyItem>> lookup = new HashMap<>();
    private HashMap<Integer, HashSet<MyItem>> removes = new HashMap<>();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout linearLayout = new LinearLayout(getApplicationContext());
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        addContentView(linearLayout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        Random random = new Random();
        Button button = new Button(getApplicationContext());
        button.setText("Remove");

        button.setOnClickListener(v -> {
            try {
                Integer i = 0;
                HashSet<MyItem> myItems = lookup.remove(i = lookup.keySet().iterator().next());


                boolean result = mClusterManager.removeItems(myItems);

                Log.d(BigClusteringDemoActivity.class.getSimpleName(), "remove "+i+" "+myItems.size()+" result =  "+result);

                mClusterManager.cluster();

                removes.put(i, myItems);

            } catch (Exception e) {
                Log.d(BigClusteringDemoActivity.class.getSimpleName(), ""+e.getMessage());
            }

        });
        linearLayout.addView(button, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        button = new Button(getApplicationContext());
        button.setText("Add");

        button.setOnClickListener(v -> {
            try {
                Integer i = 0;
                HashSet<MyItem> myItems = removes.remove(i = removes.keySet().iterator().next());


                boolean result = mClusterManager.addItems(myItems);

                Log.d(BigClusteringDemoActivity.class.getSimpleName(), "add "+i+" "+myItems.size()+" result =  "+result);
                mClusterManager.cluster();

                lookup.put(i, myItems);

            } catch (Exception e) {
                Log.d(BigClusteringDemoActivity.class.getSimpleName(), ""+e.getMessage());
            }
        });

        linearLayout.addView(button, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    @Override
    protected void startDemo(boolean isRestore) {
        if (!isRestore) {
            getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(51.503186, -0.126446), 10));
        }

        mClusterManager = new ClusterManager<>(this, getMap());
//        mClusterManager.setAlgorithm(new GridBasedAlgorithm<MyItem>());
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int widthDp = (int) (metrics.widthPixels / metrics.density);
        int heightDp = (int) (metrics.heightPixels / metrics.density);
        mClusterManager.setAlgorithm(new NonHierarchicalViewBasedAlgorithm<>(widthDp, heightDp));

        getMap().setOnCameraIdleListener(mClusterManager);
        try {
            readItems();
        } catch (JSONException e) {
            Toast.makeText(this, "Problem reading list of markers.", Toast.LENGTH_LONG).show();
        }
    }

    private void readItems() throws JSONException {
        InputStream inputStream = getResources().openRawResource(R.raw.radar_search);
        List<MyItem> items = new MyItemReader().read(inputStream);
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            double offset = i / 60d;
            for (MyItem item : items) {
                LatLng position = item.getPosition();
                double lat = position.latitude + offset;
                double lng = position.longitude + offset;
                MyItem offsetItem = new MyItem(lat, lng);
                mClusterManager.addItem(offsetItem);
                offsetItem.setType(random.nextInt(10));
                HashSet<MyItem> arrayList = lookup.containsKey(offsetItem.getType()) ? lookup.get(offsetItem.getType()) : new HashSet<>();
                arrayList.add(offsetItem);
                lookup.put(offsetItem.getType(), arrayList);

            }
        }
    }
}
