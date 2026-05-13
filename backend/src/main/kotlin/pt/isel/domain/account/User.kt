package pt.isel.domain.account

//data class User(
//    val id: Int,
//    val email: String,
//    val userName: String?,
//    val role: Role = Role.USER
//){
//    //TODO: CONSIDER MORE THINGS LIKE TRIMMING, MINIMUM SIZES AND VALID EMAIL FORMATS
//    init{
//        require(id >= 0) { "id must be >= 0" }
//        require(!email.isBlank()) { "Email cannot be blank" }
//        if(userName != null) require(!userName.isBlank()) { "UserName cannot be blank" }
//    }
//    companion object{
//        fun create(id: Int = 0, email: String, userName: String? = null) =
//            User(id, email, userName)
//    }
//}

