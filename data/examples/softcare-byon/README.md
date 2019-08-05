### Files for running the softcare-byon example

Once the **management analyser** is up and running, its API can be invoked to plan the management of the _Softcare_ application specified in `POST-body`.

More precisely, the _Softcare_ application can be added to those managed by the **management analyser** by issuing
* a POST request to the `{host}/mm` endpoint,
* whose body is the TOSCA yaml specification in [POST-body](https://github.com/di-unipi-socc/management-analyser-ws/blob/master/data/examples/softcare-byon/POST-body.yaml)

By default, the current and target state of the management planning are set to the initial state of the _Softcare_ application. To change the current and target state of such application, please issue:
* a PUT request to the `{host}/mm/SoftcareApp/plan` endpoint,
* whose body is (similar to that) in [PUT-body](https://github.com/di-unipi-socc/management-analyser-ws/blob/master/data/examples/softcare-byon/PUT-body.json)

Then, to retrieve the sequential plan allowing _Softcare_ to move from the specified current state to the specified target state, please issue
* a GET request to the `{host}/mm/SoftcareApp/plan` endpoint.

The body of the reply of such requests will be as that in [GET-plan-reply](https://github.com/di-unipi-socc/management-analyser-ws/blob/master/data/examples/softcare-byon/GET-plan-reply.json)

Instead, to retrieve a set of operations to be executed in parallel on _Softcare_ to update the current state to a state closer to the target state, please issue
* a GET request to the `{host}/mm/SoftcareApp/psteps` endpoint.

The body of the reply of such requests will be as that in [GET-psteps-reply](https://github.com/di-unipi-socc/management-analyser-ws/blob/master/data/examples/softcare-byon/GET-psteps-reply.json)
