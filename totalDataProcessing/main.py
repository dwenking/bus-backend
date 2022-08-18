# coding=utf-8
import os.path
from py2neo import *
from py2neo import Graph, Node, Relationship
import pandas as pd
import json

graph = Graph("bolt://localhost:7687", name="neo4j", password="123456")
matcher_1 = NodeMatcher(graph)
matcher_2 = RelationshipMatcher(graph)

vLinesReadPath = 'vLines.json'
stationsReadPath = 'stations.json'
vRoutesReadPath = 'vRoutes.json'
vTimestableReadPath = 'vTimetables.json'

dic = {'支线': 'Z', '社区线': 'S', '高峰线': 'G', '快速公交': 'K', '干线': 'C', '夜班线': 'N', '城乡线': 'CC', '驳接线': 'B'}
vlines_node_list = []
vstations_nodeid_list = []
vstations_nodeid_reallist = []
vstations_node_list = []
vnames_node_list = []


def create_vLinesNode():
    with open(vLinesReadPath, 'r', encoding='utf8') as file:
        for line in file:
            lines_json_data = json.loads(line)
            directional = lines_json_data['directional']
            interval = lines_json_data['interval']
            kilometer = lines_json_data['kilometer']
            time = 0
            for indx, s in enumerate(lines_json_data['onewayTime']):
                if (s.isdigit()):
                    time = time * 10 + int(s)
            onewayTime = time

            name = lines_json_data['name']
            lineNumber = name
            name = name + "路"
            # if lines_json_data['name']:
            #     name = lines_json_data['name'] + "路"
            type = dic[lines_json_data['type']]
            mrk = False
            runtime=lines_json_data['runtime']
            route=lines_json_data['route']

            if lines_json_data['directional']:
                name1 = name + "上行"
                name2 = name + "下行"
                node = Node('vLines', name=name1, lineNumber=lineNumber, directional=directional, kilometer=kilometer,
                            interval=interval,
                            runtime=runtime,
                            route=route,

                             onewayTime=onewayTime, type=type)
                vlines_node_list.append(node)
                node = Node('vLines', name=name2, lineNumber=lineNumber, directional=directional, kilometer=kilometer,
                            interval=interval,
                            route=route,
                            runtime=runtime, onewayTime=onewayTime, type=type)
                vlines_node_list.append(node)
            else:
                node = Node('vLines', name=name, lineNumber=lineNumber, directional=directional, kilometer=kilometer,
                            interval=interval,route=route,
                            runtime=runtime, onewayTime=onewayTime, type=type)
                vlines_node_list.append(node)

        for eachnode in vlines_node_list:
            graph.create(eachnode)


def calculate_vStationId():
    with open(vRoutesReadPath, 'r', encoding='utf8') as file:
        for line in file:
            routes_json_data = json.loads(line)
            for stationid in routes_json_data['alongStation']:
                vstations_nodeid_list.append(stationid)


def create_vStationNode():

    with open(stationsReadPath, 'r', encoding='utf8') as file:
        for line in file:
            station_json_data = json.loads(line)
            # print(station_json_data['id'])
            if station_json_data['name'].startswith('地铁'):
                type = 'metro'
            elif station_json_data['name'].startswith('火车西站') or station_json_data['name'].startswith('金河南站'):
                type = 'train'
            elif station_json_data['name'].startswith('金河客运站') or station_json_data['name'].startswith('北客站'):
                type = 'bus'
            else:
                type = 'normal'
            englishname = station_json_data['english']
            stationId = station_json_data['id']
            name = station_json_data['name']
            node = Node('vStations', myId=stationId, name=name, englishname=englishname, type=type)
            vstations_node_list.append(node)
    for eachnode in vstations_node_list:
        # print(eachnode)
        graph.create(eachnode)


def create_vNamesNode():
    name_list = []
    with open(stationsReadPath, 'r', encoding='utf8') as file:
        for line in file:
            station_json_data = json.loads(line)
                # 是否是始发站或终点站
            if station_json_data['name'].find('始发站') != -1 or station_json_data['name'].find('终点站') != -1:
                name = station_json_data['name'][:-5]
                print(name+"!")
                name_list.append(name)
            else:
                print(station_json_data['name'])
                name_list.append(station_json_data['name'])
    name_reallist = list(set(name_list))
    print(len(name_reallist))
    for name in name_reallist:
        node = Node('vNames', name=name)
        vnames_node_list.append(node)
        # print(node)
        graph.create(node)


