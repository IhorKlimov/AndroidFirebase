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


import com.algolia.search.saas.Query;
import com.algolia.search.saas.Query.LatLng;

public class Constants {
    public static final String[] NAMES = new String[]{
            "David",
            "John",
            "Paul",
            "Mark",
            "James",
            "Andrew",
            "Scott",
            "Steven",
            "Robert",
            "Stephen",
            "William",
            "Craig",
            "Michael",
            "Stuart",
    };

    public static final LatLng NEW_YORK = new LatLng(40.730292, -73.990401);
    public static final LatLng FORT_LAUDERDALE_FL = new LatLng(26.128536, -80.130648);
    public static final LatLng BOSTON_MA = new LatLng(42.357741, -71.058799);

    public static final LatLng[] LOCATIONS = {
            NEW_YORK, FORT_LAUDERDALE_FL, BOSTON_MA
    };

    public static LatLng randomLocation() {
        return LOCATIONS[(int) (Math.random() * LOCATIONS.length)];
    }
}
