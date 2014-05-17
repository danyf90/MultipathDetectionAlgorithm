<?php

namespace Vineyard\Controller;

use \Vineyard\Utility\Template;

class WorkerController implements IController {
    public static function handle(Template $t, array $requestParams) {

		if (count($requestParams) > 0) {
			$id = array_shift($requestParams);
			$requestedTemplate = "templates/template-worker.php";
		 	$placeTemplate = new Template($requestedTemplate);
			$placeTemplate->id = $id;
		} else {
			$requestedTemplate = "templates/template-worker-all.php";
			$placeTemplate = new Template($requestedTemplate);
		}

        $t->content = $placeTemplate;
    }
}

?>