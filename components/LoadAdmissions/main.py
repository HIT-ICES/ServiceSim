from flask import Flask, request, jsonify, make_response
from LeakyBucketForGateway import LeakyBucketForGateway
from ServiceSimComponentsShared.InstanceManager import InstanceManager
from flasgger import Swagger

app = Flask(__name__)
swagger = Swagger(app)  # 初始化 Swagger: http://localhost/apidocs

leakyBucketForGateways = InstanceManager[LeakyBucketForGateway](lambda: LeakyBucketForGateway(
    device_id=1,
    tokens={(1, 1): 10.0, (2, 1): 5.0},
    capacities={(1, 1): 10, (2, 1): 5},
    admission_rate={(1, 1): 1, (2, 1): 1}
))


@app.route('/non-admission/is-admission', methods=['POST'])
def non_load_admission_is_admission():
    """
    Check if the request can be admitted without loading Leaky Bucket configuration.
    ---
    tags:
      - Non-Admission
    responses:
      200:
        description: Admission status
        schema:
          type: object
          properties:
            admission:
              type: boolean
              example: true
    """
    return jsonify({'admission': True})


@app.route('/leak-bucket-for-gateway/<string:session_id>/is-admission', methods=['POST'])
def leak_bucket_for_gateway_is_admission(session_id):
    """
    Check if the request can be admitted based on Leaky Bucket configuration.
    ---
    tags:
      - Leaky Bucket
    parameters:
      - name: network_packet
        in: body
        required: true
        schema:
          type: object
      - name: simulator_time
        in: body
        required: true
        schema:
          type: double
    responses:
      200:
        description: Admission result
        schema:
          type: object
          properties:
            admission:
              type: boolean
              example: true
    """
    data = request.get_json()
    network_packet = data["network_packet"]
    simulator_time = data["simulator_time"]
    result = leakyBucketForGateways[session_id].is_admission(network_packet, simulator_time)
    return jsonify({'admission': result})


@app.route('/leak-bucket-for-gateway/<string:session_id>/@history', methods=['GET'])
def leak_bucket_for_gateway__get_history(session_id):
    """
    Retrieve the history of the Leaky Bucket.
    ---
    tags:
      - Leaky Bucket
    responses:
      200:
        description: Leaky Bucket history
        schema:
          type: array
          items:
            type: object
    """
    return jsonify(leakyBucketForGateways[session_id].leakybucket_history_file)


@app.route('/leak-bucket-for-gateway/<string:session_id>/@history', methods=['DELETE'])
def leak_bucket_for_gateway__clear_history(session_id):
    """
    Clear the history of the Leaky Bucket.
    ---
    tags:
      - Leaky Bucket
    responses:
      204:
        description: History cleared
    """
    leakyBucketForGateways[session_id].leakybucket_history_file = []
    return make_response('', 204)


@app.route('/leak-bucket-for-gateway/<string:session_id>', methods=['GET'])
def leak_bucket_for_gateway__get_conf(session_id):
    """
    Get the current configuration of the Leaky Bucket.
    ---
    tags:
      - Leaky Bucket
    responses:
      200:
        description: Current configuration
        schema:
          type: object
    """
    return make_response(leakyBucketForGateways[session_id].to_json(), 200)


@app.route('/leak-bucket-for-gateway/<string:session_id>', methods=['POST'])
def leak_bucket_for_gateway__set_conf(session_id):
    """
    Set a new configuration for the Leaky Bucket.
    ---
    tags:
      - Leaky Bucket
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
    history = jsonify(leakyBucketForGateways[session_id].leakybucket_history_file)
    leakyBucketForGateways[session_id] = LeakyBucketForGateway.from_json(request.get_json())
    return make_response(history, 201)


@app.route('/leak-bucket-for-gateway/<string:session_id>', methods=['DELETE'])
def leak_bucket_for_gateway_delete(session_id):
    """
    Delete the Leaky Bucket instance to release resources.
    ---
    tags:
      - Leaky Bucket
    responses:
      204:
        description: Leaky Bucket instance deleted
    """
    del leakyBucketForGateways[session_id]
    return make_response('', 204)


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=80)
