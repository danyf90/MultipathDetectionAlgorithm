<?php

namespace Vineyard\Model;

use \Vineyard\Utility\IResource;

class Photo implements IResource {
    
    const PHOTO_DIRECTORY = "Photo/";

    public static function handleRequest($method, array $requestParameters) {
        
        if ( $method != "GET" ) {
            http_response_code(501); // Not Implemented;
            return;
        }
        
        if ( count($requestParameters) != "1" ) {
            http_response_code(400); // Bad Request;
            return;
        }
        
        $filename = array_shift($requestParameters);
        
        static::echoPhotoData($filename);        
    }
    
    public static function getFullPhotoPath($filename) {
        return static::PHOTO_DIRECTORY . $filename;
    }
    
    protected static function echoPhotoData($filename) {
        
        $filename = static::getFullPhotoPath($filename);
        
        if (!file_exists($filename)) {
            http_response_code(404); // Not Found
            return;
        }
        
        header('Content-Type: image/jpg');
        header('Content-Length: ' . filesize($filename));
        readfile($filename);
    }
}

?>