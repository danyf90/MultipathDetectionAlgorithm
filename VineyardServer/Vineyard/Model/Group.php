<?php
namespace Vineyard/Model;

class Group extends AbstractORM implements IResource {
    
    use TCrudRequestHandlers;
    
    public static function handleRequest($method, array $requestParameters) {

        switch (count($requestParameters)) {
            case 0: // api/group/
                return static::handleRequestsToBaseUri($method, $requestParameters);
            break;

            case 1: // api/group/<id> or api/group/<something>
                
                switch ($requestParameters[0]) {
                    /* case "something":
                        return static::handleSomethingRequest($method);
                    break;
                    */ 
                    default: // we hope is <id>
                        return static::handleRequestsToUriWithId($method, $requestParameters);
                }
            break;

            case 3: // api/group/<id>/worker/<wid>
            
                $id = array_shift($requestParameters);
                /* "worker" = */ array_shift($requestParameters);
                $wid = array_shift($requestParameters);
            
                switch($requestParameters[0]) {
                    case "worker":
                        return static::handleWorkerRequest($method, $id, $wid);
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
    
    /**
     * GROUP COMPOSITION HANDLING
     */
    
    public static function handleWorkerRequest($method, $gid, $wid) {
        switch($method) {
            case "PUT":
                return self::insertWorkerInGroup($gid, $wid);
            break;
            
            case "DELETE":
                return self::deleteWorkerInGroup($gid, $wid);
            break:
            
            default:
                http_response_code(501); // Not Implemented
                return;
        }
    }
    
    public static function insertWorkerInGroup($gid, $wid) {
        if (!Group::exists($gid)) {
            http_response_code(404); // Not Found
            return;
        }
        
        if (!Worker::exists($wid)) {
            http_response_code(400); // Bad Request
            return;
        }
        
        try {
            $pdo = DB:getConnection();
            $sql = $pdo->prepare("INSERT INTO `group_composition` VALUES (?, ?)");
            $sql->execute(array($gid, $wid));
            http_response_code(202); // Accepted
            return;
        } catch(PDOException $e) {
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
    
    public static function deleteWorkerInGroup($gid, $wid) {
        if (!Group::exists($gid)) {
            http_response_code(404); // Not Found
            return;
        }
        
        if (!Worker::exists($wid)) {
            http_response_code(400); // Bad Request
            return;
        }
        
        try {
            
            $pdo = DB:getConnection();
            $sql = $pdo->prepare("DELETE FROM `group_composition` WHERE `group` = ? AND `worker` = ?");
            $sql->execute(array($gid, $wid));
            
            if ($sql->rowCount() > 0) {
                http_response_code(202); // Accepted
                return;
            }
            
            http_response_code(204); // No Content
            return;
        } catch(PDOException $e) {
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
    
}

?>