<?php

namespace Vineyard\Model;

use \PDO;
use \PDOException;
use \Vineyard\Utility\DB;
use \Vineyard\Utility\IResource;
use \Vineyard\Utility\Validator;
use \Vineyard\Utility\AbstractORM;
use \Vineyard\Utility\TCrudRequestHandlers;

use \Vineyard\Model\Photo;

class Place extends AbstractORM implements IResource {

    use TCrudRequestHandlers; // introduces handleRequestToBaseUri() and handleRequestToUriWithId()

    public function check() {
        $v = new Validator($this);

        $v->nonNull('name');
        $v->nullId('parent', 'Place');
        $v->notSet('photo');
        $v->nullNumeric('latitude');
        $v->nullNumeric('longitude');

        return $v->getWrongFields();
    }

    public static function getTableName() { return 'place'; }

    // Override AbstractORM::getById() to include place attributes in object instance
    static public function getById($id) {
		if (!is_numeric($id)) {
			http_response_code(400);
			return;
		}

		$s = new static();
		$s->load($id);

        // add attributes to place instance
        $pdo = DB::getConnection();

        try {
            $sql = $pdo->prepare("SELECT `key`, `value` FROM `place_attribute` WHERE `place` = ?");
            $sql->execute(array($id));
            // TODO better empty array if no attributes?
            if ($sql->rowCount() > 0) {
                $attributes = array();
                while($row = $sql->fetch(PDO::FETCH_ASSOC))
                    $attributes[$row['key']] = $row['value'];

                $s->attributes = $attributes;
            }

        } catch (PDOException $e) {
            // check which SQL error occured
            switch ($e->getCode()) {
                default:
                    http_response_code(400); // Bad Request
            }
        }

		return $s;
	}

    public static function handleRequest($method, array $requestParameters) {

        switch (count($requestParameters)) {
            case 0: // api/place/
                return static::handleRequestsToBaseUri($method, $requestParameters);
            break;

            case 1: // api/place/<id> or api/place/hierarchy or api/place/stats

                switch ($requestParameters[0]) {
                    case "stats":
                        return static::handleStatsRequest($method);
                    break;

                    case "hierarchy":
                        if ($method == 'GET')
                            return static::getHierarchy();
                        http_response_code(501); // Not Implemented
                        return;
                    break;

                    default: // we hope is <id>
                        return static::handleRequestsToUriWithId($method, $requestParameters);
                }
            break;

            case 2: // api/place/<id>/attribute | api/place/<id>/issues | api/place/<id>/photo

                $id = array_shift($requestParameters);

                switch($requestParameters[0]) {
                    case "attribute":
                        return static::handleAttributeRequest($method, $id);
                    break;

                    case "issues":
                        return static::handleIssuesRequest($method, $id);
                    break;

                    case "tasks":
                        return static::handleTasksRequest($method, $id);
                    break;

                    case "photo":
                        return static::handlePhotoRequest($method, $id);
                    break;

                    default:
                        http_response_code(400); // Bad Request
                        return;
                }
            break;

            default:
                http_response_code(501); // Not Implemented
                return;

        }
    }

    /**************************
     * ATTRIBUTE HANDLING
     **************************/

    public static function handleAttributeRequest($method, $id) {
        switch ($method) {
            // the only one implemented
            case "POST":
                return static::insertAttribute($id);
            break;

            case "PUT":
                return static::updateAttribute($id);
            break;

            case "DELETE":
                return static::deleteAttribute($id);
            break;

            // case "GET": not implemented, all attributes are already returned with a place resource
            default:
                http_response_code(501); // Not Implemented
                return;
        }
    }

    public static function insertAttribute($id) {
        $pdo = DB::getConnection();

        if (!isset($_POST['key']) || !isset($_POST['value'])) {
            http_response_code(400); // Bad Request
            return;
        }

        try {
            $sql = $pdo->prepare("INSERT INTO `place_attribute` (`place`, `key`, `value`) VALUES (?, ?, ?)");
            $sql->execute(array($id, $_POST['key'], $_POST['value']));
            http_response_code(201); // Created
            return;
        } catch (PDOException $e) {
            // check which SQL error occured
            switch ($e->getCode()) {
                case 23000: // SQL ERROR: Duplicate entry
                    http_response_code(406); // Not Acceptable
                break;

                default:
                    http_response_code(400); // Bad Request
            }
        }

    }

    public static function updateAttribute($id) {
        $pdo = DB::getConnection();

        // access PUT variables and put them in $_PUT for omogeinity
        parse_str(file_get_contents("php://input"), $_PUT);

        if (!isset($_PUT['key']) || !isset($_PUT['value'])) {
            http_response_code(400); // Bad Request
            return;
        }

        try {
            $sql = $pdo->prepare("UPDATE `place_attribute` SET `value` = ? WHERE `place` = ? AND `key` = ?");
            $sql->execute(array($_PUT['value'], $id, $_PUT['key']));
            if ($sql->rowCount() == 1)
                http_response_code(202); // Accepted
            else http_response_code(406); // Not Acceptable
            return;
        } catch (PDOException $e) {
            // check which SQL error occured
            switch ($e->getCode()) {
                default:
                    http_response_code(400); // Bad Request
            }
        }
    }

