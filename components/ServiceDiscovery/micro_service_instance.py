class MicroserviceInstance:
    VM_PRICE = {
            {1, 1, 1, 1, 1},
            {2, 2, 2, 2, 1}
    }
    def __init__(self, id, user_id, mips, number_of_pes, ram, bw, size, vmm, cloudlet_scheduler, 
                 request_time=0.0, configuration_type=0, purchase_type=0, delay_in_startup=0.0, service_id=0):
        # Initializing attributes
        self.id = id
        self.user_id = user_id
        self.mips = mips
        self.number_of_pes = number_of_pes
        self.ram = ram
        self.bw = bw
        self.size = size
        self.vmm = vmm
        self.cloudlet_scheduler = cloudlet_scheduler
        self.request_time = request_time
        self.configuration_type = configuration_type
        self.purchase_type = purchase_type
        self.delay_in_startup = delay_in_startup
        self.service_id = service_id

        # Default attributes
        self.start_time = -1.0
        self.destroy_time = -1.0
        self.life_time = 0.0
        self.price = MicroserviceInstance.VM_PRICE[purchase_type][configuration_type]
        self.bill = 0.0
        self.status = -1  # Default status
        self.cloudlet_list = []
        self.last_update_time = 0.0

    # Getter and Setter methods
    def get_service_id(self):
        return self.service_id

    def set_service_id(self, service_id):
        self.service_id = service_id

    def get_status(self):
        return self.status

    def set_status(self, status):
        self.status = status

    def get_request_time(self):
        return self.request_time

    def set_request_time(self, request_time):
        self.request_time = request_time

    def get_start_time(self):
        return self.start_time

    def set_start_time(self, start_time):
        self.start_time = start_time

    def get_destroy_time(self):
        return self.destroy_time

    def set_destroy_time(self, destroy_time):
        self.destroy_time = destroy_time

    def get_life_time(self):
        return self.life_time

    def set_life_time(self, life_time):
        self.life_time = life_time

    def get_delay_in_startup(self):
        return self.delay_in_startup

    def set_delay_in_startup(self, delay_in_startup):
        self.delay_in_startup = delay_in_startup

    def get_configuration_type(self):
        return self.configuration_type

    def set_configuration_type(self, configuration_type):
        self.configuration_type = configuration_type

    def get_purchase_type(self):
        return self.purchase_type

    def set_purchase_type(self, purchase_type):
        self.purchase_type = purchase_type

    def get_price(self):
        return self.price  

    def set_price(self, price):
        self.price = price

    def get_bill(self):
        return self.bill

    def set_bill(self, bill):
        self.bill = bill

    def get_cloudlet_list(self):
        return self.cloudlet_list

    def set_cloudlet_list(self, cloudlet_list):
        self.cloudlet_list = cloudlet_list

    def get_last_update_time(self):
        return self.last_update_time

    def set_last_update_time(self, last_update_time):
        self.last_update_time = last_update_time
