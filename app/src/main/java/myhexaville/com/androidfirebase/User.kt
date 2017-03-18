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

package myhexaville.com.androidfirebase

import com.firebase.geofire.GeoLocation
import com.google.firebase.database.Exclude
import com.google.firebase.database.ServerValue.TIMESTAMP
import myhexaville.com.androidfirebase.Constants.NAMES

/*
* Default constructor which is required for Firebase models is gerenarad by default
* */
class User(
        var name: String? = null,
        var age: Int? = null,
        var timestamp: Any? = null,
        var latitude: Double? = null,
        var longitude: Double? = null,
        @Exclude var id: String? = null) {

    companion object {
        fun randomUser(location: GeoLocation): User {
            val name = randomName
            val age = randomAge
            return User(name, age, TIMESTAMP, location.latitude, location.longitude, "0")
        }

        val randomName: String
            get() = NAMES[(Math.random() * NAMES.size).toInt()]

        val randomAge: Int
            get() = (Math.random() * 30).toInt() + 10
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as User

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

}
