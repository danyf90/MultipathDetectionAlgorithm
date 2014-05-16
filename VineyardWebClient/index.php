<?php
    require_once("autoloader.php");
    
    use \Vineyard\Utility\Template;

    $rootPath = ""; // "/admin";

    $uriWithoutParams = strtok($_SERVER['REQUEST_URI'], '?');
    // TODO remove prefix $rootPath
    $trimmedUri = trim($uriWithoutParams, "/ ");
    $requestParams = explode("/", $trimmedUri);

    $t = new Template("templates/template-main.php");

	$requestedClass = "\\Vineyard\\Controller\\HomeController";

	if (strlen($requestParams[0]) > 0) {
		
    	$resource = array_shift($requestParams);
    	$requestedClass = "\\Vineyard\\Controller\\" . ucwords($resource) . "Controller";
		
    	if (!class_exists($requestedClass) || // if class doesn't exist ...
				!in_array("Vineyard\\Controller\\IController", class_implements($requestedClass))) { // ... or it doesn't implement IResource

        	$requestedClass = "\\Vineyard\\Controller\\ErrorController";   
        	http_response_code(404);
    	}
	}

    $requestedClass::handle($t, $requestParams);
    
    $t->render();
?>