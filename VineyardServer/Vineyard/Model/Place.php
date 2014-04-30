<?php

namespace Vineyard\Model;

use \PDO;
use \PDOException;
use \Vineyard\Utility\DB;
use \Vineyard\Utility\IResource;
use \Vineyard\Utility\AbstractORM;
use \Vineyard\Utility\TCrudRequestHandlers;

class Place extends AbstractORM implements IResource {

    use TCrudRequestHandlers; // introduces handleRequestToBaseUri() and handleRequestToUriWithId()

    // TODO check method!
    public function check() { return array(); }
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

            case 1: // api/place/<id> or api/place/hierarchy
                if ($requestParameters[0] == 'hierarchy') {
                    if ($method == 'GET')
                        return static::getHierarchy();
                    http_response_code(501); // Not Implemented
                    return;
                }
            
                return static::handleRequestsToUriWithId($method, $requestParameters);
            break;

            case 2: // api/place/<id>/attribute
                if ($requestParameters[1] != "attribute") {
                    http_response_code(400); // Bad Request
                    return;
                }

                $id = array_shift($requestParameters);

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

            break;

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
    
    public function loadChildren() {
        $pdo = DB::getConnection();
        
         try {
             $sql = $pdo->prepare("SELECT id FROM `place` WHERE `parent` = ?");
             $sql->execute(array($this->id));
             
             $children = array();
             
             while ($row = $sql->fetch(PDO::FETCH_ASSOC))
                 $children[] = static::getById($row['id']);
             
             $this->children = $children;

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
            $place->loadChildren();
            foreach ($place->children as $child)
                $child->loadChildren();

            $hierarchy = json_encode($place);
            
        }, "WHERE `parent` IS NULL");
        
        return $hierarchy;
    }
}

?>