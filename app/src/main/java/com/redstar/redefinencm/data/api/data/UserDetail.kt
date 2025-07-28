package com.redstar.redefinencm.data.api.data

data class UserDetail(
// {"level":10,"listenSongs":34721,"userPoint":{"userId":32953014,"balance":14,"updateTime":1741611268941,"version":10,"status":1,"blockBalance":0},"mobileSign":false,"pcSign":false,"profile":{"privacyItemUnlimit":{"area":true,"college":true,"gender":true,"age":true,"villageAge":true},"avatarDetail":null,"accountStatus":0,"authStatus":0,"avatarImgId":109951169598555170,"avatarUrl":"http://p1.music.126.net/ZN_BmYYBfuXtphZaGRCkbg==/109951169598555174.jpg","backgroundImgId":109951163792144620,"backgroundUrl":"http://p1.music.126.net/WLTBvNL_l9ZKlslFwaCM9Q==/109951163792144631.jpg","birthday":768967898000,"city":440300,"detailDescription":"","djStatus":10,"expertTags":null,"followed":false,"gender":1,"mutual":false,"nickname":"binaryify","province":440000,"remarkName":null,"userType":0,"defaultAvatar":false,"experts":{},"vipType":11,"createTime":1407747900967,"backgroundImgIdStr":"109951163792144631","avatarImgIdStr":"109951169598555174","description":"","userId":32953014,"signature":"emmm","authority":0,"followeds":82,"follows":22,"blacklist":false,"eventCount":21,"allSubscribedCount":0,"playlistBeSubscribedCount":5,"followTime":null,"followMe":false,"artistIdentity":[],"cCount":0,"inBlacklist":false,"sDJPCount":0,"playlistCount":21,"sCount":0,"newFollows":22},"peopleCanSeeMyPlayRecord":false,"bindings":[{"expiresIn":2147483647,"refreshTime":1592285666,"bindingTime":1426295169224,"tokenJsonStr":null,"url":"","expired":false,"userId":32953014,"id":28098251,"type":1},{"expiresIn":2628968,"refreshTime":1507142393,"bindingTime":1407747883151,"tokenJsonStr":null,"url":"http://weibo.com/u/5144142752","expired":true,"userId":32953014,"id":18574366,"type":2}],"adValid":false,"code":200,"newUser":false,"recallUser":false,"createTime":1407747900967,"createDays":3865,"profileVillageInfo":{"title":"领取村民证","imageUrl":null,"targetUrl":"https://sg.music.163.com/g/cloud-card-3?full_screen=true&nm_style=sbt&market=wode"}}
    val level: Int,
    val listenSongs: Int,
    val profile: UserDetailProfile,
    val code: Int,

    )

data class UserDetailProfile(
    val avatarUrl: String,
    val nickname: String,
    val backgroundUrl: String,
    val userId: Long,
)
