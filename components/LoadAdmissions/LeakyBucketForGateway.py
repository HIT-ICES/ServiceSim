import json


class LeakyBucketForGateway:
    def __init__(self, device_id, tokens, capacities, admission_rate):
        self.device_id = device_id
        self.tokens = tokens
        self.capacities = capacities
        self.admission_rate = admission_rate
        self.leakybucket_history_file = []

        self.last_minute = 0
        self.last_time = 0.0
        self.admission_num = capacities.copy()
        self.init_admission_num()

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
        tokens = {tuple(map(int, k.strip("()").split(", "))): v for k, v in data["tokens"].items()}
        capacities = {tuple(map(int, k.strip("()").split(", "))): v for k, v in data["capacities"].items()}
        admission_rate = {tuple(map(int, k.strip("()").split(", "))): v for k, v in data["admission_rate"].items()}

        return cls(
            device_id=data["device_id"],
            tokens=tokens,
            capacities=capacities,
            admission_rate=admission_rate
        )

    def to_json(self):
        """
        将实例转换为 JSON 格式的字符串。
        """
        # 将元组键转换为字符串
        tokens = {str(k): v for k, v in self.tokens.items()}
        capacities = {str(k): v for k, v in self.capacities.items()}
        admission_rate = {str(k): v for k, v in self.admission_rate.items()}

        # 构建 JSON 数据
        data = {
            "device_id": self.device_id,
            "tokens": tokens,
            "capacities": capacities,
            "admission_rate": admission_rate
        }

        # 转换为 JSON 字符串并返回
        return json.dumps(data, indent=4)

    def init_admission_num(self):
        for key in self.admission_num.keys():
            self.admission_num[key] = 0

    def write_admission_executor_result(self, this_minute):
        for key, num in self.admission_num.items():
            self.leakybucket_history_file.append([this_minute, self.device_id, key[0], key[1], num])

    def update_tokens(self, simulator_time):
        current_time = simulator_time
        for key in self.tokens.keys():
            if key in self.admission_rate and key in self.capacities:
                time_diff = (current_time - self.last_time) / 60.0
                new_tokens = min(self.capacities[key], self.tokens[key] + time_diff * self.admission_rate[key])
                self.tokens[key] = new_tokens
            else:
                print("Error - Cannot find keys for tokens or capacities.")
        self.last_time = current_time

    def is_admission(self, network_packet, simulator_time):
        if network_packet['destination_service_id'] != 0:
            return True
        else:
            this_minute = int(simulator_time / 60)
            if this_minute != self.last_minute:
                self.write_admission_executor_result(this_minute)
                self.init_admission_num()
                self.last_minute = this_minute

            self.update_tokens(simulator_time)
            service_chain_id = network_packet['service_chain_id']
            user_level = network_packet['user_level']
            key = (service_chain_id, user_level)

            if key in self.tokens:
                if self.tokens[key] >= 1:
                    self.tokens[key] -= 1
                    self.add_admission_num(key)
                    return True
            else:
                self.add_admission_num(key)
                return True
        return False

    def add_admission_num(self, key):
        if key in self.admission_num:
            self.admission_num[key] += 1
