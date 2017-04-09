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

import android.util.Log;
import android.util.Pair;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.List;

/**
 * Created with love by ihor on 2017-02-03.
 */
public class Util {
    private static Gson gson;

    public static int sizeOf(Collection c) {
        return c == null ? 0 : c.size();
    }

    public static <T> void parseJsonList(JSONObject jsonObject, List<T> list, Class<T> type) {
        try {
            JSONArray hits = jsonObject.getJSONArray("hits");
            for (int i = 0; i < hits.length(); i++) {
                JSONObject j = hits.getJSONObject(i);
                T a = convertJsonToPojo(j.toString(), type);
                list.add(a);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static <T> T convertJsonToPojo(String json, Class<T> c) {
        if (gson == null) {
            gson = new Gson();
        }
        return gson.fromJson(json, c);
    }


}
