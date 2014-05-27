
Vineyard APIs List
=============

This file describes the REST API available in the Vineyard PHP Server.
All data is returned in JSON format.

Implemented APIs
-----------

### Place

* Retrieve all Place instances (array):

        GET http://vineyard-server.org/api/place

* Retrieve all Place instances (hierarchy):

        GET http://vineyard-server.org/api/place/hierarchy

* Retrieve the Place instance with specified id:

        GET http://vineyard-server.org/api/place/<id>/

* Create a new Place:

        POST http://vineyard-server.org/api/place/

        POST DATA:
        name=...
        description=...
        position=...
        ...

* Update an existant Place with specified id:

        PUT http://vineyard-server.org/api/place/<id>

        PUT DATA:
        name=...
        description=...
        position=...
        ...

* Delete the Place with specified id:

        DELETE http://vineyard-server.org/api/place/<id>/

#### Place attributes

* Add an attribute (key-value string pair) to an existing Place with given id:

        POST http://vineyard-server.org/api/place/<id>/attribute/

        POST DATA:
        key=...
        value=...

* Update an existant attribute (key-value string pair) to an existing Place with given id:

        PUT http://vineyard-server.org/api/place/<id>/attribute/

        PUT DATA:
        key=...
        value=...

* Delete an attribute with a given key of an existing Place with given id:

        DELETE http://vineyard-server.org/api/place/<id>/attribute/

        DELETE DATA:
        key=...

#### Place photo

* Add or replace the photo of the Place with given id:

        POST http://vineyard-server.org/api/place/<id>/photo/

        POST DATA:
        photo=<image data>

* Delete the Place photo:

        DELETE http://vineyard-server.org/api/place/<id>/photo/

#### Other Place data

* Get the list of issues or tasks of the Place with given id:

        GET http://vineyard-server.org/api/place/<id>/issues
        GET http://vineyard-server.org/api/place/<id>/tasks

* Get statistics (issue/task count) of all places

        GET http://vineyard-server.org/api/place/stats

---

### Photo

* Get photo image data given the url referenced by a resource field

        GET http://vineyard-server.org/api/photo/<photoUrl>

* Get cropped photo image data given the url referenced by a resource field

        GET http://vineyard-server.org/api/photo/<photoUrl>/?w=<width>&h=<height>

---

### Task (Issue)

* Get all Task(Issue) instances

        GET http://vineyard-server.org/api/task
        GET http://vineyard-server.org/api/issue

* Get the Task(Issue) instance with specified id:

        GET http://vineyard-server.org/api/task/<id>
        GET http://vineyard-server.org/api/issue/<id>


* Create a new Task(Issue):

        POST http://vineyard-server.org/api/task/
        POST http://vineyard-server.org/api/issue/

        POST DATA:
        title=...
        description=...
        issuer=... (only for issues)
        ...

* Update an existant Task(Issue) with specified id:

		PUT	http://vineyard-server.org/api/task/<id>/
        PUT	http://vineyard-server.org/api/issue/<id>/

        PUT DATA:
        title=...
        description=...
        issuer=... (only for issues)
        ...

* Delete the Task(Issue) with specified id:

        DELETE http://vineyard-server.org/api/task/<id>/
        DELETE http://vineyard-server.org/api/issue/<id>/


* Get Task(Issue)s per status

        GET http://vineyard-server.org/api/task/new/
        GET http://vineyard-server.org/api/task/assigned/
        GET http://vineyard-server.org/api/task/resolved/
        GET http://vineyard-server.org/api/task/open/

        GET http://vineyard-server.org/api/issue/new/
        GET http://vineyard-server.org/api/issue/assigned/
        GET http://vineyard-server.org/api/issue/resolved/
        GET http://vineyard-server.org/api/issue/open/

#### Issue photo

* Add a photo and associate it to the Issue with given <id>

        POST http://vineyard-server.org/api/issue/<id>/photo

        POST DATA:
        photo=<image data>

* Delete a photo associated to the Issue with given id:

        DELETE http://vineyard-server.org/api/issue/<id>/photo/<photoUrl>

---

### Worker

* Worker Login (unsecure, but easy way)

        POST http://vineyard-server.org/api/worker/login/

        POST DATA:
        email=...
        password=md5(<password>)

---

### Group

* get group(s)

        GET http://vineyard-server.org/api/group/
        GET http://vineyard-server.org/api/group/<id>

* CRUD on group id

        POST http://vineyard-server.org/api/group/
        PUT/DELETE http://vineyard-server.org/api/group/<id>

* add/remove a worker from a group

        PUT/DELETE http://vineyard-server.org/api/group/<gid>/worker/<wid>

### Worker

* Get worker(s)

        GET http://vineyard-server.org/api/worker/
        GET http://vineyard-server.org/api/worker/<id>

* CRUD on worker ID or username

        POST http://vineyard-server.org/api/worker/
        PUT/DELETE http://vineyard-server.org/api/worker/87
        PUT/DELETE http://vineyard-server.org/api/worker/pinco.pallo
