<?php

namespace Vineyard\Controller;

use \Vineyard\Utility\Template;

class SimpleResourceController implements IController {
    public static function handle(Template $t, array $requestParams) {
		
		$resource = array_shift($requestParams);
		try {
			if (count($requestParams) > 0) {
				$id = array_shift($requestParams);
				$requestedTemplate = "templates/template-" . $resource . ".php";
				$resourceTemplate = new Template($requestedTemplate);
				$resourceTemplate->id = $id;
			} else {
				$requestedTemplate = "templates/template-" . $resource . "-all.php";
				$resourceTemplate = new Template($requestedTemplate);
			}
		} catch (\Exception $e) {
			throw new \Exception($e->getMessage(), 404);
		}
        
        $t->content = $resourceTemplate;
		return $t;
    }
}

?>