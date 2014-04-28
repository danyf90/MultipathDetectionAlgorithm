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

    public function check() { return array(); }
    public static function getTableName() { return 'place'; }

    public static function handleRequest($method, array $requestParameters) {

        switch (count($requestParameters)) {
            case 0: // api/place/
                return static::handleRequestsToBaseUri($method, $requestParameters);
            break;

            case 1: // api/place/<id>
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
                    // case "GET":
                    // case "PUT":
                    // case "DELETE":
                    default:
                        http_response_code(501); // Not Implemented
                        return;
                }

            break;

            case 3: // api/place/<id>/attribute/<key>
                if ($requestParameters[1] != "attribute") {
                    http_response_code(400); // Bad Request
                    return;
                }

                $id = array_shift($requestParameters);
                /* "attribute" = */ array_shift($requestParameters);
                $key = array_shift($requestParameters);

                switch ($method) {
                    // the only one implemented
                    case "PUT":
                        return static::updateAttribute($id, $key);
                    break;

                    case "DELETE":
                        return static::deleteAttribute($id, $key);
                    break;

                    case "GET": // get particular attribute? surely not used, all attributes are retrieved together with the place
                    case "POST": // create with given ID? Not implemented
                    default:
                        http_response_code(501); // Not Implemented
                        return;
                }

            break;

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

    public static function updateAttribute($id, $key) {
        $pdo = DB::getConnection();
        
        // access PUT variables and put them in $_PUT for omogeinity
        parse_str(file_get_contents("php://input"), $_PUT);
        
        if (!isset($_PUT['value'])) {
            http_response_code(400); // Bad Request
            return;
        }
        
        try {
            $sql = $pdo->prepare("UPDATE `place_attribute` SET `value` = ? WHERE `place` = ? AND `key` = ?");
            $sql->execute(array($_PUT['value'], $id, $key));
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

    public static function deleteAttribute($id, $key) {
        $pdo = DB::getConnection();
        
         try {
            $sql = $pdo->prepare("DELETE FROM `place_attribute` WHERE `place` = ? AND `key` = ?");
            $sql->execute(array($id, $key));
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

}

?>