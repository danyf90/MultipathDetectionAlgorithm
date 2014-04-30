<?php

namespace Vineyard\Model;

use \Vineyard\Utility\IResource;
use \Vineyard\Utility\TemporalORM;
use \Vineyard\Utility\TCrudRequestHandlers;

class Task extends TemporalORM implements IResource {
    
    use TCrudRequestHandlers;
    
    // TODO check method!
    public function check() { return array(); }
    public static function getTableName() { return 'task'; }
    
    public static function handleRequest($method, array $requestParameters) {

        switch (count($requestParameters)) {
            case 0: // api/task/
                return static::handleRequestsToBaseUri($method, $requestParameters);
            break;

            case 1: // api/task/<id>
                return static::handleRequestsToUriWithId($method, $requestParameters);
            break;

            case 2: // api/task/<id>/photo
                if ($requestParameters[1] != "photo") {
                    http_response_code(400); // Bad Request
                    return;
                }

                $id = array_shift($requestParameters);

                switch ($method) {
                    // the only one implemented
                    case "POST":
                        return static::insertPhoto($id);
                    break;

                    // case "DELETE": you must give me the url (filename) of the photo to delete
                    // case "GET": not implemented, all attributes are already returned with a place resource
                    // case "PUT": not implemented, delete and recreate it
                    default:
                        http_response_code(501); // Not Implemented
                        return;
                }

            break;
            
            case 3: //api/task/<id>/photo/<filename>
                if ($requestParameters[1] != "photo") {
                    http_response_code(400); // Bad Request
                    return;
                }

                $id = array_shift($requestParameters);
                /* "photo" = */ array_shift($requestParameters);
                $filename = array_shift($requestParameters);

                switch ($method) {            
                    case "GET":
                        static::getPhotoData($filename);
                        return;
                    break;
                    
                    case "DELETE":
                        return static::deletePhoto($filename);
                    break;
                    
                    case "POST": // cannot create with given filename: bad request
                        http_response_code(400); // Bad Request
                        return;
                    break;
                    
                    // case "PUT": not implemented, delete and recreate it!
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
    
    public static function getPhotoData($filename) {
        if (!file_exists($filename)) {
            http_response_code(404); // Not Found
            return;
        }
        
        header('Content-Type: image/jpg');
        header('Content-Length: ' . filesize($filename));
        readfile($filename);
    }
    
    public static function insertPhoto($id) {
        
        // same interface of an upload
        
        // TODO decide destination filename in f($id, ...)
        $filename = "";
        
        if (move_uploaded_file($_FILES['photo_upload']['tmp_name'], $filename)) {
            
            $pdo = DB::getConnection();
            try {
                
                $sql = $pdo->prepare("INSERT INTO `task_photo` (`task`, `url`) VALUES (?, ?)");
                $sql->execute(array($id, $filename));
                
                http_response_code(201); // Created
                return; // TODO return URL: /api/task/$id/photo/asdf.jpg
                
            } catch (PDOException $e) {
                // check which SQL error occured
                switch ($e->getCode()) {
                    default:
                        http_response_code(400); // Bad Request
                }
                
                unlink($filename);
            }
        } else {
            http_response_code(500); // Internal Server Error
            return;
        }
        
    }
    
    public static function deletePhoto($id, $filename) {
        
        $filename = ""; // TODO
        
        if (file_exists($filename)) {
            
            try {
                $sql = $pdo->prepare("DELETE FROM `task_photo` WHERE `task` = ? AND `url` = ?");
                $sql->execute(array($id, $filename));
                
                if ($sql->rowCount() == 1)
                    http_response_code(202); // Accepted
                else http_response_code(406); // Not Acceptable
                
                unlink($filename);
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
}

?>