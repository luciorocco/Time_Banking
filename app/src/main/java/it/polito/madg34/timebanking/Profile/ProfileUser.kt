package it.polito.madg34.timebanking.Profile

import androidx.annotation.Keep
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

@Keep
@IgnoreExtraProperties
data class ProfileUser(
    @get: PropertyName("uri") @set: PropertyName("uri") var img: String? = "",
    @get: PropertyName("FULLNAME") @set: PropertyName("FULLNAME") var fullName: String? = "",
    @get: PropertyName("NICKNAME") @set: PropertyName("NICKNAME") var nickname: String? = "",
    @get: PropertyName("EMAIL") @set: PropertyName("EMAIL") var email: String?  = "",
    @get: PropertyName("LOCATION") @set: PropertyName("LOCATION") var location: String? = "",
    @get: PropertyName("ABOUT_ME") @set: PropertyName("ABOUT_ME") var aboutUser: String? = "",
    @get: PropertyName("Skills") @set: PropertyName("Skills") var skills: MutableMap<String, String> = mutableMapOf(),
    @get: PropertyName("TOTAL_TIME") @set: PropertyName("TOTAL_TIME") var totatl_time: String? = ""
     )

fun emptyProfile() : ProfileUser {
     return  ProfileUser(null, "", "", "", "", "", mutableMapOf(), "")
}