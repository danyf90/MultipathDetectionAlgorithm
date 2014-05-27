<?php

namespace Vineyard\Model;

use \PDO;
use \PDOException;
use \Vineyard\Utility\DB;
use \Vineyard\Utility\IResource;
use \Vineyard\Utility\Validator;
use \Vineyard\Utility\TrackedORM;
use \Vineyard\Utility\TCrudRequestHandlers;

use \Vineyard\Model\Photo;

class Place extends TrackedORM implements IResource {

    use TCrudRequestHandlers; // introduces handleRequestToBaseUri() and handleRequestToUriWithId()

    public function check() {
        $v = new Validator($this);

	if (!isset($this->id))
		$v->setCheckNotSetFields(true);

        $v->nonNull('name');
        $v->nullId('parent', get_class($this));
        $v->notSet('photo');
        $v->nullNumeric('latitude');
        $v->nullNumeric('longitude');

        return $v->getWrongFields();
    }

    public static function getTableName() { return 'place'; }

    // Override AbstractORM::getById() to include place attributes in object instance
    static public function getById($id) {

        $s = parent::getById($id);

        // add attributes and stats to place instance
        $s->loadAttributes();
        $s->loadStats();

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
                        return static::handleHierarchyRequest($method);
                        http_response_code(501); // Not Implemented
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

    public function loadAttributes() {
        if (!isset($this->id))
            return;

        $pdo = DB::getConnection();

        $sql = $pdo->prepare("SELECT `key`, `value` FROM `place_attribute` WHERE `place` = ?");
        $sql->execute(array($this->id));

        if ($sql->rowCount() > 0) {
            $attributes = array();
            while($row = $sql->fetch(PDO::FETCH_ASSOC))
                $attributes[$row['key']] = $row['value'];

            $this->attributes = $attributes;
        }
    }


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

            case "OPTIONS":
                header("Allow: POST, PUT, DELETE");
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
            static::updateLastModified();
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
            if ($sql->rowCount() == 1) {
                http_response_code(202); // Accepted
                static::updateLastModified();
            }
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
            if ($sql->rowCount() == 1) {
                http_response_code(202); // Accepted
                static::updateLastModified();
            }
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

        if (static::isNotModified()) {
            http_response_code(304); // Not Modified
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

        if (static::isNotModified()) {
            http_response_code(304); // Not Modified
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
                return static::insertPhoto($id);
            break;
            // delete the photo
            case "DELETE":
                return static::deletePhoto($id);
            break;

	    case "OPTIONS":
		header("Allow: POST, DELETE");
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
            static::updateLastModified();
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

    public function loadStats() {
        if (!isset($this->id))
            return;

        $pdo = DB::getConnection();
        $tQuery = "SELECT COUNT(*) AS `tasks`
            FROM `task`
            WHERE `end_time` IS NULL
            AND `issuer` IS NULL
            AND `place` = ?";

       $iQuery = "SELECT COUNT(*) AS `issues`
            FROM `task`
            WHERE `end_time` IS NULL
            AND `issuer` IS NOT NULL
            AND `place` = ?";

        $tSql = $pdo->prepare($tQuery);
        $tSql->execute(array($this->id));

        $this->tasks = $tSql->fetchColumn(0);

        $iSql = $pdo->prepare($iQuery);
        $iSql->execute(array($this->id));

        $this->issues = $iSql->fetchColumn(0);
    }

    public static function handleStatsRequest($method) {
        if ($method != "GET") {
            http_response_code(501); // Not Implemented
            return;
        }

        if (static::isNotModified()) {
            http_response_code(304); // Not Modified
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

    public function loadOffsprings($avoid = null) {
        $pdo = DB::getConnection();

         try {
	     $query = "SELECT id FROM `place` WHERE `parent` = ?";
	     $whereParams = array($this->id);

	     if (!is_null($avoid)) {
		  $query .= " AND `id` <> ?";
		  $whereParams[] = $avoid;
	     }

             $sql = $pdo->prepare($query);
             $sql->execute($whereParams);

             $children = array();

             while ($row = $sql->fetch(PDO::FETCH_ASSOC))
                 $children[] = static::getById($row['id']);

             if (count($children) > 0) {
                $this->children = $children;

                 foreach ($this->children as $child)
                     $child->loadOffsprings();
             }

        } catch (PDOException $e) {
            // check which SQL error occured
            switch ($e->getCode()) {
                default:
                    http_response_code(400); // Bad Request
            }
        }
    }

    public static function handleHierarchyRequest($method) {
        if ($method != "GET") {
            if ($method == "OPTIONS")
			     header("Allow: GET");
			else
            	http_response_code(501); // Not Implemented
            return;
        }

        if (static::isNotModified()) {
            http_response_code(304); // Not Modified
            return;
        }

	$avoid = ( isset($_GET['avoidOffsprings']) ) ? (int) $_GET['avoidOffsprings'] : null;

        $hierarchy = "{}";

        static::get(function($place) use (&$hierarchy, $avoid) {
	    if ($avoid == $place->id)
		return;

            $place->loadOffsprings($avoid);
            $hierarchy = json_encode($place);

        }, "`parent` IS NULL");

        return $hierarchy;
    }
}

?>
