<?php

namespace Vineyard\Model;

use \PDO;
use \Vineyard\Utility\DB;
use \Vineyard\Utility\IResource;
use \Vineyard\Utility\TemporalORM;
use \Vineyard\Utility\TCrudRequestHandlers;
use \Vineyard\Utility\Validator;
use Vineyard\Utility\Notificator;

use \Vineyard\Model\Photo;

class Issue extends TemporalORM implements IResource {

	use TCrudRequestHandlers;

    static $statusEnum = array('new','assigned','resolved');
    static $priorityEnum = array('low','medium','high');

    public function check() {
        $v = new Validator($this);

        $modelNamespace = "\\Vineyard\\Model\\";

        // title
        if (!isset($this->id))
            $v->set('title');

        $v->nonNull('title');
        // place
        if (!isset($this->id))
            $v->set('place');

        $v->nonNull('place');
        $v->id('place', $modelNamespace . "Place");
        // assign_time
        $v->nullTimestamp('assign_time');
        // due_time
        $v->nullTimestamp('due_time');
        // issuer
        $v->id('issuer', $modelNamespace . "Worker");
        // latitute
        $v->nullNumeric('latitude');
        // longitude
        $v->nullNumeric('longitude');
        // assigned_worker
        $v->nullId('assigned_worker', $modelNamespace . "Worker");
        // assigned_group
        $v->nullId('assigned_group', $modelNamespace . "Group");
        // assigner
        $v->nullId('assigner', $modelNamespace . "Worker");
        // modifier
        $v->id('modifier', $modelNamespace . "Worker");
        $v->set('modifier');
        // create_time
        $v->timestamp('create_time');
        // status
        $v->enum('status', self::$statusEnum);
        // priority
        $v->nullEnum('priority', self::$priorityEnum);
        // description
        // start_time
        $v->notSet('start_time');
        // end_time
        $v->notSet('end_time');

        return $v->getWrongFields();
    }
    
    public static function getTableName() { return 'task'; }

    // Override AbstractORM::listAll()
    public static function listAll() {

        $list = array();

        static::get(function($obj) use (&$list) {
            $list[] = clone $obj;
        }, "`issuer` IS NOT NULL");

        return $list;
    }

    // Override Task::onPostInsert()
    protected function onPostInsert() {
            $pdo = DB::getConnection();
            $sql = $pdo->prepare("UPDATE `task` SET `create_time` = `start_time` WHERE `id` = ? AND `end_time` IS NULL");
            $sql->execute(array($this->id));


            // Send notifications to all users
        $n = new Notificator();

        $title = "issue";
        $p = Place::getById($this->place);

        $description = $p->name . ": " . $this->title;
        if (isset($this->description) && strlen($this->description) > 0)
            $description .= " - " . $this->description;

        $message = array(
            'id' => $this->id,
            'title' => $title,
            'description' => $description,
            'placeId' => $this->place
        );

        $recipients = array();

        Worker::get(function ($worker) use (&$recipients) {
            $recipients[] = $worker->{"notification_id"};
        }, "`id` <> ? AND `notification_id` IS NOT NULL", array($this->modifier));

        $n->setData($message);
        $n->setRecipients($recipients);
        $n->send();
    }

    // Override Task::getById() to include issue photos in object instance
    static public function getById($id) {

        if (!isset($_GET['rev']))
            $s = parent::getById($id);
        else
            $s = static::getByRevision($id, $_GET['rev']);

        if (!$s->isIssue())
        	return new \stdClass();
        	
        // add photos to task instance
        $s->loadPhotos();

        return $s;
    }

    public function isIssue() {
        if (!isset($this->id))
            return;
        $pdo = DB::getConnection();
        $sql = $pdo->prepare("SELECT NOT(ISNULL(`issuer`)) FROM `task` WHERE `id` = ? AND `end_time` IS NULL");
        $sql->execute(array($this->id));
        return (bool) $sql->fetchColumn(0);

    }

    public static function handleRequest($method, array $requestParameters) {

        switch (count($requestParameters)) {
            case 0: // api/issue/
                return static::handleRequestsToBaseUri($method, $requestParameters);
            break;

            case 1: // api/issue/<id>
                if (!is_numeric($requestParameters[0]))
                    return static::handlePerStatusIssuesRequest($method, $requestParameters[0]);

                return static::handleRequestsToUriWithId($method, $requestParameters);
            break;

            case 2: // api/issue/<id>/photo or api/issue/<id>/revs/

                $id = array_shift($requestParameters);
                $service = array_shift($requestParameters);

                switch ($service) {

                    case "photo":
                        switch ($method) {
                            // the only one implemented
                            case "POST":
                                return static::insertPhoto($id);
                            break;

                            // case "DELETE": you must give me the url (filename) of the photo to delete
                            // case "GET": not implemented, all attributes are already returned with a task resource
                            // case "PUT": not implemented, delete and recreate it
                            default:
                                http_response_code(501); // Not Implemented
                                return;
                        }
                    break;

                    case "revs":
                        return static::handleRevisionsRequest($method, $id);
                    break;

                    default:
                        http_response_code(400); // Bad Request
                }

                $id = array_shift($requestParameters);


            break;

            case 3: //api/issue/<id>/photo/<filename>
                if ($requestParameters[1] != "photo") {
                    http_response_code(400); // Bad Request
                    return;
                }

                $id = array_shift($requestParameters);
                /* "photo" = */ array_shift($requestParameters);
                $filename = array_shift($requestParameters);

                switch ($method) {

                    case "DELETE":
                        return static::deletePhoto($id, $filename);
                    break;

                    case "POST": // cannot create with given filename: bad request
                        http_response_code(400); // Bad Request
                        return;
                    break;

                    case "OPTIONS":
                         header("Allow: DELETE,POST");
                    break;

                    // case "PUT": not implemented, delete and recreate it!
                    // case "GET": implemented in "Photo" resource
                    default:
                        http_response_code(501); // Not Implemented
                        return;
                }
            break;

            default:
                http_response_code(501); // Not Implemented
                return;

        }
    }

