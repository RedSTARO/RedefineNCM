package com.redstar.redefinencm.api.data

import android.provider.ContactsContract

data class loginStatus(
//    {"data":{"code":200,"account":
//    {"id":2131348937,"userName":"1_********284","type":1,"status":0,"whitelistAuthority":0,
//    "createTime":1580374122611,"tokenVersion":5,"ban":0,"baoyueVersion":0,"donateVersion":0,
//    "vipType":11,"anonimousUser":false,"paidFee":false},"profile":{"userId":2131348937,"userType":0,
//    "nickname":"RedSTAR_Cheng","avatarImgId":109951165397168030,
//    "avatarUrl":"http://p2.music.126.net/j-mPZkNI9jSTottsSa7oMw==/109951165397168025.jpg",
//    "backgroundImgId":109951166543350620,
//    "backgroundUrl":"http://p1.music.126.net/9ut7s2sknLV_uiY_1YLw1g==/109951166543350631.jpg",
//    "signature":"","createTime":1580374122647,"userName":"1_********284","accountType":1,
//    "shortUserName":"********284","birthday":1104110490901,"authority":0,"gender":0,"accountStatus":0,
//    "province":340000,"city":340100,"authStatus":0,"description":null,"detailDescription":null,
//    "defaultAvatar":false,"expertTags":null,"experts":null,"djStatus":0,"locationStatus":10,"vipType":11,
//    "followed":false,"mutual":false,"authenticated":false,"lastLoginTime":1740766887256,
//    "lastLoginIP":"182.173.75.29","remarkName":null,"viptypeVersion":1727080107015,
//    "authenticationTypes":0,"avatarDetail":null,"anchor":false}}}
    val data: loginStatusData,
)

data class loginStatusData(
    val code: Int,
    val account: loginStatusAccount,
    val profile: loginStatusProfile,
)

data class loginStatusAccount(
    val id: Long,
)

data class loginStatusProfile(
    val nickname: String,
    val avatarUrl: String,
    val backgroundUrl: String
)