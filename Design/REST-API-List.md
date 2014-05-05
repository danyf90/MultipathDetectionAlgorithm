
Vineyard APIs List
=============

This file describes the REST API available in the Vineyard PHP Server.  
All data is returned in JSON format.

Implemented APIs
-----------

### Place

* Retrieve all Place instances:

        GET http://vineyard-server.org/api/place

* Retrieve the Place instance with specified id:

        GET http://vineyard-server.org/api/place/<id>/

* Create a new Place:

        POST http://vineyard-server.org/place/
        
        POST DATA:
        name=...
        description=...
        position=...
        ...

* Update an existant Place with specified id:

        PUT http://vineyard-server.org/place/<id>
        
        PUT DATA:
        name=...
        description=...
        position=...
        ...

* Delete the Place with specified id:

        DELETE http://vineyard-server.org/place/<id>/
#### Place attributes


* Add an attribute (key-value string pair) to an existing Place with given id:

        POST http://vineyard-server.org/place/<id>/attribute/    
        
        POST DATA:
        key=...
        value=...

* Update an existant attribute (key-value string pair) to an existing Place with given id:

        PUT http://vineyard-server.org/place/<id>/attribute/
        
        PUT DATA:
        key=...
        value=...

* Delete an attribute with a given key of an existing Place with given id:

        DELETE http://vineyard-server.org/place/<id>/attribute/
        
        DELETE DATA:
        key=...
#### Place photo


* Add or replace the photo of the Place with given id:

        POST http://vineyard-server.org/place/<id>/photo/
        
        POST DATA:
        photo=<image data>

* Delete the Place photo:

        DELETE http://vineyard-server.org/place/<id>/photo/
#### Other Place data


* Get the list of issues or tasks of the Place with given id:

        GET http://vineyard-server.org/place/<id>/issues
        GET http://vineyard-server.org/place/<id>/tasks

* Get statistics (issue/task count) of all places

        GET http://vineyard-server.org/place/stats

* Get the hierarchy of Places

        GET http://vineyard-server.org/place/hierarchy

---

### Photo

* Get photo image data given the url referenced by a resource field

        GET http://vineyard-server.org/photo/<photoUrl>

---

### Task

* Get all Task instances

        GET http://vineyard-server.org/task

* Get the Task instance with specified id:

        GET http://vineyard-server.org/task/<id>


* Create a new Task:

        POST http://vineyard-server.org/task/
        
        POST DATA:
        title=...
        description=...
        issuer=...
        ...

* Update an existant Task with specified id:

        PUT	http://vineyard-server.org/issue/<id>/
        
        PUT DATA:
        title=...
        description=...
        issuer=...
        ...

* Delete the Task with specified id:

        DELETE http://vineyard-server.org/task/<id>/
#### Task photo


* Add a photo and associate it to the Task with given <id>

        POST http://vineyard-server.org/task/<id>/photo
        
        POST DATA:
        photo=<image data>

* Delete a photo associated to the Task with given id:

        DELETE http://vineyard-server.org/issue/<id>/photo/<photoUrl>

Not Implemented Yet
-------------------

### Worker

* Get worker(s)
        GET http://vineyard-server.org/worker/
        GET http://vineyard-server.org/worker/<id>

* CRUD on worker ID or username
        POST http://vineyard-server.org/worker/
        PUT/DELETE http://vineyard-server.org/worker/87
        PUT/DELETE http://vineyard-server.org/worker/pinco.pallo

### Group

* get group(s)
        GET http://vineyard-server.org/group/
        GET http://vineyard-server.org/group/<id>

* CRUD on group id
        POST http://vineyard-server.org/group/
        PUT/DELETE http://vineyard-server.org/group/<id>

* add/remove a worker from a group
        PUT/DELETE http://vineyard-server.org/group/<gid>/worker/<wid>

### Task/Issue

* Get Tasks per status

        GET http://vineyard-server.org/task/new/
        GET http://vineyard-server.org/task/assigned/
        GET http://vineyard-server.org/task/resolved/

### Photo

* Get cropped version of a particular photo

	GET http://vineyard-server.org/photo/<filename>/?w=<width>&h=<height>

