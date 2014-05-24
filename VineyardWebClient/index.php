<?php
    require_once("autoloader.php");
    
    use \Vineyard\Utility\Template;
	use \Vineyard\Controller\HomeController;
	use \Vineyard\Controller\ErrorController;
	use \Vineyard\Controller\SimpleResourceController;

    $rootPath = ""; // "/admin";

    $uriWithoutParams = strtok($_SERVER['REQUEST_URI'], '?');
    // TODO remove prefix $rootPath
    $trimmedUri = trim($uriWithoutParams, "/ ");
    $requestParams = explode("/", $trimmedUri);

    $t = new Template("templates/template-main.php");

	if (strlen($requestParams[0]) > 0) {
		
		try {
			$t = SimpleResourceController::handle($t, $requestParams);
		} catch (\Exception $e) {
			http_response_code($e->getCode());
			$t = ErrorController::handle($t, $requestParams);
		}
		
    	/*$resource = array_shift($requestParams);
    	$requestedClass = "\\Vineyard\\Controller\\" . ucwords($resource) . "Controller";
		
    	if (!class_exists($requestedClass) || // if class doesn't exist ...
				!in_array("Vineyard\\Controller\\IController", class_implements($requestedClass))) { // ... or it doesn't implement IResource

        	$requestedClass = "\\Vineyard\\Controller\\ErrorController";   
        	http_response_code(404);
    	}*/
	} else 
		$t = HomeController::handle($t, $requestParams);
    
    $t->render();
?>