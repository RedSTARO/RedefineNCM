import requests
import json

# 直接用字符串构造 Cookie 头
headers = {
    "Cookie":
        "MUSIC_R_T=1580374122647; Max-Age=2147483647; Expires=Mon, 13 Apr 2093 08:57:58 GMT; Path=/wapi/feedback; HTTPOnly;MUSIC_R_T=1580374122647; Max-Age=2147483647; Expires=Mon, 13 Apr 2093 08:57:58 GMT; Path=/weapi/feedback; HTTPOnly;MUSIC_A_T=1580374122611; Max-Age=2147483647; Expires=Mon, 13 Apr 2093 08:57:58 GMT; Path=/openapi/clientlog; HTTPOnly;NMTID=00OFZGjbf886AlzpkS6kbmsmM3bfdsAAAGV0Pp67g; Max-Age=315360000; Expires=Sat, 24 Mar 2035 05:43:51 GMT; Path=/;;MUSIC_A_T=1580374122611; Max-Age=2147483647; Expires=Mon, 13 Apr 2093 08:57:58 GMT; Path=/eapi/clientlog; HTTPOnly;MUSIC_A_T=1580374122611; Max-Age=2147483647; Expires=Mon, 13 Apr 2093 08:57:58 GMT; Path=/api/feedback; HTTPOnly;MUSIC_A_T=1580374122611; Max-Age=2147483647; Expires=Mon, 13 Apr 2093 08:57:58 GMT; Path=/wapi/clientlog; HTTPOnly;MUSIC_A_T=1580374122611; Max-Age=2147483647; Expires=Mon, 13 Apr 2093 08:57:58 GMT; Path=/wapi/feedback; HTTPOnly;MUSIC_A_T=1580374122611; Max-Age=2147483647; Expires=Mon, 13 Apr 2093 08:57:58 GMT; Path=/neapi/clientlog; HTTPOnly;MUSIC_R_T=1580374122647; Max-Age=2147483647; Expires=Mon, 13 Apr 2093 08:57:58 GMT; Path=/wapi/clientlog; HTTPOnly;MUSIC_R_T=1580374122647; Max-Age=2147483647; Expires=Mon, 13 Apr 2093 08:57:58 GMT; Path=/eapi/feedback; HTTPOnly;MUSIC_A_T=1580374122611; Max-Age=2147483647; Expires=Mon, 13 Apr 2093 08:57:58 GMT; Path=/weapi/feedback; HTTPOnly;__csrf=149fefd5255127cc80153686175a7d20; Max-Age=1296010; Expires=Thu, 10 Apr 2025 05:44:01 GMT; Path=/;;MUSIC_R_T=1580374122647; Max-Age=2147483647; Expires=Mon, 13 Apr 2093 08:57:58 GMT; Path=/api/clientlog; HTTPOnly;MUSIC_A_T=1580374122611; Max-Age=2147483647; Expires=Mon, 13 Apr 2093 08:57:58 GMT; Path=/api/clientlog; HTTPOnly;MUSIC_R_T=1580374122647; Max-Age=2147483647; Expires=Mon, 13 Apr 2093 08:57:58 GMT; Path=/api/feedback; HTTPOnly;MUSIC_R_T=1580374122647; Max-Age=2147483647; Expires=Mon, 13 Apr 2093 08:57:58 GMT; Path=/neapi/clientlog; HTTPOnly;MUSIC_A_T=1580374122611; Max-Age=2147483647; Expires=Mon, 13 Apr 2093 08:57:58 GMT; Path=/eapi/feedback; HTTPOnly;MUSIC_R_T=1580374122647; Max-Age=2147483647; Expires=Mon, 13 Apr 2093 08:57:58 GMT; Path=/weapi/clientlog; HTTPOnly;MUSIC_A_T=1580374122611; Max-Age=2147483647; Expires=Mon, 13 Apr 2093 08:57:58 GMT; Path=/weapi/clientlog; HTTPOnly;MUSIC_SNS=; Max-Age=0; Expires=Wed, 26 Mar 2025 05:43:51 GMT; Path=/;MUSIC_R_T=1580374122647; Max-Age=2147483647; Expires=Mon, 13 Apr 2093 08:57:58 GMT; Path=/eapi/clientlog; HTTPOnly;MUSIC_R_T=1580374122647; Max-Age=2147483647; Expires=Mon, 13 Apr 2093 08:57:58 GMT; Path=/openapi/clientlog; HTTPOnly;MUSIC_U=007BFEE46C759D6054ED832E35180941956F84B6A955D8BB9FB74672868D97CA42B9879AF52975DD5B97FB4CA9A36EA025A4DE96607A74EDBDA928DEE1B72405A73F63DF19797961F2247CE7082D4F100F6DB5BD3828F2C218B41E9E8922021EF19D444B158592BF6124B7C07F31B6BBA91BB7D84F27680366980D287EFC6482DE228DF28995564EA277390296EEE33C191E31C99524025BBAEC2724F93D85413530D26FA90BC704AEDBE45DBEA1A188A545D6294D407FF4DDC4300E1E8020885F7A30BD5F56C6EB79D6187A40BA26A939611678F09E3E94127088DDF47F35182C451C688EB008DA03191B5D5AA689858AA10E6845F610D03E278D92B8F66C84DF89B841FBA52F97E9B27FFEF5024A35423BA042058EB303F082BD6D01FA44F33B39B68CB68B215059EFD9604349C1F7BD; Max-Age=15552000; Expires=Mon, 22 Sep 2025 05:43:51 GMT; Path=/; HTTPOnly;MUSIC_A_T=1580374122611; Max-Age=2147483647; Expires=Mon, 13 Apr 2093 08:57:58 GMT; Path=/neapi/feedback; HTTPOnly;MUSIC_R_T=1580374122647; Max-Age=2147483647; Expires=Mon, 13 Apr 2093 08:57:58 GMT; Path=/neapi/feedback; HTTPOnly"
}

