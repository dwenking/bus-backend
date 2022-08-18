import pandas as pd
import json
import datetime

timetableRoutePath = 'timetables.json'
f1 = open("outdata.txt", "w", encoding='utf-8')
timetabledic = {}

with open(timetableRoutePath, 'r', encoding='utf8') as file:
    cnt = 0
    for line in file:  # 遍历一条路线
        lines_json_data = json.loads(line)

        time_table_list = lines_json_data['timetable']
        time_interval_list = []
        pre_time_interval_list = []
        for index, eachtimetable in enumerate(time_table_list):  # 遍历各个班次的时间表
            time_interval_list = []
            for indx, time in enumerate(eachtimetable):  # 取一个班次，遍历这个班次中各个站点的时间
                if indx == 0:
                    continue
                thisrealtime = datetime.datetime.strptime(time, '%H:%M')  # 这个站点经过的时间
                prerealtime = datetime.datetime.strptime(eachtimetable[indx - 1], '%H:%M')  # 前一个站点经过的时间
                # 如果这个站点经过的时间比前一个的时间大，则正常相减并取分钟
                if (thisrealtime > prerealtime):
                    time_interval_list.append(int((thisrealtime - prerealtime).seconds / 60))
                # 特殊情况:两个站点时间跨了一天，则加上一天计算
                else:
                    interval = thisrealtime - prerealtime + datetime.timedelta(days=1)
                    time_interval_list.append(int((interval).seconds / 60))
            # 如果是这个线路的第一个班次，则不进行班次对比，pre中记录上一个班次的时间表，和time进行对比，如果有不相同的时间间隔则记录到cnt中
            if index != 0:
                for indxx, x in enumerate(pre_time_interval_list):
                    if time_interval_list[indxx] != pre_time_interval_list[indxx]:
                        print("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
                        cnt = cnt + 1
                        print(str(indxx) + " " + lines_json_data["name"])
            pre_time_interval_list = time_interval_list
            # print(time_interval_list, file=f1)
        timetabledic["name"] = lines_json_data["name"]
        timetabledic["stationIntervals"] = time_interval_list
        print(timetabledic, file=f1)
        print(timetabledic)
    print(cnt)
