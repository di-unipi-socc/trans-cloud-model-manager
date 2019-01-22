# management-analyser-ws
A REST API for analysing and planning the management of TOSCA YAML apps, based on management protocols

## Running the management analyser
After cloning the repository of the **management analyser**, please move to the corresponding folder and issue the following commands:
* `mvn clean install` (to **build** the service)
* `java -jar target\management-analyser-ws-0.1.jar server config.yml` (to **run** the service)

## Using the management analyser
Once the **management analyser** is running, it can be used as follows.

#### Adding a new application
An application _app_ can be added to those managed by the **management analyser** by issuing
* a POST request to the `{host}/mm` endpoint,
* whose body is the TOSCA yaml specification of _app_ (see [POST-body](https://github.com/di-unipi-socc/management-analyser-ws/blob/master/data/examples/softcare-byon/POST-body.yaml) for a concrete example).

IMPORTANT: The current version of the **management analyser** requires that each requirement of each node in a POSTed application must indicate both the `node` and the `capability` satisfying such requirement (please see the corresponding [issue](https://github.com/di-unipi-socc/management-analyser-ws/issues/1)).

#### Setting the current and target state of an application
The current and target state of an application _app_ previously added to the **management analyser** can be updated by issuing
* a PUT request to the `{host}/mm/app` endpoint,
* whose body is a JSON object specifying the `current` and/or `target` state of each node forming _app_ (see [PUT-body](https://github.com/di-unipi-socc/management-analyser-ws/blob/master/data/examples/softcare-byon/PUT-body.json) for a concrete example).

#### Retrieving plans
Once the current and target state of an application _app_ have been set, the **management analyser** permits retrieving the sequence of operations allowing _app_ to move from the current state to the target one. Such a sequence can be retrieved by issuing
* a GET request to the `{host}/mm/SoftcareApp` endpoint.
The body of the reply of such requests will be as that in [GET-reply](https://github.com/di-unipi-socc/management-analyser-ws/blob/master/data/examples/softcare-byon/GET-reply.json)
