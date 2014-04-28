<?php

namespace Vineyard\Utility;

use \Vineyard\Utility\IResource;

class RequestRouter {
	
	public static function route() {
        
        // echo "Your request: ", $_SERVER['REQUEST_METHOD'], " ", $_SERVER['REQUEST_URI'];
        
        $trimmedUri = trim($_SERVER['REQUEST_URI'], "/ ");
        $requestParams = explode("/", $trimmedUri);
        
        array_shift($requestParams); // remove "api" from request parameters

        $requestedClass = "\\Vineyard\\Model\\" . ucfirst(array_shift($requestParams));
        
		if (!class_exists($requestedClass) || // if class doesn't exist ...
            !in_array("Vineyard\\Utility\\IResource", class_implements($requestedClass))) { // ... or it doesn't implement IResource
            
            echo "<h1>No implementing class found.. This is a 404!</h1>",
            "<p> Your request: <strong>", $_SERVER['REQUEST_URI'], "</strong></p>";
            
			http_response_code(404);
			return;
        }
        
        // http_response_code is set inside handleRequest
		echo $requestedClass::handleRequest($_SERVER['REQUEST_METHOD'], $requestParams);
	}
}

?>
