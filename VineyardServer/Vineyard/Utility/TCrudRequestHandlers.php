<?php

namespace Vineyard\Utility;

trait TCrudRequestHandlers {
    public static function handleRequestsToBaseUri($method, array $requestParameters) {
        switch ($method) {
                
                case 'GET': // get list of all resource instance
                    $places = static::listAll();
                    echo json_encode($places);
                break;
                
                case 'POST': // create a new resource instance
                    $response = self::insert();
                    echo $response; // the new id or an error message
                break;
                
                case 'PUT':
                case 'DELETE':
                    // no id is given
                     http_response_code(400); // Bad Request
                break;
                
                default:
                    http_response_code(501); // Not Implemented
            }
    }
    
    public static function handleRequestsToUriWithId($method, array $requestParameters) {
        
        $resourceId = array_shift($requestParameters);
            
        if (!empty($requestParameters)) { // mmm.. too much stuff in request..
            http_response_code(501); // Not Implemented
            return;
        }

        switch ($method) {

            case 'GET': // get a place
                $resource = static::getById($resourceId);

                if (isset($resource->id))
                    echo json_encode($resource);
                else http_response_code(404); // Not Found
            break;

            case 'POST': // create a new resource instance with strange parameters? not implemented
                http_response_code(501); // Not Implemented
            break;

            case 'PUT':
                echo static::update($resourceId);
            break;

            case 'DELETE':
                echo static::delete($resourceId);
            break;

            default:
                http_response_code(501); // Not Implemented
        }
    } 
}

?>
