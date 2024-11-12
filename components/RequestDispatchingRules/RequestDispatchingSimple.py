import json
import random


class RequestDispatchingSimple:
    def __init__(self):
        self.network_devices = []

    @classmethod
    def from_json(cls, json_data):
        """
        从 JSON 字符串或 JSON 文件内容初始化实例。
        参数：
            - json_data：JSON 格式的字符串或字典对象。
        """
        if isinstance(json_data, str):
            # 将 JSON 字符串转换为列表
            data = json.loads(json_data)
        else:
            raise ValueError("Invalid JSON data. Must be a JSON string.")

        return cls(
            network_devices=data
        )

    def to_json(self):
        """
        将实例转换为 JSON 格式的字符串。
        """

        # 转换为 JSON 字符串并返回
        return json.dumps(self.network_devices, indent=4)


    def find_device_id(self, this_device_id, request_can_dispatch_list, child_device_ids, parent_device_ids, same_level_device_ids):
        if not request_can_dispatch_list:
            print("Error - Service Discovery Information Missing or there is no service instance available.")
            return -1

        # 1. check self
        key_to_check = str(this_device_id)
        if key_to_check in request_can_dispatch_list:
            if len(request_can_dispatch_list[key_to_check]) > 0:
                return this_device_id

        # 2. check same level devices
        available_device_ids = []
        for device_id in same_level_device_ids:
            key_to_check = str(device_id)
            if key_to_check in request_can_dispatch_list:
                if len(request_can_dispatch_list[key_to_check]) > 0:
                    available_device_ids.append(device_id)

        if len(available_device_ids) > 0:
            return random.choice(available_device_ids)

        # 3. check child devices
        available_device_ids = []
        for device_id in child_device_ids:
            key_to_check = str(device_id)
            if key_to_check in request_can_dispatch_list:
                if len(request_can_dispatch_list[key_to_check]) > 0:
                    available_device_ids.append(device_id)

        if len(available_device_ids) > 0:
            return random.choice(available_device_ids)

        # 4. check parent devices
        available_device_ids = []
        for device_id in parent_device_ids:
            key_to_check = str(device_id)
            if key_to_check in request_can_dispatch_list:
                if len(request_can_dispatch_list[key_to_check]) > 0:
                    available_device_ids.append(device_id)

        if len(available_device_ids) > 0:
            return random.choice(available_device_ids)

        # 5. check parents' children
        available_device_ids = []
        for parent_device_id in parent_device_ids:
            for network_device in self.network_devices:
                if network_device["id"] == parent_device_id:
                    for device_id in network_device["child_device_ids"]:
                        key_to_check = str(device_id)
                        if key_to_check in request_can_dispatch_list:
                            if len(request_can_dispatch_list[key_to_check]) > 0:
                                available_device_ids.append(device_id)

        if len(available_device_ids) > 0:
            return random.choice(available_device_ids)


        # 6. check cloud
        available_device_ids = []
        for network_device in self.network_devices:
            if network_device["identity"] == "cloud":
                key_to_check = str(network_device["id"])
                if key_to_check in request_can_dispatch_list:
                    if len(request_can_dispatch_list[key_to_check]) > 0:
                        available_device_ids.append(device_id)

        if len(available_device_ids) > 0:
            return random.choice(available_device_ids)

        print("Error - Service Discovery Information Missing or there is no service instance available.")
        return -1
