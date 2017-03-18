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
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.firebase.geofire.GeoLocation;

import java.util.List;

import static myhexaville.com.androidfirebase.Util.sizeOf;

public class Adapter extends RecyclerView.Adapter<Holder> {
    private Context mContext;
    private List<User> mUsers;

    public Adapter(Context c, List<User> users) {
        mContext = c;
        mUsers = users;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewDataBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(mContext),
                R.layout.list_item,
                parent,
                false);

        return new Holder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        User u = mUsers.get(position);
        holder.binding.setUser(u);
    }

    @Override
    public int getItemCount() {
        return sizeOf(mUsers);
    }

    public void setUsers(List<User> list) {
        mUsers = list;
        notifyDataSetChanged();
    }

    public User getUser(int position) {
        if (position > mUsers.size() - 1) {
            return new User();
        } else {
            return mUsers.get(position);
        }
    }

}