    /**
     * PHOTO MANAGEMENT
     */
    public function loadPhotos() {
        if (!isset($this->id))
            return;

        $pdo = DB::getConnection();

        try {
            $sql = $pdo->prepare("SELECT `url` FROM `task_photo` WHERE `task` = ?");
            $sql->execute(array($this->id));

            if ($sql->rowCount() > 0)
                $this->photos = $sql->fetchAll(PDO::FETCH_COLUMN, 0);

        } catch (PDOException $e) {
            // check which SQL error occured
            switch ($e->getCode()) {
                default:
                    http_response_code(400); // Bad Request
            }
        }
    }

    public static function insertPhoto($id) {

        $t = new static();
        $t->loadEmpty($id);
        if (!$t->isIssue()) {
            http_response_code(403); // Forbidden
            return;
        }

        if (!isset($_FILES['photo'])) {
            http_response_code(400); // Bad Request
            return;
        }

        // same interface of an upload
        $filename = "i" . time() . "_" . $id;
        $filepath = Photo::getFullPhotoPath($filename);

        if (move_uploaded_file($_FILES['photo']['tmp_name'], $filepath)) {

            $pdo = DB::getConnection();
            try {

                $sql = $pdo->prepare("INSERT INTO `task_photo` (`task`, `url`) VALUES (?, ?)");
                $sql->execute(array($id, $filename));

                http_response_code(201); // Created
                static::updateLastModified();
                return json_encode(array('url' => $filename));

            } catch (PDOException $e) {
                // check which SQL error occured
                switch ($e->getCode()) {
                    default:
                        http_response_code(400); // Bad Request
                }

                unlink($filepath);
            }
        } else {
            http_response_code(500); // Internal Server Error
            return;
        }

    }

    public static function deletePhoto($id, $filename) {

        $filepath = Photo::getFullPhotoPath($filename);

        if (file_exists($filepath)) {

            $pdo = DB::getConnection();

            try {
                $sql = $pdo->prepare("DELETE FROM `task_photo` WHERE `task` = ? AND `url` = ?");
                $sql->execute(array($id, $filename));

                if ($sql->rowCount() == 1) {
                    http_response_code(202); // Accepted
                    static::updateLastModified();
                }
                else http_response_code(406); // Not Acceptable

                unlink($filepath);
                return;
            } catch (PDOException $e) {
                // check which SQL error occured
                switch ($e->getCode()) {
                    default:
                    http_response_code(400); // Bad Request
                }
            }
        } else
            http_response_code(400); // Bad Request
    }

    /**
     * PER STATUS ISSUE LIST
     */

    public static function handlePerStatusIssuesRequest($method, $status) {
        if ($method != "GET") {
            http_response_code(501); // Not Implemeted
            return;
        }

        if (static::isNotModified()) {
            http_response_code(304); // Not Modified
            return;
        }

        $issues = array();

        switch ($status) {
            case "open": $where = "`status` <> 'resolved'"; break;
            case "new": $where = "`status` = 'new'"; break;
            case "assigned": $where = "`status` = 'assigned'"; break;
            case "resolved": $where = "`status` = 'resolved'"; break;
            default:
                http_response_code(400); // Bad Request
                return;
        }

        static::get(function($issue) use (&$issues) {
            $issues[] = clone $issue;
        }, $where . " AND `issuer` IS NOT NULL");

        return json_encode($issues);
    }

    /**
     * ISSUE REVISIONS
     */

    public static function handleRevisionsRequest($method, $id) {
        if ($method != "GET") {
            http_response_code(501); // Not Implemented
            return;
        }

        $pdo = DB::getConnection();
        try {
            $sql = $pdo->prepare("SELECT `end_time`, `modifier` FROM `" . static::getTableName() . "` WHERE `id` = ? AND `issuer` IS NOT NULL ORDER BY `end_time` DESC");
            $sql->execute(array($id));

            if ($sql->rowCount() > 0)
				return json_encode($sql->fetchAll(PDO::FETCH_ASSOC));
				
			http_response_code(404);
			return;


        } catch (PDOException $e) {
            // check which SQL error occured
            switch ($e->getCode()) {
                default:
                    http_response_code(400); // Bad Request
            }
        }
    }
}

?>