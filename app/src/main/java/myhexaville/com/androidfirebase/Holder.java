/*
 * Copyright (C) 2017 The Android Open Source Project
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

package myhexaville.com.androidfirebase;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import myhexaville.com.androidfirebase.databinding.ListItemBinding;

import static android.widget.Toast.LENGTH_SHORT;

/**
 * Created with love by ihor on 2017-02-03.
 */
public class Holder extends RecyclerView.ViewHolder {
    ListItemBinding binding;

    public Holder(View itemView) {
        super(itemView);
        binding = DataBindingUtil.bind(itemView);
        itemView.setOnClickListener(v -> {
            User user = binding.getUser();
            Context c = itemView.getContext();
            Toast.makeText(
                    c,
                    "Location of " + user.getName() + " is " + user.getLatitude() + " " + user.getLongitude()
                    , LENGTH_SHORT).show();

            Intent intent = new Intent(c, MapsActivity.class)
                    .putExtra("lat", user.getLatitude() + "")
                    .putExtra("lng", user.getLongitude() + "")
                    .putExtra("name", user.getName());

            c.startActivity(intent);
        });
    }
}