# 查询参数
params = {
    "realIP": "192.168.1.1"
}

uid = 2131348937
songlistid = 12638852128
songid = 211277

def main(api):
    urlBase = "https://ncm.tryagain.fun" + api
    # 发送请求
    response = requests.get(urlBase, params=params, headers=headers)
    print(response.text)

    # 解析返回的 JSON
    response_json = response.json()

    # 定义一个递归函数来修改字符串字段和列表处理
    def modify_values(obj):
        if isinstance(obj, dict):
            for key, value in obj.items():
                # 如果值是字符串，修改它
                if isinstance(value, str):
                    obj[key] = "AStringValue"
                elif isinstance(value, int):
                    obj[key] = 111111
                # 如果值是列表，修改为只保留第一项
                elif isinstance(value, list):
                    if value:
                        obj[key] = [modify_values(value[0])]  # 保留第一项
                    else:
                        obj[key] = []  # 空列表保持为空
                # 递归处理字典
                else:
                    obj[key] = modify_values(value)
        elif isinstance(obj, list):
            if obj:
                return [modify_values(obj[0])]  # 只保留第一项
            else:
                return []  # 空列表保持为空
        return obj

    # 修改 JSON 数据中的字段
    modified_json = modify_values(response_json)

    # 将修改后的内容保存到独立的 JSON 文件
    with open(f"{api.replace("/", "_").replace("?", "_").split("=")[0]}.json", "w", encoding="utf-8") as f:
        json.dump(modified_json, f, ensure_ascii=False, indent=4)

    print(urlBase)
    print(f"修改后的数据已经保存到 {api.replace("/", "_")}.json")

apis = ["/user/account", f"/user/detail?uid={uid}", f"/login/status?cookie={headers["Cookie"]}",
        "/login/qr/key", f"/login/qr/create?key=5606f361-1708-49aa-a201-c966bcde8cac&qrimg=true", f"/login/qr/check?key=5606f361-1708-49aa-a201-c966bcde8cac",
        "/daily_signin", f"/user/playlist?uid={uid}", f"/playlist/track/all?id={songlistid}", f"/playlist/detail?id={songlistid}", f"/song/url/v1?id={songid}&level=standard",
        f"/song/detail?id={songid}", f"/lyric?id={songid}", f"/inner/version"]
for i in apis:
    main(i)