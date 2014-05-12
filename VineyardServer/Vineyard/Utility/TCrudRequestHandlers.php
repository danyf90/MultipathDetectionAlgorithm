<?php

namespace Vineyard\Utility;

trait TCrudRequestHandlers {
    public static function handleRequestsToBaseUri($method, array $requestParameters) {
        switch ($method) {
                
                case 'GET': // get list of all resource instance
                    $places = static::listAll();
                    return json_encode($places);
                break;
                
                case 'POST': // create a new resource instance
                    $response = static::insert();
                    return json_encode($response); // the new id or an error message
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

            case 'GET': // get a resource
                $resource = static::getById($resourceId);

                if (isset($resource->id))
                    return json_encode($resource);
                else http_response_code(404); // Not Found
            break;

            case 'POST': // create a new resource instance with strange parameters? not implemented
                http_response_code(501); // Not Implemented
            break;

            case 'PUT':
                $response = static::update($resourceId);
                if (!is_string($response))
                    return json_encode($response);
                return $response;
            break;

            case 'DELETE':
                $response = static::delete($resourceId);
                if (!is_string($response))
                    return json_encode($response);
                return $response;
            break;

            default:
                http_response_code(501); // Not Implemented
        }
    } 
}

?>
