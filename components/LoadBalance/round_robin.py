import json
class RoundRobin:
    def __init__(self):
        self.position = {}  # serviceId -> instanceId position
        self.service0_position = {}  # serviceChainId -> instanceId position

    @classmethod
    def from_json(cls, json_data):
        """
        从 JSON 字符串或 JSON 文件内容初始化实例。
        参数：
            - json_data：JSON 格式的字符串或字典对象。
        """
        if isinstance(json_data, str):
            # 将 JSON 字符串转换为字典
            data = json.loads(json_data)
        elif isinstance(json_data, dict):
            # 直接使用字典
            data = json_data
        else:
            raise ValueError("Invalid JSON data. Must be a JSON string or dictionary.")

        # 转换 JSON 中的字符串键为元组键
        position = {int(k): v for k, v in data["position"].items()}
        service0_position = {int(k): v for k, v in data["service0Position"].items()}

        return cls(
            position=position,
            service0_position=service0_position
        )

    def to_json(self):
        """
        将实例转换为 JSON 格式的字符串。
        """
        # Prepare a dictionary representation of the instance
        data = {
            "position": {str(k): v for k, v in self.position.items()},
            "service0Position": {str(k): v for k, v in self.service0_position.items()}
        }
        # Convert to JSON string
        return json.dumps(data, indent=4)

    def find_instance_id(self, service_discovery, service_id, device_id):
        if service_id in self.position and service_id in service_discovery.get_service_id_to_instance_list():
            if device_id in service_discovery.get_service_id_to_instance_list()[service_id]:
                instances = service_discovery.get_service_id_to_instance_list()[service_id][device_id]
                if len(instances) == 0:
                    print("Service Discovery Information Missing")
                    return -1
                pos = self.position[service_id]
                pos = 0 if pos + 1 > len(instances) - 1 else pos + 1
                self.position[service_id] = pos
                return instances[pos]
            else:
                print("Service Discovery Information Missing")
                return -1
        else:
            if service_id in service_discovery.get_service_id_to_instance_list():
                if device_id in service_discovery.get_service_id_to_instance_list()[service_id]:
                    instances = service_discovery.get_service_id_to_instance_list()[service_id][device_id]
                    if len(instances) == 0:
                        print("Service Discovery Information Missing")
                        return -1
                    self.position[service_id] = 0
                    return instances[0]
                else:
                    print("Service Discovery Information Missing")
                    return -1
            print("Service Discovery Information Missing")
            return -1

    def find_service0_instance_id(self, service_discovery, service_chain_id, device_id):
        if service_chain_id in self.service0_position and service_chain_id in service_discovery.get_service0_to_instance_list():
            if device_id in service_discovery.get_service0_to_instance_list()[service_chain_id]:
                instances = service_discovery.get_service0_to_instance_list()[service_chain_id][device_id]
                if len(instances) == 0:
                    print("Service Discovery Information Missing")
                    return -1
                pos = self.service0_position[service_chain_id]
                pos = 0 if pos + 1 > len(instances) - 1 else pos + 1
                self.service0_position[service_chain_id] = pos
                return instances[pos]
            else:
                print("Service Discovery Information Missing")
                return -1
        else:
            if service_chain_id in service_discovery.get_service0_to_instance_list():
                if device_id in service_discovery.get_service0_to_instance_list()[service_chain_id]:
                    instances = service_discovery.get_service0_to_instance_list()[service_chain_id][device_id]
                    if len(instances) == 0:
                        print("Service Discovery Information Missing")
                        return -1
                    self.service0_position[service_chain_id] = 0
                    return instances[0]
                else:
                    print("Service Discovery Information Missing")
                    return -1
            print("Service Discovery Information Missing")
            return -1
