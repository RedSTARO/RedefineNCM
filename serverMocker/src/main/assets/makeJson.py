import requests
import json
import os

# 直接用字符串构造 Cookie 头
headers = {
    "Cookie":
    ""
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
    downloadPath = os.path.expandvars(r"%USERPROFILE%\Downloads\RedefineNCMServerMocker")
    os.makedirs(downloadPath, exist_ok=True)
    with open(f"{downloadPath}/{api.replace("/", "_").split("?")[0]}.json", "w", encoding="utf-8") as f:
        json.dump(response_json, f, ensure_ascii=False, indent=4)

    # 定义一个递归函数来修改字符串字段和列表处理
    def modify_values(obj):
        if isinstance(obj, dict):
            for key, value in obj.items():
                # 如果值是字符串，修改它
                if isinstance(value, str):
                    obj[key] = "AStringValue"
                elif isinstance(value, bool):
                    obj[key] = False
                elif isinstance(value, int):
                    if key != "code":
                        obj[key] = 111111
                # 如果值是列表，修改为只保留3项
                elif isinstance(value, list):
                    if value:
                        obj[key] = [modify_values(value[i]) for i in range(len(value) )if i < 3]  # 保留3项
                    else:
                        obj[key] = []  # 空列表保持为空
                # 递归处理字典
                else:
                    obj[key] = modify_values(value)
        elif isinstance(obj, list):
            if obj:
                return [modify_values(obj[i]) for i in range(len(obj) ) if i < 3]   # 只保留3项
            else:
                return []  # 空列表保持为空
        return obj

    # 修改 JSON 数据中的字段
    modified_json = modify_values(response_json)

    # 将修改后的内容保存到独立的 JSON 文件
    with open(f"{api.replace("/", "_").split("?")[0]}.json", "w", encoding="utf-8") as f:
        json.dump(modified_json, f, ensure_ascii=False, indent=4)

    print(urlBase)
    print(f"修改后的数据已经保存到 {api.replace("/", "_")}.json")

spApis = [f"/login/qr/create?key=5606f361-1708-49aa-a201-c966bcde8cac&qrimg=true", f"/login/qr/check?key=5606f361-1708-49aa-a201-c966bcde8cac"]

apis = ["/user/account", f"/user/detail?uid={uid}", f"/login/status?cookie={headers["Cookie"]}",
        "/login/qr/key", f"/daily_signin", f"/user/playlist?uid={uid}", f"/playlist/track/all?id={songlistid}", f"/playlist/detail?id={songlistid}", f"/song/url/v1?id={songid}&level=standard",
        f"/song/detail?id={songid}", f"/lyric?id={songid}", f"/inner/version", "/playlist/update/playcount", "/recommend/resource", "/recommend/songs",
        f"/likelist?uid={uid}", f"/like?id={songid}"]
# for i in apis:
#     main(i)

main(apis[-1])