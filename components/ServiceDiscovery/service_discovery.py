import json
from micro_service_instance import MicroserviceInstance 
class ServiceDiscovery:
    def __init__(self):
        # 使用嵌套字典来模拟服务发现信息
        self.service_id_to_instance_list = {}  # serviceId -> { deviceId -> [instanceId] }
        self.service0_to_instance_list = {}  # serviceChainId -> { deviceId -> [instanceId] }
    
    @classmethod
    def from_json(cls, json_data):
        # 初始化实例
        instance = cls()
        
        # 解析 JSON 数据
        data = json.loads(json_data) if isinstance(json_data, str) else json_data
        
        # 解析 service_id_to_instance_list 和 service0_to_instance_list
        instance.service_id_to_instance_list = {
            int(service_id): {int(device_id): instances 
                              for device_id, instances in device_dict.items()}
            for service_id, device_dict in data["serviceIdToInstanceList"].items()
        }
        
        instance.service0_to_instance_list = {
            int(service_chain_id): {int(device_id): instances 
                                    for device_id, instances in device_dict.items()}
            for service_chain_id, device_dict in data["service0ToInstanceList"].items()
        }
        
        return instance

    def to_json(self):
        # 将类的属性转换为 JSON 格式
        data = {
            "serviceIdToInstanceList": {
                str(service_id): {str(device_id): instances 
                                  for device_id, instances in device_dict.items()}
                for service_id, device_dict in self.service_id_to_instance_list.items()
            },
            "service0ToInstanceList": {
                str(service_chain_id): {str(device_id): instances 
                                        for device_id, instances in device_dict.items()}
                for service_chain_id, device_dict in self.service0_to_instance_list.items()
            }
        }
        return json.dumps(data, indent=4)
    
    def get_service_id_to_instance_list(self):
        return self.service_id_to_instance_list

    def set_service_id_to_instance_list(self, service_id_to_instance_list):
        self.service_id_to_instance_list = service_id_to_instance_list

    def get_service0_to_instance_list(self):
        return self.service0_to_instance_list

    def set_service0_to_instance_list(self, service0_to_instance_list):
        self.service0_to_instance_list = service0_to_instance_list

    # 添加服务发现信息
    def add_service_discovery_info(self, microservice_instance):
        service_id = microservice_instance.get_service_id()
        device_id = microservice_instance.get_host().get_datacenter().get_id()
        
        if service_id < 0 or device_id < 0:
            print("ServiceDiscovery: addServiceDiscoveryInfo error in serviceId or deviceId")
            return
        
        # 添加到 service_id_to_instance_list
        if service_id not in self.service_id_to_instance_list:
            self.service_id_to_instance_list[service_id] = {}
        
        if device_id not in self.service_id_to_instance_list[service_id]:
            self.service_id_to_instance_list[service_id][device_id] = []
        
        if microservice_instance.get_id() not in self.service_id_to_instance_list[service_id][device_id]:
            self.service_id_to_instance_list[service_id][device_id].append(microservice_instance.get_id())

        # 如果 serviceId 是 0，更新 service0_to_instance_list
        if service_id == 0:
            service_chain_id = microservice_instance.service_chain_id
            if service_chain_id not in self.service0_to_instance_list:
                self.service0_to_instance_list[service_chain_id] = {}
            
            if device_id not in self.service0_to_instance_list[service_chain_id]:
                self.service0_to_instance_list[service_chain_id][device_id] = []
            
            if microservice_instance.get_id() not in self.service0_to_instance_list[service_chain_id][device_id]:
                self.service0_to_instance_list[service_chain_id][device_id].append(microservice_instance.get_id())

    # 移除服务发现信息
    def remove_service_discovery_info(self, microservice_instance):
        service_id = microservice_instance.get_service_id()
        device_id = microservice_instance.get_host().get_datacenter().get_id()

        # 从 service_id_to_instance_list 中移除
        if service_id in self.service_id_to_instance_list:
            if device_id in self.service_id_to_instance_list[service_id]:
                if microservice_instance.get_id() in self.service_id_to_instance_list[service_id][device_id]:
                    self.service_id_to_instance_list[service_id][device_id].remove(microservice_instance.get_id())

        # 如果 serviceId 是 0，从 service0_to_instance_list 中移除
        if service_id == 0:
            service_chain_id = microservice_instance.service_chain_id
            if service_chain_id in self.service0_to_instance_list:
                if device_id in self.service0_to_instance_list[service_chain_id]:
                    if microservice_instance.get_id() in self.service0_to_instance_list[service_chain_id][device_id]:
                        self.service0_to_instance_list[service_chain_id][device_id].remove(microservice_instance.get_id())
    