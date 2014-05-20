<?php

namespace Vineyard\Utility;

trait TCrudRequestHandlers {

    protected static function isNotModified() {

        // always add Last-Modified header on each GET reqeust
        static::addLastModifiedHeader();

        $headers = apache_request_headers();
        if (!isset($headers['If-Modified-Since']))
            return false;

        $requestTime = strtotime($headers['If-Modified-Since']);
        $lastModifiedTime = strtotime(static::lastModified());
        // cache miss
        if ($lastModifiedTime > $requestTime)
            return false;

        // cache hit
        return true;
    }

    protected static function addLastModifiedHeader() {
        $time = strtotime(static::lastModified());
        header('Last-Modified: ' . gmdate('D, d M Y H:i:s', $time) . ' GMT');
    }

    public static function handleRequestsToBaseUri($method, array $requestParameters) {
        switch ($method) {

                case 'GET': // get list of all resource instance
                    if (static::isNotModified()) {
                        http_response_code(304); // Not Modified
                        return;
                    }

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

		case 'OPTIONS':
			header("Allow: GET, POST");
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
                if (static::isNotModified()) {
                    http_response_code(304); // Not Modified
                    return;
                }

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

	    case 'OPTIONS':
		header("Allow: GET, PUT, DELETE");
	    break;
            default:
                http_response_code(501); // Not Implemented
        }
    }
}

?>