    public static function deleteAttribute($id) {
        $pdo = DB::getConnection();

        // access PUT variables and put them in $_PUT for omogeinity
        parse_str(file_get_contents("php://input"), $_DELETE);

        if (!isset($_DELETE['key'])) {
            http_response_code(400); // Bad Request
            return;
        }

        try {
            $sql = $pdo->prepare("DELETE FROM `place_attribute` WHERE `place` = ? AND `key` = ?");
            $sql->execute(array($id, $_DELETE['key']));
            if ($sql->rowCount() == 1)
                http_response_code(202); // Accepted
            else http_response_code(406); // Not Acceptable
            return;
        } catch (PDOException $e) {
            // check which SQL error occured
            switch ($e->getCode()) {
                default:
                    http_response_code(400); // Bad Request
            }
        }
    }

     /**************************
     * ISSUES/TASKS (PER PLACE) HANDLING
     **************************/

    public static function handleIssuesRequest($method, $id) {
        if ($method != "GET") {
            http_response_code(501); // Not Implemented
            return;
        }

        $issues = array();

        Task::get(function($issue) use (&$issues) {
            $issues[] = clone $issue;
        }, "`place` = ? AND `issuer` IS NOT NULL AND `status` <> 'done'", array($id));

        return json_encode($issues);
    }

     public static function handleTasksRequest($method, $id) {
        if ($method != "GET") {
            http_response_code(501); // Not Implemented
            return;
        }

        $tasks = array();

        Task::get(function($task) use (&$tasks) {
            $tasks[] = clone $task;
        }, "`place` = ? AND `issuer` IS NULL AND `status` <> 'done'", array($id));

        return json_encode($tasks);
    }

    /**************************
     * PHOTO HANDLING
     **************************/

    public static function handlePhotoRequest($method, $id) {
        switch ($method) {
            // creates or replaces the photo
            case "POST":
                static::insertPhoto($id);
            break;
            // delete the photo
            case "DELETE":
                static::deletePhoto($id);
            break;

            // case "GET": already returned in object instance!
            // case "PUT": use POST, it does the same thing
            default:
                http_response_code(501); // Not Implemented
        }
    }

    public static function insertPhoto($id) {

        $filename = "p" . time() . "_" . $id;

        $filepath = Photo::getFullPhotoPath($filename);

        if (move_uploaded_file($_FILES['photo']['tmp_name'], $filepath)) {

            $pdo = DB::getConnection();
            try {

                static::deletePhoto($id);

                $sql = $pdo->prepare("UPDATE `place` SET `photo` = ? WHERE `id` = ?");
                $sql->execute(array($filename, $id));

                http_response_code(201); // Created
                return; // TODO return URL: /api/place/$id/photo/<filename>

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

    public static function deletePhoto($id) {

        $pdo = DB::getConnection();

        try {
            $sql = $pdo->prepare("SELECT `photo` FROM `place` WHERE `id` = ?");
            $sql->execute(array($id));

            $photoUrl = $sql->fetchColumn(0);
            if ($photoUrl != null) {
                $filepath = Photo::getFullPhotoPath($photoUrl);
                if (file_exists($filepath))
                    unlink($filepath);

                $sql = $pdo->prepare("UPDATE `place` SET `photo` = NULL WHERE `id` = ?");
                $sql->execute(array($id));
            }

            http_response_code(202); // Accepted

        } catch (PDOException $e) {
            // check which SQL error occured
            switch ($e->getCode()) {
                default:
                    http_response_code(400); // Bad Request
            }
        }
    }

     /**************************
     * STATS HANDLING
     **************************/

    public static function handleStatsRequest($method) {
        if ($method != "GET") {
            http_response_code(501); // Not Implemented
            return;
        }

        $results = array();

        $pdo = DB::getConnection();
        $tQuery = "SELECT `place`, COUNT(*) AS `tasks`
            FROM `task`
            WHERE `end_time` IS NULL AND
            `issuer` IS NULL
            GROUP BY `place`";

       $iQuery = "SELECT `place`, COUNT(*) AS `issues`
            FROM `task`
            WHERE `end_time` IS NULL AND
            `issuer` IS NOT NULL
            GROUP BY `place`";

        try {

            $results = array();

            $tSql = $pdo->query($tQuery);

            while ($row = $tSql->fetch(PDO::FETCH_ASSOC))
                $results[$row['place']] = $row;

            $iSql = $pdo->query($iQuery);

            while ($row = $iSql->fetch(PDO::FETCH_ASSOC)) {
                if (isset($results[$row['place']]))
                    $newResult = array_merge($results[$row['place']], $row);
                else $newResult = $row;
                $results[$row['place']] = $newResult;
            }

            return json_encode(array_values($results));

        }  catch (PDOException $e) {
            // check which SQL error occured
            switch ($e->getCode()) {
                default:
                    http_response_code(400); // Bad Request
            }
        }

    }

     /**************************
     * HIERARCHY HANDLING
     **************************/

    public function loadOffsprings() {
        $pdo = DB::getConnection();

         try {
             $sql = $pdo->prepare("SELECT id FROM `place` WHERE `parent` = ?");
             $sql->execute(array($this->id));

             $children = array();

             while ($row = $sql->fetch(PDO::FETCH_ASSOC))
                 $children[] = static::getById($row['id']);

             $this->children = $children;

             foreach ($this->children as $child)
                 $child->loadOffsprings();


        } catch (PDOException $e) {
            // check which SQL error occured
            switch ($e->getCode()) {
                default:
                    http_response_code(400); // Bad Request
            }
        }
    }

    public static function getHierarchy() {

        $hierarchy = "";

        static::get(function($place) use (&$hierarchy) {
            $place->loadOffsprings();

            $hierarchy = json_encode($place);

        }, "`parent` IS NULL");

        return $hierarchy;
    }
}

?>