def createName_Station_Relationship():
    match_allnames_node = matcher_1.match("vNames")
    res_names_node = list(match_allnames_node)

    for namenode in res_names_node:
        # print(namenode['name'])
        name = namenode['name']
        name1 = name + "(终点站)"
        name2 = name + "(始发站)"
        match_allstations_node = matcher_1.match("vStations", name=name)
        res_stations_node = list(match_allstations_node)
        if len(res_stations_node) > 0:
            for eachnode in res_stations_node:
                relationship = Relationship(eachnode, "in", namenode)
                graph.create(relationship)
        match_allstations_node = matcher_1.match("vStations", name=name1)
        res_stations_node = list(match_allstations_node)
        if len(res_stations_node) > 0:
            for eachnode in res_stations_node:
                relationship = Relationship(eachnode, "end", namenode)
                graph.create(relationship)
        match_allstations_node = matcher_1.match("vStations", name=name2)
        res_stations_node = list(match_allstations_node)
        if len(res_stations_node) > 0:
            for eachnode in res_stations_node:
                relationship = Relationship(eachnode, "begin", namenode)
                graph.create(relationship)


def createStations_Lines_Relationship():
    with open(vRoutesReadPath, 'r', encoding='utf8') as file:
        for line in file:
            lines_json_data = json.loads(line)
            match_thisline_node = matcher_1.match("vLines", name=lines_json_data['name'])
            res_thislines_node = list(match_thisline_node)
            lines_nodeid_list = lines_json_data['alongStation']
            # print(lines_nodeid_list)
            for indx, id in enumerate(lines_nodeid_list):
                # print(id)
                match_thisstation_node = matcher_1.match("vStations", myId=id)
                res_thisstation_node = list(match_thisstation_node)
                # print(res_thisstation_node)
                if indx == 0:
                    relationship = Relationship(res_thisstation_node[0], "begin", res_thislines_node[0])
                elif indx == len(lines_nodeid_list) - 1:
                    relationship = Relationship(res_thisstation_node[0], "end", res_thislines_node[0])
                else:
                    relationship = Relationship(res_thisstation_node[0], "in", res_thislines_node[0])
                # print(relationship)
                graph.create(relationship)


def createStation_StationRelationship():
    lines_dict = {}
    # linesname_list = []
    # with open(vRoutesReadPath, 'r', encoding='utf8') as file:
    #     for line in file:
    #         lines_json_data = json.loads(line)
    #         linesname_list.append(lines_json_data["name"])
    # print(linesname_list)
    lines_intervals_dic = {}
    with open(vTimestableReadPath, 'r', encoding='utf8') as file:
        for line in file:
            lines_json_data = json.loads(line)
            lines_intervals_dic[lines_json_data["name"]] = lines_json_data["stationIntervals"]
            # print(lines_json_data["stationIntervals"])
    with open(vRoutesReadPath, 'r', encoding='utf8') as file:
        for line in file:
            lines_json_data = json.loads(line)
            line_node = matcher_1.match("vLines", name=lines_json_data["name"])
            thisline_node_list = list(line_node)
            thisline_node = thisline_node_list[0]
            stationIntervals = lines_intervals_dic[lines_json_data["name"]]
            alongStation = lines_json_data["alongStation"]
            for indx, stationId in enumerate(alongStation):
                if indx == 0:
                    continue
                node1 = matcher_1.match("vStations", myId=alongStation[indx - 1])
                node2 = matcher_1.match("vStations", myId=stationId)
                res_node1 = list(node1)
                res_node2 = list(node2)
                # print(res_node1[0]["myId"] + " " + res_node2[0]["myId"])
                defaultweight = 1
                cql = "MATCH (from:vStations{myId:\'" + str(res_node1[0]["myId"]) + "\'})," + \
                      "(to:vStations{myId:\'" + str(res_node2[0]["myId"]) + "\'}) " + \
                      "MERGE(from)-[r:vNEAR{name: \'" + str(lines_json_data["name"]) + "\',lineNumber:\'" + str(
                    thisline_node["lineNumber"]) + \
                      "\',time:" + str(stationIntervals[indx - 1]) + \
                      ",weight:" + str(defaultweight) + "}]->(to)"

                # properties = {"name": lines_json_data["name"], "lineNumber": thisline_node["lineNumber"],
                #               "time": stationIntervals[indx - 1], "weight": defaultweight}
                # relationship = Relationship(res_node1[0], "vNear", res_node2[0], **properties)
                # graph.create(relationship)
                graph.run(cql)


if __name__ == '__main__':
    cql="match (n:vLines),(m:vNames),(k:vStations) detach delete n,m,k"
    graph.run(cql)
    print("create_vLinesNode")
    create_vLinesNode()
    # print("calculate_vStationId")
    # calculate_vStationId()
    print("create_vStationNode")
    create_vStationNode()
    print("create_vNamesNode")
    create_vNamesNode()
    print("createName_Station_Relationship")
    createName_Station_Relationship()
    print("createStations_Lines_Relationship")
    createStations_Lines_Relationship()
    print("createStation_StationRelationship")
    createStation_StationRelationship()
