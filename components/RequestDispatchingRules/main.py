from flask import Flask, request, jsonify, make_response
from RequestDispatchingSimple import RequestDispatchingSimple
from flasgger import Swagger

app = Flask(__name__)
swagger = Swagger(app)  # 初始化 Swagger: http://localhost/apidocs

requestDispatchingSimple = RequestDispatchingSimple()

@app.route('/request-dispatching-simple/find-device-id', methods=['POST'])
def request_dispatching_simple_find_device_id():
    """
    Check which device/cluster the request should be dispatched based on Request Dispatching rule.
    ---
    tags:
      - Request Dispatching Simple
    parameters:
      - name: this_device_id
        in: body
        required: true
        schema:
          type: int
      - name: request_can_dispatch_list
        in: body
        required: true
        schema:
          type: object
      - name: child_device_ids
        in: body
        required: true
        schema:
          type: list
      - name: parent_device_ids
        in: body
        required: true
        schema:
          type: list
      - name: same_level_device_ids
        in: body
        required: true
        schema:
          type: list
    responses:
      200:
        description: Request dispatching result
        schema:
          type: object
          properties:
            device_id:
              type: int
              example: 1
    """
    data = request.get_json()
    this_device_id = data["this_device_id"]
    request_can_dispatch_list = data["request_can_dispatch_list"]
    child_device_ids = data["child_device_ids"]
    parent_device_ids = data["parent_device_ids"]
    same_level_device_ids = data["same_level_device_ids"]
    result = requestDispatchingSimple.find_device_id(this_device_id, request_can_dispatch_list, child_device_ids, parent_device_ids, same_level_device_ids)
    return jsonify({'device_id': result})


@app.route('/request-dispatching-simple/@config', methods=['POST'])
def request_dispatching_simple__set_conf():
    """
    Set a new configuration for the Request Dispatching.
    ---
    tags:
      - Request Dispatching Simple
    parameters:
      - name: configuration
        in: body
        required: true
        schema:
          type: object
    responses:
      201:
        description: Configuration updated
    """
    global requestDispatchingSimple
    requestDispatchingSimple = requestDispatchingSimple.from_json(request.get_json())
    return make_response('', 201)

@app.route('/request-dispatching-simple/@config', methods=['GET'])
def request_dispatching_simple__get_conf():
    """
    Get the current configuration of the Request Dispatching.
    ---
    tags:
      - Request Dispatching Simple
    responses:
      200:
        description: Current configuration
        schema:
          type: object
    """
    return make_response(requestDispatchingSimple.to_json(), 200)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=80)