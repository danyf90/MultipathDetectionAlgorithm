<?php

namespace Vineyard\Model;

use \PDO;
use \Vineyard\Utility\DB;
use \Vineyard\Utility\IResource;
use \Vineyard\Utility\TemporalORM;
use \Vineyard\Utility\TCrudRequestHandlers;
use \Vineyard\Utility\Validator;
use Vineyard\Utility\Notificator;

class Task extends TemporalORM implements IResource {

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
        $v->nullId('issuer', $modelNamespace . "Worker");
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
        }, "`issuer` IS NULL");

        return $list;
    }

    // Override AbstractORM::onPostInsert()
    protected function onPostInsert() {
            $pdo = DB::getConnection();
            $sql = $pdo->prepare("UPDATE `task` SET `create_time` = `start_time` WHERE `id` = ? AND `end_time` IS NULL");
            $sql->execute(array($this->id));


            // Send notifications to all users
        $n = new Notificator();

        $title = "task";
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

    // Override AbstractORM::getById() to include task revisions
    static public function getById($id) {

        if (!isset($_GET['rev']))
            $s = parent::getById($id);
        else
            $s = static::getByRevision($id, $_GET['rev']);
            
        if ($s->isIssue())
        	return new \stdClass();

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
            case 0: // api/task/
                return static::handleRequestsToBaseUri($method, $requestParameters);
            break;

            case 1: // api/task/<id>
                if (!is_numeric($requestParameters[0]))
                    return static::handlePerStatusTasksRequest($method, $requestParameters[0]);

                return static::handleRequestsToUriWithId($method, $requestParameters);
            break;

            case 2: // api/task/<id>/revs/

                $id = array_shift($requestParameters);
                $service = array_shift($requestParameters);

                switch ($service) {

                    case "revs":
                        return static::handleRevisionsRequest($method, $id);
                    break;

                    default:
                        http_response_code(400); // Bad Request
                }

                $id = array_shift($requestParameters);


            break;

            default:
                http_response_code(501); // Not Implemented
                return;

        }
    }

    /**
     * PER STATUS TASK LIST
     */

    public static function handlePerStatusTasksRequest($method, $status) {
        if ($method != "GET") {
            http_response_code(501); // Not Implemeted
            return;
        }

        if (static::isNotModified()) {
            http_response_code(304); // Not Modified
            return;
        }

        $tasks = array();

        switch ($status) {
            case "open": $where = "`status` <> 'resolved'"; break;
            case "new": $where = "`status` = 'new'"; break;
            case "assigned": $where = "`status` = 'assigned'"; break;
            case "resolved": $where = "`status` = 'resolved'"; break;
            default:
                http_response_code(400); // Bad Request
                return;
        }

        static::get(function($task) use (&$tasks) {
            $tasks[] = clone $task;
        }, $where . " AND `issuer` IS NULL");

        return json_encode($tasks);
    }

    /**
     * TASK REVISIONS
     */

    public static function handleRevisionsRequest($method, $id) {
        if ($method != "GET") {
            http_response_code(501); // Not Implemented
            return;
        }

        $pdo = DB::getConnection();
        try {
            $sql = $pdo->prepare("SELECT `end_time`, `modifier` FROM `" . static::getTableName() . "` WHERE `id` = ? AND `issuer` IS NULL ORDER BY `end_time` DESC");
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
